#include <jni.h>
// JNI Capsule Manager - Исправленная версия для Clean Architecture
//
// Все колбэки направляются в JniCallbackHandler
//

#include "CCapsuleAPI.h"
#include "helpers.h"

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
static jclass callbackHandlerClass = nullptr;
static jobject callbackHandlerInstance = nullptr;
static jclass deviceInfoClass = nullptr;

// Кэшированные method IDs (для производительности)
static jmethodID mid_deviceConnectionState = nullptr;
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
static jmethodID mid_onBatteryChargeReceived = nullptr;
static jmethodID mid_onDeviceFound = nullptr;

// Мьютексы для thread safety
static std::mutex deviceMutex;
static std::mutex javaCallbackMutex;
static std::atomic<bool> stopRequested{false};

// ==========================================
// RAII helper для JNI строк
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
    JniString(const JniString&) = delete;
    JniString& operator=(const JniString&) = delete;
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
// Safe Java Object Guard
// ==========================================
class JavaCallbackGuard {
public:
    JavaCallbackGuard() : env_(nullptr), callback_(nullptr) {
        std::lock_guard<std::mutex> lock(javaCallbackMutex);
        if (callbackHandlerInstance == nullptr) {
            return;
        }
        if (javaVM->GetEnv((void**)&env_, JNI_VERSION_1_6) != JNI_OK) {
            javaVM->AttachCurrentThread(&env_, nullptr);
            attached_ = true;
        }
        callback_ = callbackHandlerInstance;
    }
    ~JavaCallbackGuard() {
        if (attached_ && env_) {
            javaVM->DetachCurrentThread();
        }
    }
    JNIEnv* env() const { return env_; }
    jobject callback() const { return callback_; }
    bool isValid() const { return env_ != nullptr && callback_ != nullptr; }
    JavaCallbackGuard(const JavaCallbackGuard&) = delete;
    JavaCallbackGuard& operator=(const JavaCallbackGuard&) = delete;
    JavaCallbackGuard(JavaCallbackGuard&&) = delete;
private:
    JNIEnv* env_;
    jobject callback_;
    bool attached_ = false;
};

// ==========================================
// Callbacks (направляются в JniCallbackHandler)
// ==========================================

void onConnectionStatusChanged(clCDevice, clCDevice_ConnectionStatus state) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Connection State Changed: %d", state);

    {
        std::lock_guard<std::mutex> lock(deviceMutex);
        if (!device) {
            __android_log_print(ANDROID_LOG_WARN, "CAPSULE", "Device is null during connection callback, skipping");
            return;
        }
    }

    JavaCallbackGuard guard;
    if (!guard.isValid()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Callback handler is null");
        return;
    }

    JNIEnv* env = guard.env();

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

    env->CallVoidMethod(guard.callback(), mid_deviceConnectionState, stateValue);

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in deviceConnectionState");
        env->ExceptionClear();
    }
}

void onDeviceError(clCDevice, const char* error) noexcept {
    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Device error: %s", error ? error : "null");
    // Ошибка логируется, но не отправляется в Kotlin (можно добавить при необходимости)
}

void onDeviceList(clCDeviceLocator, clCDeviceInfoList devices, clCDeviceLocator_FailReason fail_reason) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Locator event");

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

    JavaCallbackGuard guard;
    if (!guard.isValid()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Callback handler is null");
        return;
    }

    JNIEnv* env = guard.env();

    clCError error;
    int32_t szSensors = clCDeviceInfoList_GetCount(devices, &error);
    if (!error.success || szSensors <= 0) {
        __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "No devices found");
        return;
    }

    // Отправляем каждое устройство отдельно через onDeviceFound
    for (int32_t i = 0; i < szSensors; ++i) {
        clCDeviceInfo deviceDescriptor = clCDeviceInfoList_GetDeviceInfo(devices, i, &error);
        if (!error.success || !deviceDescriptor) {
            continue;
        }

        const char* name = clCDeviceInfo_GetName(deviceDescriptor);
        const char* serial = clCDeviceInfo_GetSerial(deviceDescriptor);

        if (name && serial) {
            jstring jName = env->NewStringUTF(name);
            jstring jSerial = env->NewStringUTF(serial);

            env->CallVoidMethod(guard.callback(), mid_onDeviceFound, jName, jSerial);

            env->DeleteLocalRef(jName);
            env->DeleteLocalRef(jSerial);
        }

        if (env->ExceptionCheck()) {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onDeviceFound");
            env->ExceptionClear();
        }
    }
}

void onDeviceResistanceUpdate(clCDevice, clCResistance resistance) noexcept {
    int32_t count = clCResistance_GetCount(resistance);
    if (count < 4) {
        __android_log_print(ANDROID_LOG_WARN, "CAPSULE", "Not enough resistance channels");
        return;
    }

    double o1 = clCResistance_GetValue(resistance, 0);
    double o2 = clCResistance_GetValue(resistance, 3);
    double t3 = clCResistance_GetValue(resistance, 1);
    double t4 = clCResistance_GetValue(resistance, 2);

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onResistanceReceived,
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
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "cardioData is null");
        return;
    }

    if (cardioData->heartRate + cardioData->kaplanIndex == 0) {
        return;
    }

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onCardioReceived,
                        static_cast<jlong>(cardioData->timestampMilli),
                        static_cast<jfloat>(cardioData->heartRate),
                        static_cast<jboolean>(cardioData->hasArtifacts),
                        static_cast<jfloat>(cardioData->kaplanIndex),
                        static_cast<jboolean>(cardioData->metricsAvailable),
                        static_cast<jboolean>(cardioData->motionArtifacts),
                        static_cast<jboolean>(cardioData->skinContact),
                        static_cast<jboolean>(cardioData->stressIndex != 0.0f));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onCardioReceived");
        env->ExceptionClear();
    }
}

void onCalibrated(clCNFBCalibrator, const clCIndividualNFBData* data) noexcept {
    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    if (data == nullptr || data->failReason != clC_IndividualNFBCalibrationFailReason_None) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Calibration failed");
        env->CallVoidMethod(guard.callback(), mid_calibrationStateChanged, static_cast<jint>(6));
        return;
    }

    env->CallVoidMethod(guard.callback(), mid_onCalibrationReceived,
                        static_cast<jfloat>(data->individualFrequency),
                        static_cast<jfloat>(data->individualPeakFrequency),
                        static_cast<jfloat>(data->individualPeakFrequencyPower),
                        static_cast<jfloat>(data->individualPeakFrequencySuppression),
                        static_cast<jfloat>(data->individualBandwidth),
                        static_cast<jfloat>(data->individualNormalizedPower),
                        static_cast<jfloat>(data->lowerFrequency),
                        static_cast<jfloat>(data->upperFrequency));

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "IAF: %f, IAPF: %f",
                        data->individualFrequency, data->individualPeakFrequency);

    env->CallVoidMethod(guard.callback(), mid_calibrationStateChanged, static_cast<jint>(4));

    {
        std::lock_guard<std::mutex> lock(deviceMutex);
        if (ps) {
            clCPhysiologicalStates_StartBaselineCalibration(ps);
        }
        if (productivity) {
            clCProductivity_StartBaselineCalibration(productivity);
        }
    }

    env->CallVoidMethod(guard.callback(), mid_calibrationStateChanged, static_cast<jint>(5));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onCalibrated");
        env->ExceptionClear();
    }
}

void onCalibrationStageFinishedEvent(clCNFBCalibrator) noexcept {
    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    clCError error;

    std::lock_guard<std::mutex> lock(deviceMutex);

    if (!calibrator) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Calibrator is null");
        return;
    }

    switch (stage) {
        case clCIndividualNFBCalibrationStage_1:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stage 1 finished");
            stage = clCIndividualNFBCalibrationStage_2;
            env->CallVoidMethod(guard.callback(), mid_calibrationStateChanged, static_cast<jint>(1));
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
            break;
        case clCIndividualNFBCalibrationStage_2:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stage 2 finished");
            stage = clCIndividualNFBCalibrationStage_3;
            env->CallVoidMethod(guard.callback(), mid_calibrationStateChanged, static_cast<jint>(2));
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
            break;
        case clCIndividualNFBCalibrationStage_3:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stage 3 finished");
            stage = clCIndividualNFBCalibrationStage_4;
            env->CallVoidMethod(guard.callback(), mid_calibrationStateChanged, static_cast<jint>(3));
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
            break;
        case clCIndividualNFBCalibrationStage_4:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stage 4 finished - calibration complete");
            break;
    }

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in calibration stage");
        env->ExceptionClear();
    }
}

void onUpdateUserState(clCNFB, const clCNFB_UserState* userState) noexcept {
    if (!userState) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "userState is null");
        return;
    }

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onNFBReceived,
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
    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "NFB error: %s", error ? error : "null");
}

void onMEMSUpdate(clCMEMS, clCMEMSTimedData data) noexcept {
    if (!data) return;

    const int32_t count = clCMEMSTimedData_GetCount(data);
    if (count <= 0) return;

    const clCPoint3d accelerometer = clCMEMSTimedData_GetAccelerometer(data, 0);
    const clCPoint3d gyroscope = clCMEMSTimedData_GetGyroscope(data, 0);
    const auto timestamp = clCMEMSTimedData_GetTimestampMilli(data, 0);

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onMEMSReceived,
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
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "baselines is null");
        return;
    }
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Productivity baseline updated: gravity: %f", baselines->gravity);

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onProductivityBaselineReceived,
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
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "metrics is null");
        return;
    }

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onProductivityReceived,
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
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "indexes is null");
        return;
    }

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onProductivityIndexesReceived,
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
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Productivity calibration progress: %.2f%%", progress);

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onProductivityScore, static_cast<jfloat>(progress));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onProductivityScore");
        env->ExceptionClear();
    }
}

void onProductivityIndividualNFBUpdate(clCProductivity) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Productivity individual NFB updated");
}

void onPhysiologicalStatesCalibrated(clCPhysiologicalStates, const clCPhysiologicalStates_Baselines* baselines) noexcept {
    if (!baselines) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "baselines is null");
        return;
    }
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Physiological baseline updated");

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onPhysiologicalBaselineReceived,
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
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Physiological calibration progress: %.2f%%", value);
}

void onPhysiologicalStatesUpdate(clCPhysiologicalStates, const clCPhysiologicalStates_Value* value) noexcept {
    if (!value) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "value is null");
        return;
    }

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onPhysiologicalReceived,
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
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Physiological individual NFB updated");
}

void onEmotionalStatesUpdate(clCEmotions, const clCEmotions_States* states) noexcept {
    if (!states) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "states is null");
        return;
    }

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onEmotionReceived,
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
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Cardio calibrated");
}

// ==========================================
// EEG Callbacks
// ==========================================

void onEEGData(clCDevice, clCEEGTimedData eegData) noexcept {
    if (!eegData) return;

    clCError error;
    const int32_t samples = clCEEGTimedData_GetSamplesCount(eegData, &error);
    const int32_t channels = clCEEGTimedData_GetChannelsCount(eegData, &error);

    if (channels < 2 || samples <= 0) {
        return;
    }

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    const int32_t sampleIndex = 0;
    const long timestamp = clCEEGTimedData_GetTimestampMilli(eegData, sampleIndex, &error);

    // RAW данные
    float rawCh1 = clCEEGTimedData_GetRawValue(eegData, 0, sampleIndex, &error);
    float rawCh2 = clCEEGTimedData_GetRawValue(eegData, 1, sampleIndex, &error);

    if (error.success) {
        env->CallVoidMethod(guard.callback(), mid_onEEGRawDataReceived,
                            static_cast<jlong>(timestamp),
                            static_cast<jfloat>(rawCh1),
                            static_cast<jfloat>(rawCh2));
    }

    // Processed данные
    float processedCh1 = clCEEGTimedData_GetProcessedValue(eegData, 0, sampleIndex, &error);
    float processedCh2 = clCEEGTimedData_GetProcessedValue(eegData, 1, sampleIndex, &error);

    if (error.success) {
        env->CallVoidMethod(guard.callback(), mid_onEEGProcessedDataReceived,
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

    if (channels < 2) return;

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    bool artifact1 = clCEEGArtifacts_GetArtifactByChannel(eegArtifacts, 0, &error);
    bool artifact2 = clCEEGArtifacts_GetArtifactByChannel(eegArtifacts, 1, &error);

    float quality1 = clCEEGArtifacts_GetEEGQuality(eegArtifacts, 0, &error);
    float quality2 = clCEEGArtifacts_GetEEGQuality(eegArtifacts, 1, &error);

    env->CallVoidMethod(guard.callback(), mid_onEEGArtifactsReceived,
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
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Battery: %d%%", charge);

    JavaCallbackGuard guard;
    if (!guard.isValid()) return;

    JNIEnv* env = guard.env();

    env->CallVoidMethod(guard.callback(), mid_onBatteryChargeReceived, static_cast<jfloat>(charge));

    if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Exception in onBatteryChargeReceived");
        env->ExceptionClear();
    }
}

// ==========================================
// Cleanup
// ==========================================

void removeAll() {
    std::lock_guard<std::mutex> lock(deviceMutex);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Removing all resources");

    emotions = nullptr;
    ps = nullptr;
    productivity = nullptr;
    mems = nullptr;
    cardio = nullptr;
    nfb = nullptr;
    calibrator = nullptr;

    if (device) {
        clCError error;
        clCDevice_Disconnect(device, &error);
        clCDevice_Release(device);
        device = nullptr;
    }

    if (locator) {
        clCDeviceLocator_Destroy(locator);
        locator = nullptr;
    }

    std::lock_guard<std::mutex> javaLock(javaCallbackMutex);
    if (callbackHandlerInstance) {
        JNIEnv* env = nullptr;
        if (javaVM && javaVM->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_OK) {
            env->DeleteGlobalRef(callbackHandlerInstance);
        }
        callbackHandlerInstance = nullptr;
    }

    stopRequested.store(false);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "All resources removed");
}

// ==========================================
// JNI Lifecycle
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

    // Находим класс JniCallbackHandler
    jclass tempCallbackHandlerClass = env->FindClass("com/neuroproject/neuro/jni/JniCallbackHandler");
    if (!tempCallbackHandlerClass) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to find JniCallbackHandler class");
        return JNI_ERR;
    }
    callbackHandlerClass = static_cast<jclass>(env->NewGlobalRef(tempCallbackHandlerClass));
    env->DeleteLocalRef(tempCallbackHandlerClass);

    // Получаем instance JniCallbackHandler через getInstance()
    jmethodID getInstance = env->GetStaticMethodID(callbackHandlerClass, "getInstance", "()Lcom/neuroproject/neuro/jni/JniCallbackHandler;");
    if (!getInstance) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to find getInstance method");
        return JNI_ERR;
    }

    jobject instance = env->CallStaticObjectMethod(callbackHandlerClass, getInstance);
    if (!instance) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to get JniCallbackHandler instance");
        return JNI_ERR;
    }
    callbackHandlerInstance = env->NewGlobalRef(instance);
    env->DeleteLocalRef(instance);

    // Находим класс DeviceInfo (для совместимости, может не использоваться)
    jclass tempDeviceInfo = env->FindClass("com/neuroproject/neuro/domain/model/DeviceInfo");
    if (tempDeviceInfo) {
        deviceInfoClass = static_cast<jclass>(env->NewGlobalRef(tempDeviceInfo));
        env->DeleteLocalRef(tempDeviceInfo);
    }

    // Кэшируем method IDs
    mid_deviceConnectionState = env->GetMethodID(callbackHandlerClass, "deviceConnectionState", "(I)V");
    mid_onResistanceReceived = env->GetMethodID(callbackHandlerClass, "onResistanceReceived", "(DDDD)V");
    mid_onCardioReceived = env->GetMethodID(callbackHandlerClass, "onCardioReceived", "(JFZFZZZF)V");
    mid_calibrationStateChanged = env->GetMethodID(callbackHandlerClass, "onCalibrationStateReceived", "(I)V");
    mid_onCalibrationReceived = env->GetMethodID(callbackHandlerClass, "onCalibrationReceived", "(FFFFFFFF)V");
    mid_onNFBReceived = env->GetMethodID(callbackHandlerClass, "onNFBReceived", "(JFFFFF)V");
    mid_onMEMSReceived = env->GetMethodID(callbackHandlerClass, "onMEMSReceived", "(JFFFFFF)V");
    mid_onProductivityBaselineReceived = env->GetMethodID(callbackHandlerClass, "onProductivityBaselineReceived", "(JFFFFFF)V");
    mid_onProductivityReceived = env->GetMethodID(callbackHandlerClass, "onProductivityReceived", "(JDFFFFFF)V");
    mid_onProductivityIndexesReceived = env->GetMethodID(callbackHandlerClass, "onProductivityIndexesReceived", "(JFFFFFFFFZ)V");
    mid_onProductivityScore = env->GetMethodID(callbackHandlerClass, "onProductivityScore", "(F)V");
    mid_onPhysiologicalBaselineReceived = env->GetMethodID(callbackHandlerClass, "onPhysiologicalBaselineReceived", "(JFFFFF)V");
    mid_onPhysiologicalReceived = env->GetMethodID(callbackHandlerClass, "onPhysiologicalReceived", "(JFFFFFFZZ)V");
    mid_onEmotionReceived = env->GetMethodID(callbackHandlerClass, "onEmotionReceived", "(JFFFFF)V");
    mid_onEEGRawDataReceived = env->GetMethodID(callbackHandlerClass, "onEEGRawDataReceived", "(JFF)V");
    mid_onEEGProcessedDataReceived = env->GetMethodID(callbackHandlerClass, "onEEGProcessedDataReceived", "(JFF)V");
    mid_onEEGArtifactsReceived = env->GetMethodID(callbackHandlerClass, "onEEGArtifactsReceived", "(JZZFF)V");
    mid_onBatteryChargeReceived = env->GetMethodID(callbackHandlerClass, "onBatteryChargeReceived", "(F)V");
    mid_onDeviceFound = env->GetMethodID(callbackHandlerClass, "onDeviceFound", "(Ljava/lang/String;Ljava/lang/String;)V");

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "JNI initialized successfully");

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "JNI_OnUnload - cleaning up");

    JNIEnv* env = nullptr;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_OK) {
        if (callbackHandlerClass) {
            env->DeleteGlobalRef(callbackHandlerClass);
            callbackHandlerClass = nullptr;
        }
        if (callbackHandlerInstance) {
            env->DeleteGlobalRef(callbackHandlerInstance);
            callbackHandlerInstance = nullptr;
        }
        if (deviceInfoClass) {
            env->DeleteGlobalRef(deviceInfoClass);
            deviceInfoClass = nullptr;
        }
    }

    removeAll();

    javaVM = nullptr;

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "JNI cleanup complete");
}

// ==========================================
// JNI Methods (вызываются из Kotlin)
// ==========================================

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeInitCapsule(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "nativeInitCapsule called");
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

    JniString deviceID(env, id);
    if (!deviceID.isValid()) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to get device ID string");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Connecting to device: %s", deviceID.get());

    std::lock_guard<std::mutex> lock(deviceMutex);

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

    clCDevice_SetOnConnectionStatusChangedEvent(device, onConnectionStatusChanged);
    clCDevice_SetOnResistanceUpdateEvent(device, onDeviceResistanceUpdate);
    clCDevice_SetOnErrorEvent(device, onDeviceError);

    calibrator = clCNFBCalibrator_CreateOrGet(device);
    if (calibrator) {
        clCNFBCalibrator_SetOnCalibratedEvent(calibrator, onCalibrated);
        clCNFBCalibrator_SetOnCalibrationStageFinishedEvent(calibrator, onCalibrationStageFinishedEvent);
    }

    nfb = clCNFB_Create(device, &error);
    if (!error.success || !nfb) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create NFB: %s", error.message);
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

    clCDevice_Connect(device, true, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Connection failed: %s", error.message);
        goto cleanup_error;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Connection initiated successfully");
    return;

    cleanup_error:
    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Cleaning up after connection error");
    emotions = nullptr;
    ps = nullptr;
    productivity = nullptr;
    mems = nullptr;
    cardio = nullptr;
    nfb = nullptr;
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

    JavaCallbackGuard guard;
    if (guard.isValid()) {
        guard.env()->CallVoidMethod(guard.callback(), mid_calibrationStateChanged, static_cast<jint>(0));
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
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopSession(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stop session");
    stopRequested.store(true);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopResistance(
        JNIEnv* env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stop resistance");
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

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeDisconnect(
        JNIEnv *env, jobject thiz) {

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "nativeDisconnect called");

    std::lock_guard<std::mutex> lock(deviceMutex);
    if (!device) {
        __android_log_print(ANDROID_LOG_WARN, "CAPSULE", "Device is null, nothing to disconnect");
        return;
    }

    clCError error;
    clCDevice_Disconnect(device, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Disconnect failed: %s", error.message);
    }
}