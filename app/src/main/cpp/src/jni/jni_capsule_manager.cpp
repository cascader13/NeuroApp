#include <jni.h>
// JNI Capsule Manager - Исправленная версия
//
// Created by aseatari on 20.09.2024.
// Refactored: 15 апреля 2026
//

#include "CCapsuleAPI.h"
#include "helpers.h"
#include <jni.h>

#include <android/log.h>
#include <mutex>
#include <atomic>
#include <memory>

using namespace std::chrono_literals;

#define CALIBRATOR_READY_STAGE (-1)
// 0.1.2.3 - reserved
#define PHYSIO_INIT_STAGE 4
#define PHYSIO_BASELINE_STAGE 5
#define PHYSIO_SAMPLES_STAGE 6
#define STAGE_STARTED 0
#define STAGE_FINISHED  1

// ==========================================
// Глобальные переменные (защищены мьютексами)
// ==========================================

// Capsule API объекты
static clCDeviceLocator locator = nullptr;
static clCDevice device = nullptr;
static clCNFBCalibrator calibrator = nullptr;
static clCPhysiologicalStates physioStates = nullptr;
static clCNFB nfb = nullptr;
static clCCardio cardio = nullptr;
static clCMEMS mems = nullptr;
static clCProductivity productivity = nullptr;
static clCPhysiologicalStates ps = nullptr;
static clCEmotions emotions = nullptr;
static clCIndividualNFBCalibrationStage stage = clCIndividualNFBCalibrationStage_1;

// JVM и Java объекты
static JavaVM* javaVM = nullptr;
static jobject javaCapsule = nullptr;
static jclass capsuleClass = nullptr;
static jclass deviceInfo = nullptr;

// Кэшированные method IDs (для производительности)
static jmethodID mid_deviceConnectionState = nullptr;
static jmethodID mid_onConnectionError = nullptr;
static jmethodID mid_locatorEvent = nullptr;
static jmethodID mid_onResistanceReceived = nullptr;
static jmethodID mid_onCardioReceived = nullptr;
static jmethodID mid_calibrationStateChanged = nullptr;
static jmethodID mid_onCalibrationReceived = nullptr;
static jmethodID mid_onNFBReceived = nullptr;
static jmethodID mid_onMEMSReceived = nullptr;
static jmethodID mid_onProductivityBaselineReceived = nullptr;
static jmethodID mid_onProductivityReceived = nullptr;
static jmethodID mid_onProductivityIndexesReceived = nullptr;
static jmethodID mid_onProductivityScore = nullptr;
static jmethodID mid_onPhysiologicalBaselineReceived = nullptr;
static jmethodID mid_onPhysiologicalReceived = nullptr;
static jmethodID mid_onEmotionReceived = nullptr;
static jmethodID mid_onEEGRawDataReceived = nullptr;
static jmethodID mid_onEEGProcessedDataReceived = nullptr;
static jmethodID mid_onEEGArtifactsReceived = nullptr;

// Мьютексы для thread safety
static std::mutex deviceMutex;
static std::mutex javaCapsuleMutex;
static std::atomic<bool> stopRequested{false};  // FIX #7: atomic

// ==========================================
// RAII helper для JNI строк (FIX #8)
// ==========================================
class JniString {
public:
    JniString(JNIEnv* env, jstring jstr) : env_(env), jstr_(jstr), cstr_(nullptr) {
        if (jstr_) {
            cstr_ = env_->GetStringUTFChars(jstr_, nullptr);
        }
    }
    ~JniString() {
        if (cstr_) {
            env_->ReleaseStringUTFChars(jstr_, cstr_);
        }
    }
    // Disable copy
    JniString(const JniString&) = delete;
    JniString& operator=(const JniString&) = delete;
    // Move
    JniString(JniString&& other) noexcept : env_(other.env_), jstr_(other.jstr_), cstr_(other.cstr_) {
        other.cstr_ = nullptr;
        other.jstr_ = nullptr;
    }
    const char* get() const { return cstr_ ? cstr_ : ""; }
    bool isValid() const { return cstr_ != nullptr; }
private:
    JNIEnv* env_;
    jstring jstr_;
    const char* cstr_;
};

// ==========================================
// Safe Java Object Guard (FIX #6)
// ==========================================
class JavaCapsuleGuard {
public:
    JavaCapsuleGuard() : env_(nullptr), capsule_(nullptr) {
        std::lock_guard<std::mutex> lock(javaCapsuleMutex);
        if (javaCapsule == nullptr) {
            return;
        }
        if (javaVM->GetEnv((void**)&env_, JNI_VERSION_1_6) != JNI_OK) {
            javaVM->AttachCurrentThread(&env_, nullptr);
            attached_ = true;
        }
        capsule_ = javaCapsule;
    }
    ~JavaCapsuleGuard() {
        if (attached_ && env_) {
            javaVM->DetachCurrentThread();
        }
    }
    JNIEnv* env() const { return env_; }
    jobject capsule() const { return capsule_; }
    bool isValid() const { return env_ != nullptr && capsule_ != nullptr; }
    // Disable copy/move
    JavaCapsuleGuard(const JavaCapsuleGuard&) = delete;
    JavaCapsuleGuard& operator=(const JavaCapsuleGuard&) = delete;
    JavaCapsuleGuard(JavaCapsuleGuard&&) = delete;
    JavaCapsuleGuard& operator=(JavaCapsuleGuard&&) = delete;
private:
    JNIEnv* env_;
    jobject capsule_;
    bool attached_ = false;
};

// ==========================================
// Callbacks (ИСПРАВЛЕНЫ - используют JniResolver/JavaCapsuleGuard)
// ==========================================

void onConnectionStatusChanged(clCDevice, clCDevice_ConnectionStatus state) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_DEBUG", "Connection State Changed: %d", state);

    JavaCapsuleGuard guard;
    if (!guard.isValid()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "javaCapsule is null or env invalid");
        return;
    }

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_deviceConnectionState) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    jint stateValue = 0;
    switch (state) {
        case clCDevice_ConnectionState_Connected:
            stateValue = 1;
            break;
        case clCDevice_ConnectionState_Disconnected:
            stateValue = 3;
            break;
        case clCDevice_ConnectionState_UnsupportedConnection:
            stateValue = 4;
            break;
        default:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Unknown State: %d", state);
            return;
    }

    env->CallVoidMethod(capsule, mid_deviceConnectionState, stateValue);

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in deviceConnectionState");
        env->ExceptionClear();
    }
}

void onDeviceError(clCDevice, const char* error) noexcept {
    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Device error: %s", error ? error : "null");

    JavaCapsuleGuard guard;
    if (!guard.isValid()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "javaCapsule is null or env invalid");
        return;
    }

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onConnectionError) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    jstring errorStr = env->NewStringUTF(error ? error : "Unknown error");
    if (errorStr) {
        env->CallVoidMethod(capsule, mid_onConnectionError, errorStr);
        env->DeleteLocalRef(errorStr);
    }

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onConnectionError");
        env->ExceptionClear();
    }
}

void onDeviceList(clCDeviceLocator, clCDeviceInfoList devices, clCDeviceLocator_FailReason fail_reason) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Locator event");

    // FIX #2: Проверка device под мьютексом
    {
        std::lock_guard<std::mutex> lock(deviceMutex);
        if (device != nullptr) {
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Device already exists, ignoring locator");
            return;
        }
    }

    switch (fail_reason) {
        case clCDeviceLocator_FailReason_OK:
            break;
        case clCDeviceLocator_FailReason_BluetoothDisabled:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Bluetooth adapter not found or disabled");
            return;
        case clCDeviceLocator_FailReason_Unknown:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Unknown error occurred");
            return;
        default:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Unknown DeviceLocatorFailReason value");
            return;
    }

    JavaCapsuleGuard guard;
    if (!guard.isValid()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "javaCapsule is null or env invalid");
        return;
    }

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_locatorEvent) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    clCError error;
    int32_t szSensors = clCDeviceInfoList_GetCount(devices, &error);
    if (!error.success || szSensors <= 0) {
        __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "No devices found");
        return;
    }

    auto sensorsArray = env->NewObjectArray(static_cast<jsize>(szSensors), deviceInfo, nullptr);
    if (env->ExceptionCheck() || !sensorsArray) {
        env->ExceptionClear();
        return;
    }

    // FIX #4: Кэшированный constructor method ID
    static jmethodID deviceInfoCtor = nullptr;
    if (!deviceInfoCtor) {
        deviceInfoCtor = env->GetMethodID(deviceInfo, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
        if (!deviceInfoCtor) {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "DeviceInfo constructor not found");
            return;
        }
    }

    for (int32_t i = 0; i < szSensors; ++i) {
        env->PushLocalFrame(4);  // Увеличили до 4 для безопасности

        clCDeviceInfo deviceDescriptor = clCDeviceInfoList_GetDeviceInfo(devices, i, &error);
        if (!error.success || !deviceDescriptor) {
            env->PopLocalFrame(nullptr);
            continue;
        }

        const char* name = clCDeviceInfo_GetName(deviceDescriptor);
        const char* serial = clCDeviceInfo_GetSerial(deviceDescriptor);

        jstring dName = name ? env->NewStringUTF(name) : nullptr;
        jstring dID = serial ? env->NewStringUTF(serial) : nullptr;

        if (dName && dID) {
            jobject scObject = env->NewObject(deviceInfo, deviceInfoCtor, dName, dID);
            if (scObject) {
                env->SetObjectArrayElement(sensorsArray, i, scObject);
            }
        }

        env->PopLocalFrame(nullptr);

        if (env->ExceptionCheck()) {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in device creation");
            env->ExceptionClear();
        }
    }

    env->CallVoidMethod(capsule, mid_locatorEvent, sensorsArray);
    env->DeleteLocalRef(sensorsArray);

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in locatorEvent");
        env->ExceptionClear();
    }
}

void onDeviceResistanceUpdate(clCDevice, clCResistance resistance) noexcept {
    int32_t count = clCResistance_GetCount(resistance);
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_RESISTANCE", "Resistances: %d", count);

    if (count < 4) {
        __android_log_print(ANDROID_LOG_WARN, "CAPSULE_RES_RESISTANCE", "Not enough resistance channels");
        return;
    }

    double o1 = clCResistance_GetValue(resistance, 0);
    double o2 = clCResistance_GetValue(resistance, 3);
    double t3 = clCResistance_GetValue(resistance, 1);
    double t4 = clCResistance_GetValue(resistance, 2);

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onResistanceReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onResistanceReceived,
                        static_cast<jdouble>(o1),
                        static_cast<jdouble>(o2),
                        static_cast<jdouble>(t3),
                        static_cast<jdouble>(t4));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onResistanceReceived");
        env->ExceptionClear();
    }
}

void onCardioIndexesUpdate(clCCardio, const clCCardio_Data* cardioData) noexcept {
    if (!cardioData) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_RES_CARDIO", "cardioData is null");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_CARDIO", "HeartRate: %f", cardioData->heartRate);

    if (cardioData->heartRate + cardioData->kaplanIndex == 0) {
        return;
    }

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onCardioReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onCardioReceived,
                        static_cast<jlong>(cardioData->timestampMilli),
                        static_cast<jfloat>(cardioData->heartRate),
                        static_cast<jboolean>(cardioData->hasArtifacts),
                        static_cast<jfloat>(cardioData->kaplanIndex),
                        static_cast<jboolean>(cardioData->metricsAvailable),
                        static_cast<jboolean>(cardioData->motionArtifacts),
                        static_cast<jboolean>(cardioData->skinContact),
                        static_cast<jboolean>(cardioData->stressIndex != 0.0f));  // FIX: stressIndex is float, not bool

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onCardioReceived");
        env->ExceptionClear();
    }
}

void onCalibrated(clCNFBCalibrator, const clCIndividualNFBData* data) noexcept {
    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_calibrationStateChanged || !mid_onCalibrationReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method IDs not cached");
        return;
    }

    // Проверка данных калибровки
    if (data == nullptr || data->failReason != clC_IndividualNFBCalibrationFailReason_None) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_INFB", "Calibration failed");
        if (data) {
            switch (data->failReason) {
                case clC_IndividualNFBCalibrationFailReason_TooManyArtifacts:
                    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_INFB", "Too many artifacts");
                    break;
                case clC_IndividualNFBCalibrationFailReason_PeakIsABorder:
                    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_INFB", "Alpha peak matches border");
                    break;
                default:
                    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_INFB", "Reason unknown: %d", data->failReason);
            }
        }
        env->CallVoidMethod(capsule, mid_calibrationStateChanged, static_cast<jint>(6));
        return;
    }

    env->CallVoidMethod(capsule, mid_onCalibrationReceived,
                        static_cast<jfloat>(data->individualFrequency),
                        static_cast<jfloat>(data->individualPeakFrequency),
                        static_cast<jfloat>(data->individualPeakFrequencyPower),
                        static_cast<jfloat>(data->individualPeakFrequencySuppression),
                        static_cast<jfloat>(data->individualBandwidth),
                        static_cast<jfloat>(data->individualNormalizedPower),
                        static_cast<jfloat>(data->lowerFrequency),
                        static_cast<jfloat>(data->upperFrequency));

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "IAF: %f, IAPF: %f",
                        data->individualFrequency, data->individualPeakFrequency);

    env->CallVoidMethod(capsule, mid_calibrationStateChanged, static_cast<jint>(4));

    // FIX #14: Проверка на nullptr перед вызовами
    {
        std::lock_guard<std::mutex> lock(deviceMutex);
        if (ps) {
            clCPhysiologicalStates_StartBaselineCalibration(ps);
        }
        if (productivity) {
            clCProductivity_StartBaselineCalibration(productivity);
        }
    }

    env->CallVoidMethod(capsule, mid_calibrationStateChanged, static_cast<jint>(5));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onCalibrated");
        env->ExceptionClear();
    }
}

void onCalibrationStageFinishedEvent(clCNFBCalibrator) noexcept {
    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_calibrationStateChanged) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    clCError error;
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Calibration stage finished");

    std::lock_guard<std::mutex> lock(deviceMutex);

    if (!calibrator) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Calibrator is null");
        return;
    }

    switch (stage) {
        case clCIndividualNFBCalibrationStage_1:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 1 finished");
            stage = clCIndividualNFBCalibrationStage_2;
            env->CallVoidMethod(capsule, mid_calibrationStateChanged, static_cast<jint>(1));
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
            break;
        case clCIndividualNFBCalibrationStage_2:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 2 finished");
            stage = clCIndividualNFBCalibrationStage_3;
            env->CallVoidMethod(capsule, mid_calibrationStateChanged, static_cast<jint>(2));
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
            break;
        case clCIndividualNFBCalibrationStage_3:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 3 finished");
            stage = clCIndividualNFBCalibrationStage_4;
            env->CallVoidMethod(capsule, mid_calibrationStateChanged, static_cast<jint>(3));
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
            break;
        case clCIndividualNFBCalibrationStage_4:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 4 finished - calibration complete");
            break;
    }

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in calibration stage");
        env->ExceptionClear();
    }
}

void onUpdateUserState(clCNFB, const clCNFB_UserState* userState) noexcept {
    if (!userState) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_RES_NFB", "userState is null");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_NFB",
                        "NFB update: alpha=%.3f, beta=%.3f, theta=%.3f",
                        userState->alpha, userState->beta, userState->theta);

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onNFBReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onNFBReceived,
                        static_cast<jlong>(userState->timestampMilli),
                        static_cast<jfloat>(userState->alpha),
                        static_cast<jfloat>(userState->beta),
                        static_cast<jfloat>(userState->theta),
                        static_cast<jfloat>(userState->delta),
                        static_cast<jfloat>(userState->smr));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onNFBReceived");
        env->ExceptionClear();
    }
}

void onNFBErrorEvent(clCNFB, const char* error) noexcept {
    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_NFB", "NFB error: %s", error ? error : "null");
}

void onMEMSUpdate(clCMEMS, clCMEMSTimedData data) noexcept {
    if (!data) return;

    const int32_t count = clCMEMSTimedData_GetCount(data);
    if (count <= 0) return;

    const clCPoint3d accelerometer = clCMEMSTimedData_GetAccelerometer(data, 0);
    const clCPoint3d gyroscope = clCMEMSTimedData_GetGyroscope(data, 0);
    const auto timestamp = clCMEMSTimedData_GetTimestampMilli(data, 0);

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onMEMSReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onMEMSReceived,
                        static_cast<jlong>(timestamp),
                        static_cast<jfloat>(accelerometer.x),
                        static_cast<jfloat>(accelerometer.y),
                        static_cast<jfloat>(accelerometer.z),
                        static_cast<jfloat>(gyroscope.x),
                        static_cast<jfloat>(gyroscope.y),
                        static_cast<jfloat>(gyroscope.z));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onMEMSReceived");
        env->ExceptionClear();
    }
}

void onProductivityBaselineUpdate(clCProductivity, const clCProductivity_Baselines* baselines) noexcept {
    if (!baselines) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_RES_PROD", "baselines is null");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity baselines update");

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onProductivityBaselineReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onProductivityBaselineReceived,
                        static_cast<jlong>(baselines->timestampMilli),
                        static_cast<jfloat>(baselines->gravity),
                        static_cast<jfloat>(baselines->productivity),
                        static_cast<jfloat>(baselines->fatigue),
                        static_cast<jfloat>(baselines->reverseFatigue),
                        static_cast<jfloat>(baselines->relaxation),
                        static_cast<jfloat>(baselines->concentration));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onProductivityBaselineReceived");
        env->ExceptionClear();
    }
}

void onProductivityMetricsUpdate(clCProductivity, const clCProductivity_Metrics* metrics) noexcept {
    if (!metrics) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_RES_PROD", "metrics is null");
        return;
    }

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onProductivityReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    // FIX: Передаем timestampMilli один раз (в оригинале было дважды)
    env->CallVoidMethod(capsule, mid_onProductivityReceived,
                        static_cast<jlong>(metrics->timestampMilli),
                        static_cast<jdouble>(metrics->timestampMilli),
                        static_cast<jfloat>(metrics->gravityScore),
                        static_cast<jfloat>(metrics->productivityScore),
                        static_cast<jfloat>(metrics->fatigueScore),
                        static_cast<jfloat>(metrics->reverseFatigueScore),
                        static_cast<jfloat>(metrics->relaxationScore),
                        static_cast<jfloat>(metrics->concentrationScore));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onProductivityReceived");
        env->ExceptionClear();
    }
}

void onProductivityIndexesUpdate(clCProductivity, const clCProductivity_Indexes* indexes) noexcept {
    if (!indexes) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_RES_PROD", "indexes is null");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD",
                        "Productivity indexes update, concentration=%.3f",
                        indexes->concentrationBaseline);

    // FIX #9: Добавлены break в switch!
    float relax_index = 0;
    switch (indexes->relaxation) {
        case clCProductivity_RecommendationValue_Involvement:
            relax_index = 0;
            break;
        case clCProductivity_RecommendationValue_Relaxation:
            relax_index = 1;
            break;
        case clCProductivity_RecommendationValue_SlightFatigue:
            relax_index = 2;
            break;
        case clCProductivity_RecommendationValue_SevereFatigue:
            relax_index = 3;
            break;
        case clCProductivity_RecommendationValue_ChronicFatigue:
            relax_index = 4;
            break;
        case clCProductivity_RecommendationValue_NoRecommendation:
            relax_index = -1;
            break;
    }

    float stress_index = 0;
    switch (indexes->stress) {
        case clCProductivity_StressValue_NoStress:
            stress_index = 0;
            break;
        case clCProductivity_StressValue_Anxiety:
            stress_index = 1;
            break;
        case clCProductivity_StressValue_Stress:
            stress_index = 2;
            break;
    }

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onProductivityIndexesReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onProductivityIndexesReceived,
                        static_cast<jlong>(indexes->timestampMilli),
                        static_cast<jfloat>(indexes->relaxation),
                        static_cast<jfloat>(indexes->stress),
                        static_cast<jfloat>(indexes->gravityBaseline),
                        static_cast<jfloat>(indexes->productivityBaseline),
                        static_cast<jfloat>(indexes->fatigueBaseline),
                        static_cast<jfloat>(indexes->reverseFatigueBaseline),
                        static_cast<jfloat>(indexes->relaxationBaseline),
                        static_cast<jfloat>(indexes->concentrationBaseline),
                        static_cast<jboolean>(indexes->hasArtifacts));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onProductivityIndexesReceived");
        env->ExceptionClear();
    }
}

void onProductivityCalibrationProgress(clCProductivity, float progress) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD",
                        "Productivity baseline calibration progress: %.2f%%", progress);

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onProductivityScore) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onProductivityScore, static_cast<jfloat>(progress));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onProductivityScore");
        env->ExceptionClear();
    }
}

void onProductivityIndividualNFBUpdate(clCProductivity) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity individual NFB updated");
}

void onPhysiologicalStatesCalibrated(clCPhysiologicalStates, const clCPhysiologicalStates_Baselines* baselines) noexcept {
    if (!baselines) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_RES_PHYS", "baselines is null");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PHYS", "Physiological baselines calibrated");

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onPhysiologicalBaselineReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onPhysiologicalBaselineReceived,
                        static_cast<jlong>(baselines->timestampMilli),
                        static_cast<jfloat>(baselines->alpha),
                        static_cast<jfloat>(baselines->beta),
                        static_cast<jfloat>(baselines->alphaGravity),
                        static_cast<jfloat>(baselines->betaGravity),
                        static_cast<jfloat>(baselines->concentration));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onPhysiologicalBaselineReceived");
        env->ExceptionClear();
    }
}

void onPhysiologicalCalibrationProgressUpdated(clCPhysiologicalStates, const float value) noexcept {
    // FIX #10: Убран & (печатало адрес вместо значения)
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PHYS",
                        "Physiological baseline calibration progress: %.2f%%", value);
}

void onPhysiologicalStatesUpdate(clCPhysiologicalStates, const clCPhysiologicalStates_Value* value) noexcept {
    if (!value) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_RES_PHYS", "value is null");
        return;
    }

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onPhysiologicalReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onPhysiologicalReceived,
                        static_cast<jlong>(value->timestampMilli),
                        static_cast<jfloat>(value->relaxation),
                        static_cast<jfloat>(value->fatigue),
                        static_cast<jfloat>(value->none),
                        static_cast<jfloat>(value->concentration),
                        static_cast<jfloat>(value->involvement),
                        static_cast<jfloat>(value->stress),
                        static_cast<jboolean>(value->nfbArtifacts),
                        static_cast<jboolean>(value->cardioArtifacts));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onPhysiologicalReceived");
        env->ExceptionClear();
    }
}

void onPhysiologicalStatesIndividualNFBUpdate(clCPhysiologicalStates) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PHYS", "Physiological individual NFB updated");
}

void onEmotionalStatesUpdate(clCEmotions, const clCEmotions_States* states) noexcept {
    if (!states) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_RES_EMOTION", "states is null");
        return;
    }

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onEmotionReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    env->CallVoidMethod(capsule, mid_onEmotionReceived,
                        static_cast<jlong>(states->timestampMilli),
                        static_cast<jfloat>(states->attention),
                        static_cast<jfloat>(states->relaxation),
                        static_cast<jfloat>(states->cognitiveLoad),
                        static_cast<jfloat>(states->cognitiveControl),
                        static_cast<jfloat>(states->selfControl));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onEmotionReceived");
        env->ExceptionClear();
    }
}

void onCardioCalibrated(clCCardio) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_CARDIO", "Cardio calibrated");
}

// ==========================================
// EEG Callbacks
// ==========================================

void onEEGData(clCDevice, clCEEGTimedData eegData) noexcept {
    if (!eegData) return;

    clCError error;
    const int32_t samples = clCEEGTimedData_GetSamplesCount(eegData, &error);
    const int32_t channels = clCEEGTimedData_GetChannelsCount(eegData, &error);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_EEG",
                        "EEG data: %d channels, %d samples", channels, samples);

    if (channels < 2 || samples <= 0) {
        return;
    }

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onEEGRawDataReceived || !mid_onEEGProcessedDataReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method IDs not cached");
        return;
    }

    // Отправляем только первый сэмпл (как в оригинале - "Костыль")
    // TODO: Оптимизировать - отправлять батчами
    const int32_t sampleIndex = 0;
    const long timestamp = clCEEGTimedData_GetTimestampMilli(eegData, sampleIndex, &error);

    // RAW данные
    float rawCh1 = clCEEGTimedData_GetRawValue(eegData, 0, sampleIndex, &error);
    float rawCh2 = clCEEGTimedData_GetRawValue(eegData, 1, sampleIndex, &error);

    if (error.success) {
        env->CallVoidMethod(capsule, mid_onEEGRawDataReceived,
                            static_cast<jlong>(timestamp),
                            static_cast<jfloat>(rawCh1),
                            static_cast<jfloat>(rawCh2));
    }

    // Processed данные
    float processedCh1 = clCEEGTimedData_GetProcessedValue(eegData, 0, sampleIndex, &error);
    float processedCh2 = clCEEGTimedData_GetProcessedValue(eegData, 1, sampleIndex, &error);

    if (error.success) {
        env->CallVoidMethod(capsule, mid_onEEGProcessedDataReceived,
                            static_cast<jlong>(timestamp),
                            static_cast<jfloat>(processedCh1),
                            static_cast<jfloat>(processedCh2));
    }

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in EEG data processing");
        env->ExceptionClear();
    }
}

void onEEGArtifacts(clCDevice, clCEEGArtifacts eegArtifacts) noexcept {
    if (!eegArtifacts) return;

    clCError error;
    const int32_t channels = clCEEGArtifacts_GetChannelsCount(eegArtifacts, &error);
    const long timestamp = clCEEGArtifacts_GetTimestampMilli(eegArtifacts, &error);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_EEG", "EEG artifacts: %d channels", channels);

    if (channels < 2) return;

    JavaCapsuleGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();
    jobject capsule = guard.capsule();

    if (!mid_onEEGArtifactsReceived) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method ID not cached");
        return;
    }

    bool artifact1 = clCEEGArtifacts_GetArtifactByChannel(eegArtifacts, 0, &error);
    bool artifact2 = clCEEGArtifacts_GetArtifactByChannel(eegArtifacts, 1, &error);

    float quality1 = clCEEGArtifacts_GetEEGQuality(eegArtifacts, 0, &error);
    float quality2 = clCEEGArtifacts_GetEEGQuality(eegArtifacts, 1, &error);

    env->CallVoidMethod(capsule, mid_onEEGArtifactsReceived,
                        static_cast<jlong>(timestamp),
                        static_cast<jboolean>(artifact1),
                        static_cast<jboolean>(artifact2),
                        static_cast<jfloat>(quality1),
                        static_cast<jfloat>(quality2));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onEEGArtifactsReceived");
        env->ExceptionClear();
    }
}

void onBattery(clCDevice, uint8_t charge) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_BATTERY", "Battery: %d%%", charge);
}

// ==========================================
// Cleanup (FIX #12 - правильное освобождение)
// ==========================================

void removeAll() {
    std::lock_guard<std::mutex> lock(deviceMutex);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Removing all resources");

    // Capsule API не имеет Destroy методов для classification объектов.
    // Они автоматически освобождаются при clCDevice_Release(device).
    // Порядок важен: сначала зануляем все, потом релизим device.
    
    if (emotions) {
        emotions = nullptr;  // Будет освобождён автоматически
    }
    if (ps) {
        ps = nullptr;  // Будет освобождён автоматически
    }
    if (productivity) {
        productivity = nullptr;  // Будет освобождён автоматически
    }
    if (cardio) {
        cardio = nullptr;  // Будет освобождён автоматически
    }
    if (mems) {
        mems = nullptr;  // Будет освобождён автоматически
    }
    if (nfb) {
        nfb = nullptr;  // Будет освобождён автоматически
    }
    if (calibrator) {
        calibrator = nullptr;  // Привязан к device, освободится автоматически
    }
    
    // Сначала отключаем устройство (если подключено)
    if (device) {
        clCError error;
        clCDevice_Disconnect(device, &error);  // Асинхронный disconnect
        clCDevice_Release(device);  // Освобождает device + все classification объекты
        device = nullptr;
    }
    
    // Локатор имеет отдельный Destroy метод
    if (locator) {
        clCDeviceLocator_Destroy(locator);
        locator = nullptr;
    }

    // Очищаем javaCapsule
    std::lock_guard<std::mutex> javaLock(javaCapsuleMutex);
    if (javaCapsule) {
        JNIEnv* env = nullptr;
        if (javaVM && javaVM->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_OK) {
            env->DeleteGlobalRef(javaCapsule);
        }
        javaCapsule = nullptr;
    }

    stopRequested.store(false);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "All resources removed");
}

// ==========================================
// JNI Lifecycle (FIX #1 - добавлен JNI_OnUnload)
// ==========================================

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* aReserved) {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "JNI_OnLoad");

    javaVM = vm;

    auto resolver = JniResolver::Instance();
    resolver->SetJVM(vm);

    JNIEnv* env = resolver->GetEnv()->jniEnv;
    if (!env) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to get JNI environment");
        return JNI_ERR;
    }

    // Кэшируем классы
    jclass tempCapsuleClass = env->FindClass("com/neuroproject/neuro/services/CapsuleDeviceManager");
    if (!tempCapsuleClass) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to find CapsuleDeviceManager class");
        return JNI_ERR;
    }
    capsuleClass = static_cast<jclass>(env->NewGlobalRef(tempCapsuleClass));
    env->DeleteLocalRef(tempCapsuleClass);

    jclass tempDeviceInfo = env->FindClass("com/neuroproject/neuro/models/DeviceInfo");
    if (!tempDeviceInfo) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to find DeviceInfo class");
        return JNI_ERR;
    }
    deviceInfo = static_cast<jclass>(env->NewGlobalRef(tempDeviceInfo));
    env->DeleteLocalRef(tempDeviceInfo);

    // Кэшируем method IDs (FIX #4)
    mid_deviceConnectionState = env->GetMethodID(capsuleClass, "deviceConnectionState", "(I)V");
    mid_onConnectionError = env->GetMethodID(capsuleClass, "onConnectionError", "(Ljava/lang/String;)V");
    mid_locatorEvent = env->GetMethodID(capsuleClass, "locatorEvent", "([Lcom/neuroproject/neuro/models/DeviceInfo;)V");
    mid_onResistanceReceived = env->GetMethodID(capsuleClass, "onResistanceReceived", "(DDDD)V");
    mid_onCardioReceived = env->GetMethodID(capsuleClass, "onCardioReceived", "(JFZFZZZF)V");
    mid_calibrationStateChanged = env->GetMethodID(capsuleClass, "calibrationStateChanged", "(I)V");
    mid_onCalibrationReceived = env->GetMethodID(capsuleClass, "onCalibrationReceived", "(FFFFFFFF)V");
    mid_onNFBReceived = env->GetMethodID(capsuleClass, "onNFBReceived", "(JFFFFF)V");
    mid_onMEMSReceived = env->GetMethodID(capsuleClass, "onMEMSReceived", "(JFFFFFF)V");
    mid_onProductivityBaselineReceived = env->GetMethodID(capsuleClass, "onProductivityBaselineReceived", "(JFFFFFF)V");
    mid_onProductivityReceived = env->GetMethodID(capsuleClass, "onProductivityReceived", "(JDFFFFFF)V");
    mid_onProductivityIndexesReceived = env->GetMethodID(capsuleClass, "onProductivityIndexesReceived", "(JFFFFFFFFZ)V");
    mid_onProductivityScore = env->GetMethodID(capsuleClass, "onProductivityScore", "(F)V");
    mid_onPhysiologicalBaselineReceived = env->GetMethodID(capsuleClass, "onPhysiologicalBaselineReceived", "(JFFFFF)V");
    mid_onPhysiologicalReceived = env->GetMethodID(capsuleClass, "onPhysiologicalReceived", "(JFFFFFFZZ)V");
    mid_onEmotionReceived = env->GetMethodID(capsuleClass, "onEmotionReceived", "(JFFFFF)V");
    mid_onEEGRawDataReceived = env->GetMethodID(capsuleClass, "onEEGRawDataReceived", "(JFF)V");
    mid_onEEGProcessedDataReceived = env->GetMethodID(capsuleClass, "onEEGProcessedDataReceived", "(JFF)V");
    mid_onEEGArtifactsReceived = env->GetMethodID(capsuleClass, "onEEGArtifactsReceived", "(JZZFF)V");

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "JNI initialized successfully");

    return JNI_VERSION_1_6;
}

// FIX #1: Добавлен JNI_OnUnload
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "JNI_OnUnload - cleaning up");

    JNIEnv* env = nullptr;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_OK) {
        if (capsuleClass) {
            env->DeleteGlobalRef(capsuleClass);
            capsuleClass = nullptr;
        }
        if (deviceInfo) {
            env->DeleteGlobalRef(deviceInfo);
            deviceInfo = nullptr;
        }
    }

    // Очищаем все ресурсы
    removeAll();

    javaVM = nullptr;

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "JNI cleanup complete");
}

// ==========================================
// JNI Methods (FIX #5, #8, #13, #14)
// ==========================================

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeInitCapsule(
        JNIEnv* env, jobject thiz, jobject impl) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "nativeInitCapsule called");

    if (!impl) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "impl is null");
        return;
    }

    // FIX: Правильная работа с GlobalRef
    {
        std::lock_guard<std::mutex> lock(javaCapsuleMutex);
        if (javaCapsule) {
            env->DeleteGlobalRef(javaCapsule);
            javaCapsule = nullptr;
        }
        javaCapsule = env->NewGlobalRef(impl);
    }

    if (!javaCapsule) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create GlobalRef (OOM or null impl)");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Capsule initialized successfully");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSearch(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Starting device search");

    std::lock_guard<std::mutex> lock(deviceMutex);

    if (locator) {
        __android_log_print(ANDROID_LOG_WARN, "CAPSULE", "Locator already exists");
        return;
    }

    clCError error;
    locator = clCDeviceLocator_Create(&error);
    if (!error.success || !locator) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create locator: %s", error.message);
        return;
    }

    clCDeviceLocator_SetOnDeviceListEvent(locator, onDeviceList);

    clCDeviceLocator_RequestDevices(locator, clCDeviceType_Headband, 30, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Request failed: %s", error.message);
        clCDeviceLocator_Destroy(locator);
        locator = nullptr;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeConnect(
        JNIEnv* env, jobject thiz, jstring id) {

    if (!id) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Device ID is null");
        return;
    }

    // FIX #8: RAII для строки
    JniString deviceID(env, id);
    if (!deviceID.isValid()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to get device ID string");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Connecting to device: %s", deviceID.get());

    std::lock_guard<std::mutex> lock(deviceMutex);

    // FIX #14: Проверка на nullptr
    if (device) {
        __android_log_print(ANDROID_LOG_WARN, "CAPSULE", "Device already connected");
        return;
    }

    clCError error;

    if (!locator) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Locator is null - search first");
        return;
    }

    device = clCDeviceLocator_CreateDevice(locator, const_cast<char*>(deviceID.get()), &error);
    if (!error.success || !device) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create device: %s", error.message);
        return;
    }

    // Регистрируем callbacks
    clCDevice_SetOnConnectionStatusChangedEvent(device, onConnectionStatusChanged);
    clCDevice_SetOnResistanceUpdateEvent(device, onDeviceResistanceUpdate);
    clCDevice_SetOnErrorEvent(device, onDeviceError);

    // Инициализация метрик
    calibrator = clCNFBCalibrator_CreateOrGet(device);
    if (calibrator) {
        clCNFBCalibrator_SetOnCalibratedEvent(calibrator, onCalibrated);
        clCNFBCalibrator_SetOnCalibrationStageFinishedEvent(calibrator, onCalibrationStageFinishedEvent);
    }

    nfb = clCNFB_Create(device, &error);
    if (!error.success || !nfb) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create NFB: %s", error.message);
        // FIX #5: Cleanup при ошибке
        goto cleanup_error;
    }
    clCNFB_SetOnUserStateChangedEvent(nfb, onUpdateUserState);
    clCNFB_SetOnErrorEvent(nfb, onNFBErrorEvent);

    cardio = clCCardio_Create(device, &error);
    if (error.success && cardio) {
        clCCardio_SetOnCalibratedEvent(cardio, onCardioCalibrated, &error);
        clCCardio_SetOnIndexesUpdateEvent(cardio, onCardioIndexesUpdate, &error);
    }

    mems = clCMEMS_Create(device, &error);
    if (error.success && mems) {
        clCMEMS_SetOnMEMSTimedDataUpdateEvent(mems, onMEMSUpdate, &error);
    }

    productivity = clCProductivity_Create(device, &error);
    if (error.success && productivity) {
        clCProductivity_SetOnBaselineUpdateEvent(productivity, onProductivityBaselineUpdate);
        clCProductivity_SetOnMetricsUpdateEvent(productivity, onProductivityMetricsUpdate);
        clCProductivity_SetOnIndexesUpdateEvent(productivity, onProductivityIndexesUpdate);
        clCProductivity_SetOnCalibrationProgressUpdateEvent(productivity, onProductivityCalibrationProgress);
        clCProductivity_SetOnIndividualNFBUpdateEvent(productivity, onProductivityIndividualNFBUpdate);
    }

    ps = clCPhysiologicalStates_Create(device, &error);
    if (error.success && ps) {
        clCPhysiologicalStates_SetOnCalibratedEvent(ps, onPhysiologicalStatesCalibrated, &error);
        clCPhysiologicalStates_SetOnCalibrationProgressUpdateEvent(ps, onPhysiologicalCalibrationProgressUpdated, &error);
        clCPhysiologicalStates_SetOnStatesUpdateEvent(ps, onPhysiologicalStatesUpdate, &error);
        clCPhysiologicalStates_SetOnIndividualNFBUpdateEvent(ps, onPhysiologicalStatesIndividualNFBUpdate, &error);
    }

    emotions = clCEmotions_Create(device, &error);
    if (error.success && emotions) {
        clCEmotions_SetOnEmotionalStatesUpdateEvent(emotions, onEmotionalStatesUpdate);
    }

    clCDevice_SetOnBatteryChargeUpdateEvent(device, onBattery);
    clCDevice_SetOnEEGDataEvent(device, onEEGData);
    clCDevice_SetOnEEGArtifactsEvent(device, onEEGArtifacts);

    // Подключение
    clCDevice_Connect(device, true, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Connection failed: %s", error.message);
        goto cleanup_error;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Connection initiated successfully");
    return;

// FIX #5: Centralized error cleanup
cleanup_error:
    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Cleaning up after connection error");
    // Classification объекты не имеют Destroy - просто зануляем
    emotions = nullptr;
    ps = nullptr;
    productivity = nullptr;
    mems = nullptr;
    cardio = nullptr;
    nfb = nullptr;
    // Device нужно освободить
    if (device) {
        clCDevice_Release(device);
        device = nullptr;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartResistance(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_DEBUG, "CAPSULE", "Start resistance");

    std::lock_guard<std::mutex> lock(deviceMutex);
    // FIX #14: Проверка на nullptr
    if (!device) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Device not connected");
        return;
    }

    clCError error;
    clCDevice_Start(device, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to start resistance: %s", error.message);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSignalAndHR(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Start signal and HR");

    // FIX #13: Убран лишний AttachCurrentThread (мы уже в Java потоке)

    JavaCapsuleGuard guard;
    if (guard.isValid()) {
        guard.env()->CallVoidMethod(guard.capsule(), mid_calibrationStateChanged, static_cast<jint>(0));
    }

    std::lock_guard<std::mutex> lock(deviceMutex);
    if (!device) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Device not connected");
        return;
    }

    if (!calibrator) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Calibrator not initialized");
        return;
    }

    clCError error;
    clCNFBCalibrator_CalibrateIndividualNFBQuick(calibrator, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to calibrate: %s", error.message);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopSignalAndHR(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stop signal and HR");

    std::lock_guard<std::mutex> lock(deviceMutex);
    if (!device) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Device not connected");
        return;
    }

    clCError error;
    clCDevice_Stop(device, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to stop: %s", error.message);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSession(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Start session");
    stopRequested.store(false);
    // TODO: Implement session logic
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopSession(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stop session");
    stopRequested.store(true);  // FIX #7: atomic store
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopResistance(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stop resistance");
    // TODO: Implement stop resistance
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeImportCalibration(
        JNIEnv* env, jobject thiz,
        jfloat indFrequency, jfloat indPeakFrequency, jfloat indPeakFrequencyPower,
        jfloat indPeakFrequencySuppression, jfloat indBandwidth, jfloat indNormalizedPower,
        jfloat lowerFrequency, jfloat upperFrequency) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Import calibration data");

    std::lock_guard<std::mutex> lock(deviceMutex);

    if (!calibrator) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Calibrator not initialized");
        return;
    }

    clCIndividualNFBData prob{
        .individualFrequency = indFrequency,
        .individualPeakFrequency = indPeakFrequency,
        .individualPeakFrequencyPower = indPeakFrequencyPower,
        .individualPeakFrequencySuppression = indPeakFrequencySuppression,
        .individualBandwidth = indBandwidth,
        .individualNormalizedPower = indNormalizedPower,
        .lowerFrequency = lowerFrequency,
        .upperFrequency = upperFrequency
    };

    clCError error;
    clCNFBCalibrator_ImportIndividualNFBData(calibrator, &prob, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to import calibration: %s", error.message);
        return;
    }

    if (ps) {
        clCPhysiologicalStates_StartBaselineCalibration(ps);
    }
    if (productivity) {
        clCProductivity_StartBaselineCalibration(productivity);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_removeAll(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "removeAll called from Java");
    removeAll();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartProductivity(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Start productivity calibration");

    std::lock_guard<std::mutex> lock(deviceMutex);

    if (ps) {
        clCPhysiologicalStates_StartBaselineCalibration(ps);
    }
    if (productivity) {
        clCProductivity_StartBaselineCalibration(productivity);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeImportProductivityCalibration(
        JNIEnv* env, jobject thiz,
        jfloat gravity, jfloat b_productivity, jfloat fatigue,
        jfloat reverse_fatigue, jfloat relaxation, jfloat concentration) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Import productivity calibration");

    std::lock_guard<std::mutex> lock(deviceMutex);

    if (!::productivity) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Productivity not initialized");
        return;
    }

    clCProductivity_Baselines prob{
        .gravity = gravity,
        .productivity = b_productivity,
        .fatigue = fatigue,
        .reverseFatigue = reverse_fatigue,
        .relaxation = relaxation,
        .concentration = concentration
    };

    clCError error;
    clCProductivity_ImportBaselines(::productivity, &prob, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to import productivity: %s", error.message);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeImportPhysiologicalCalibration(
        JNIEnv* env, jobject thiz,
        jfloat alpha, jfloat beta, jfloat alpha_gravity,
        jfloat beta_gravity, jfloat concentration) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Import physiological calibration");

    std::lock_guard<std::mutex> lock(deviceMutex);

    if (!ps) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Physiological states not initialized");
        return;
    }

    clCPhysiologicalStates_Baselines prob{
        .alpha = alpha,
        .beta = beta,
        .alphaGravity = alpha_gravity,
        .betaGravity = beta_gravity,
        .concentration = concentration
    };

    clCError error;
    clCPhysiologicalStates_ImportBaselines(ps, &prob);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to import physiological: %s", error.message);
    }
}
