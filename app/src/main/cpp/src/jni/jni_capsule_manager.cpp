#include <jni.h>

//
// Created by aseatari on 20.09.2024.
//

#include "CCapsuleAPI.h"
#include "helpers.h"
#include <jni.h>

#include <android/log.h>

using namespace std::chrono_literals;

#define CALIBRATOR_READY_STAGE (-1)
// 0.1.2.3 - reserved
#define PHYSIO_INIT_STAGE 4
#define PHYSIO_BASELINE_STAGE 5
#define PHYSIO_SAMPLES_STAGE 6
#define STAGE_STARTED 0
#define STAGE_FINISHED  1

// Глобальные переменные
clCDeviceLocator locator = nullptr;
clCDevice device = nullptr;
clCNFBCalibrator calibrator = nullptr;
clCPhysiologicalStates physioStates = nullptr;
clCNFB nfb = nullptr;
clCCardio cardio = nullptr;
clCMEMS mems = nullptr;
clCProductivity productivity = nullptr;
clCPhysiologicalStates ps = nullptr;
clCEmotions emotions = nullptr;
clCIndividualNFBCalibrationStage stage = clCIndividualNFBCalibrationStage_1;
JavaVM* javaVM = nullptr;

// java объекты
jobject javaCapsule;
jclass capsuleClass;
jclass deviceInfo;

// callbacks

void onConnectionStatusChanged(clCDevice, clCDevice_ConnectionStatus state) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_DEBUG", "Connection State Changed: %d", state);

    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID fun = env->GetMethodID(capsuleClass, "deviceConnectionState", "(I)V");
    if (!fun) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method deviceConnectionState not found");
        return;
    }

    switch (state) {
        case clCDevice_ConnectionState_Connected:
            env->CallVoidMethod(javaCapsule, fun, 1);
            break;
        case clCDevice_ConnectionState_Disconnected:
            env->CallVoidMethod(javaCapsule, fun, 3);
            break;
        case clCDevice_ConnectionState_UnsupportedConnection:
            env->CallVoidMethod(javaCapsule, fun, 4);
            break;
        default:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Unknown State: %d", state);
            break;
    }

    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    }
}

void onDeviceError(clCDevice, const char* error) noexcept {
    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Device error: %s", error);

    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID fun = env->GetMethodID(capsuleClass, "onConnectionError", "(Ljava/lang/String;)V");
    if (!fun) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_DEBUG", "Method onConnectionError not found");
        return;
    }

    jstring errorStr = env->NewStringUTF(error ? error : "Unknown error");
    env->CallVoidMethod(javaCapsule, fun, errorStr);
    env->DeleteLocalRef(errorStr);

    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    }
}

void onDeviceList(clCDeviceLocator, clCDeviceInfoList devices, clCDeviceLocator_FailReason fail_reason) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Locator event");
    if (device != nullptr) {
        return;
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
    clCError error;

    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    int32_t szSensors = clCDeviceInfoList_GetCount(devices, &error);
    auto sensorsArray = env->NewObjectArray(static_cast<jsize>(szSensors),
                                            deviceInfo,
                                            nullptr);
    jmethodID fun = env->GetMethodID(capsuleClass, "locatorEvent", "([Lcom/neuroproject/neuro/models/DeviceInfo;)V");

    if (clCDeviceInfoList_GetCount(devices, &error) != 0) {
        while (szSensors--) {
            env->PushLocalFrame(1);

            clCDeviceInfo deviceDescriptor = clCDeviceInfoList_GetDeviceInfo(devices, szSensors, &error);

            jstring dName = env->NewStringUTF(clCDeviceInfo_GetName(deviceDescriptor));
            jstring dID = env->NewStringUTF(clCDeviceInfo_GetSerial(deviceDescriptor));

            jmethodID infoObject = env->GetMethodID(deviceInfo, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
            jobject scObject = env->NewObject(deviceInfo, infoObject, dName, dID);

            env->SetObjectArrayElement(sensorsArray, szSensors, scObject);
            env->PopLocalFrame(nullptr);
        }
    }
    env->CallVoidMethod(javaCapsule, fun, sensorsArray);
}

void onDeviceResistanceUpdate(clCDevice, clCResistance resistance) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_RESISTANCE", "Resistances: %d", clCResistance_GetCount(resistance));
    double o1 = clCResistance_GetValue(resistance, 0);
    double o2 = clCResistance_GetValue(resistance, 3);
    double t3 = clCResistance_GetValue(resistance, 1);
    double t4 = clCResistance_GetValue(resistance, 2);
    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID resistFun = env->GetMethodID(capsuleClass, "onResistanceReceived", "(DDDD)V");
    env->CallVoidMethod(javaCapsule, resistFun, static_cast<jdouble>(o1),
                        static_cast<jdouble>(o2),
                        static_cast<jdouble>(t3),
                        static_cast<jdouble>(t4));
}

void onCardioIndexesUpdate(clCCardio, const clCCardio_Data* cardioData) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_CARDIO", "HeartRate: %f", cardioData->heartRate);
    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID cardioFun = env->GetMethodID(capsuleClass, "onCardioReceived", "(JFZFZZZF)V");
    env->CallVoidMethod(javaCapsule, cardioFun, static_cast<jlong>(cardioData->timestampMilli),
                        static_cast<jfloat>(cardioData->heartRate),
                        static_cast<jboolean>(cardioData->hasArtifacts),
                        static_cast<jfloat>(cardioData->kaplanIndex),
                        static_cast<jboolean>(cardioData->metricsAvailable),
                        static_cast<jboolean>(cardioData->motionArtifacts),
                        static_cast<jboolean>(cardioData->skinContact),
                        static_cast<jboolean>(cardioData->stressIndex));
}

void onCalibrated(clCNFBCalibrator, const clCIndividualNFBData* data) noexcept {
    if (data == nullptr || data->failReason != clC_IndividualNFBCalibrationFailReason_None) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_INFB", "Calibration failed");
        switch (data->failReason) {
            case clC_IndividualNFBCalibrationFailReason_TooManyArtifacts:
                __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_INFB", "Too many artifacts");
                break;
            case clC_IndividualNFBCalibrationFailReason_PeakIsABorder:
                __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_INFB", "Alpha peak matches one of the alpha range borders");
                break;
            default:
                __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_INFB", "Reason unknown");
        }
    }
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "IAF: %f\nIAPF: %f", data->individualFrequency, data->individualPeakFrequency);
    clCPhysiologicalStates_StartBaselineCalibration(ps);
    clCProductivity_StartBaselineCalibration(productivity);
}

void onCalibrationStageFinishedEvent(clCNFBCalibrator) noexcept {
    clCError error;
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Event");
    switch (stage) {
        case clCIndividualNFBCalibrationStage_1:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 1");
            stage = clCIndividualNFBCalibrationStage_2;
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
            break;
        case clCIndividualNFBCalibrationStage_2:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 2");
            stage = clCIndividualNFBCalibrationStage_3;
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
            break;
        case clCIndividualNFBCalibrationStage_3:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 3");
            stage = clCIndividualNFBCalibrationStage_4;
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
            break;
        case clCIndividualNFBCalibrationStage_4:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 4");
            break;
    }
}

void onUpdateUserState(clCNFB, const clCNFB_UserState* userState) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_NFB", "NFB update state: alpha = %f , beta = %f , theta = %f", userState->alpha, userState->beta, userState->theta);
    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID nfbFun = env->GetMethodID(capsuleClass, "onNFBReceived", "(JFFFFF)V");
    env->CallVoidMethod(javaCapsule, nfbFun, static_cast<jlong>(userState->timestampMilli),
                        static_cast<jfloat>(userState->alpha),
                        static_cast<jfloat>(userState->beta),
                        static_cast<jfloat>(userState->theta),
                        static_cast<jfloat>(userState->delta),
                        static_cast<jfloat>(userState->smr));
}

void onNFBErrorEvent(clCNFB, const char* error) noexcept {
    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_NFB", "NFB error: %s", error);
}

void onMEMSUpdate(clCMEMS, clCMEMSTimedData data) noexcept {
    const int32_t count = clCMEMSTimedData_GetCount(data);
    if (count > 0) {
        const clCPoint3d accelerometer = clCMEMSTimedData_GetAccelerometer(data, 0);
        const clCPoint3d gyroscope = clCMEMSTimedData_GetGyroscope(data, 0);
        const auto timestamp = clCMEMSTimedData_GetTimestampMilli(data, 0);

        JNIEnv* env = nullptr;
        javaVM->AttachCurrentThread(&env, nullptr);
        jmethodID MEMSFun = env->GetMethodID(capsuleClass, "onMEMSReceived", "(JFFFFFF)V");
        env->CallVoidMethod(javaCapsule, MEMSFun, static_cast<jlong>(timestamp),
                            static_cast<jfloat>(accelerometer.x),
                            static_cast<jfloat>(accelerometer.y),
                            static_cast<jfloat>(accelerometer.z),
                            static_cast<jfloat>(gyroscope.x),
                            static_cast<jfloat>(gyroscope.y),
                            static_cast<jfloat>(gyroscope.z));
    }
}

void onProductivityBaselineUpdate(clCProductivity, const clCProductivity_Baselines* baselines) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity baselines update");
}

void onProductivityMetricsUpdate(clCProductivity, const clCProductivity_Metrics* metrics) noexcept {
    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID ProdFun = env->GetMethodID(capsuleClass, "onProductivityReceived", "(JDFFFFFF)V");
    env->CallVoidMethod(javaCapsule, ProdFun, static_cast<jlong>(metrics->timestampMilli),
                        static_cast<jdouble>(metrics->timestampMilli),
                        static_cast<jfloat>(metrics->gravityScore),
                        static_cast<jfloat>(metrics->productivityScore),
                        static_cast<jfloat>(metrics->fatigueScore),
                        static_cast<jfloat>(metrics->reverseFatigueScore),
                        static_cast<jfloat>(metrics->relaxationScore),
                        static_cast<jfloat>(metrics->concentrationScore));
}

void onProductivityIndexesUpdate(clCProductivity, const clCProductivity_Indexes* indexes) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity indexes update");
}

void onProductivityCalibrationProgress(clCProductivity, float progress) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity baseline calibration progress: %f", progress);
}

void onProductivityIndividualNFBUpdate(clCProductivity) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity individual nfb data has been updated");
}

void onPhysiologicalStatesCalibrated(clCPhysiologicalStates, const clCPhysiologicalStates_Baselines* baselines) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PHYS", "Physiological states baselines calibrated");
}

void onPhysiologicalStatesUpdate(clCPhysiologicalStates, const clCPhysiologicalStates_Value* value) noexcept {
    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID resistFun = env->GetMethodID(capsuleClass, "onPhysiologicalReceived", "(JFFFFFFZZ)V");
    env->CallVoidMethod(javaCapsule, resistFun, static_cast<jlong>(value->timestampMilli),
                        static_cast<jfloat>(value->relaxation),
                        static_cast<jfloat>(value->fatigue),
                        static_cast<jfloat>(value->none),
                        static_cast<jfloat>(value->concentration),
                        static_cast<jfloat>(value->involvement),
                        static_cast<jfloat>(value->stress),
                        static_cast<jboolean>(value->nfbArtifacts),
                        static_cast<jboolean>(value->cardioArtifacts));
}

void onPhysiologicalStatesIndividualNFBUpdate(clCPhysiologicalStates) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PHYS", "Physiological states individual nfb data has been updated");
}

void onEmotionalStatesUpdate(clCEmotions, const clCEmotions_States* states) noexcept {
    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID EmFun = env->GetMethodID(capsuleClass, "onEmotionReceived", "(JFFFFF)V");
    env->CallVoidMethod(javaCapsule, EmFun, static_cast<jlong>(states->timestampMilli),
                        static_cast<jfloat>(states->attention),
                        static_cast<jfloat>(states->relaxation),
                        static_cast<jfloat>(states->cognitiveLoad),
                        static_cast<jfloat>(states->cognitiveControl),
                        static_cast<jfloat>(states->selfControl));
}

void onCardioCalibrated(clCCardio) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_CARDIO", "Calibrated");
}

// КОЛБЭКИ ДЛЯ EEG ДАННЫХ
void onEEGData(clCDevice, clCEEGTimedData eegData) noexcept {
    clCError error;
    const int32_t samples = clCEEGTimedData_GetSamplesCount(eegData, &error);
    const int32_t channels = clCEEGTimedData_GetChannelsCount(eegData, &error);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_EEG", "EEG data received: %d channels, %d samples", channels, samples);

    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);

    // Для каждого сэмпла отправляем отдельный вызов
    for (int32_t sampleIndex = 0; sampleIndex < /*Костыль*/1; ++sampleIndex) {
        const long timestamp = clCEEGTimedData_GetTimestampMilli(eegData, sampleIndex, &error);

        // RAW данные (если есть хотя бы 2 канала)
        if (channels >= 2) {
            float rawCh1 = clCEEGTimedData_GetRawValue(eegData, 0, sampleIndex, &error);
            float rawCh2 = clCEEGTimedData_GetRawValue(eegData, 1, sampleIndex, &error);

            jmethodID rawFun = env->GetMethodID(capsuleClass, "onEEGRawDataReceived", "(JFF)V");
            if (rawFun) {
                env->CallVoidMethod(javaCapsule, rawFun,
                                    static_cast<jlong>(timestamp),
                                    static_cast<jfloat>(rawCh1),
                                    static_cast<jfloat>(rawCh2));
            }
        }

        // Processed данные (если есть хотя бы 2 канала)
        if (channels >= 2) {
            float processedCh1 = clCEEGTimedData_GetProcessedValue(eegData, 0, sampleIndex, &error);
            float processedCh2 = clCEEGTimedData_GetProcessedValue(eegData, 1, sampleIndex, &error);

            jmethodID processedFun = env->GetMethodID(capsuleClass, "onEEGProcessedDataReceived", "(JFF)V");
            if (processedFun) {
                env->CallVoidMethod(javaCapsule, processedFun,
                                    static_cast<jlong>(timestamp),
                                    static_cast<jfloat>(processedCh1),
                                    static_cast<jfloat>(processedCh2));
            }
        }
    }

    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    }
}

void onEEGArtifacts(clCDevice, clCEEGArtifacts eegArtifacts) noexcept {
    clCError error;
    const int32_t channels = clCEEGArtifacts_GetChannelsCount(eegArtifacts, &error);
    const long timestamp = clCEEGArtifacts_GetTimestampMilli(eegArtifacts, &error);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_EEG", "EEG artifacts received: %d channels", channels);

    if (channels >= 2) {
        JNIEnv* env = nullptr;
        javaVM->AttachCurrentThread(&env, nullptr);

        // Получаем артефакты и качество для каждого канала
        bool artifact1 = clCEEGArtifacts_GetArtifactByChannel(eegArtifacts, 0, &error);
        bool artifact2 = clCEEGArtifacts_GetArtifactByChannel(eegArtifacts, 1, &error);

        float quality1 = clCEEGArtifacts_GetEEGQuality(eegArtifacts, 0, &error);
        float quality2 = clCEEGArtifacts_GetEEGQuality(eegArtifacts, 1, &error);

        jmethodID artifactsFun = env->GetMethodID(capsuleClass, "onEEGArtifactsReceived", "(JZZFF)V");
        if (artifactsFun) {
            env->CallVoidMethod(javaCapsule, artifactsFun,
                                static_cast<jlong>(timestamp),
                                static_cast<jboolean>(artifact1),
                                static_cast<jboolean>(artifact2),
                                static_cast<jfloat>(quality1),
                                static_cast<jfloat>(quality2));
        }

        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
    }
}

// Убираем PSD колбэк полностью
void removeAll() {
    if (mems) {
        mems = nullptr;
    }
    if (cardio) {
        cardio = nullptr;
    }
    if (nfb) {
        nfb = nullptr;
    }
    if (device) {
        clCDevice_Release(device);
        device = nullptr;
    }
    if (physioStates) {
        physioStates = nullptr;
    }
    if (locator) {
        clCDeviceLocator_Destroy(locator);
        locator = nullptr;
    }
    if (cardio) {
        cardio = nullptr;
    }
    if (productivity) {
        productivity = nullptr;
    }
    if (ps) {
        ps = nullptr;
    }
}

// MAIN LOOP
bool stopRequested = false;
void Loop() {
    while (!stopRequested) {
        std::this_thread::sleep_for(50ms);
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* aReserved) {
    auto resolver = JniResolver::Instance();
    javaVM = vm;
    resolver->SetJVM(vm);
    JNIEnv* env = resolver->GetEnv()->jniEnv;

    capsuleClass = env->FindClass("com/neuroproject/neuro/services/CapsuleDeviceManager");
    capsuleClass = static_cast<jclass>(env->NewGlobalRef(capsuleClass));

    deviceInfo = env->FindClass("com/neuroproject/neuro/models/DeviceInfo");
    deviceInfo = static_cast<jclass>(env->NewGlobalRef(deviceInfo));
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeInitCapsule(
        JNIEnv* env, jobject thiz, jobject impl) {
    javaCapsule = env->NewGlobalRef(impl);
    if (javaCapsule) {
        env->DeleteGlobalRef(javaCapsule);
        javaCapsule = nullptr;
    }

    javaCapsule = env->NewGlobalRef(impl);
    if (!javaCapsule) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create global ref for javaCapsule");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Capsule Initialized successfully");

    jmethodID fun = env->GetMethodID(capsuleClass, "onCapsuleStateChanged", "(I)V");
    env->CallVoidMethod(javaCapsule, fun, 1); //Initialized

    Loop();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSearch(
        JNIEnv* env, jobject thiz) {
    clCError error;
    if (!locator) {
        locator = clCDeviceLocator_Create(&error);
        if (!error.success) {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create locator: %s", error.message);
            return;
        }
        clCDeviceLocator_SetOnDeviceListEvent(locator, onDeviceList);
    }

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Starting device search...");
    clCDeviceLocator_RequestDevices(locator, clCDeviceType_Headband, 30, &error);

    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Search failed: %s", error.message);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeConnect(
        JNIEnv* env, jobject thiz, jstring id) {
    clCError error;
    const char* deviceID = env->GetStringUTFChars(id, nullptr);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Connecting to device: %s", deviceID);

    device = clCDeviceLocator_CreateDevice(locator, deviceID, &error);

    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create device: %s", error.message);
        jmethodID errorFun = env->GetMethodID(capsuleClass, "onConnectionError", "(Ljava/lang/String;)V");
        if (errorFun) {
            env->CallVoidMethod(javaCapsule, errorFun, env->NewStringUTF(error.message));
        }
        env->ReleaseStringUTFChars(id, deviceID);
        return;
    }

    clCDevice_SetOnConnectionStatusChangedEvent(device, onConnectionStatusChanged);
    clCDevice_SetOnResistanceUpdateEvent(device, onDeviceResistanceUpdate);
    clCDevice_SetOnErrorEvent(device, onDeviceError);

    // Инициализация метрик
    calibrator = clCNFBCalibrator_CreateOrGet(device);
    clCNFBCalibrator_SetOnCalibratedEvent(calibrator, onCalibrated);
    clCNFBCalibrator_SetOnCalibrationStageFinishedEvent(calibrator, onCalibrationStageFinishedEvent);

    nfb = clCNFB_Create(device, &error);
    if (nfb == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create NFB object: %s", error.message);
        return;
    }
    clCNFB_SetOnUserStateChangedEvent(nfb, onUpdateUserState);
    clCNFB_SetOnErrorEvent(nfb, onNFBErrorEvent);

    cardio = clCCardio_Create(device, &error);
    if (cardio != nullptr) {
        clCCardio_SetOnCalibratedEvent(cardio, onCardioCalibrated, &error);
        clCCardio_SetOnIndexesUpdateEvent(cardio, onCardioIndexesUpdate, &error);
    }

    mems = clCMEMS_Create(device, &error);
    if (mems != nullptr) {
        clCMEMS_SetOnMEMSTimedDataUpdateEvent(mems, onMEMSUpdate, &error);
    }

    productivity = clCProductivity_Create(device, &error);
    if (productivity != nullptr) {
        clCProductivity_SetOnBaselineUpdateEvent(productivity, onProductivityBaselineUpdate);
        clCProductivity_SetOnMetricsUpdateEvent(productivity, onProductivityMetricsUpdate);
        clCProductivity_SetOnIndexesUpdateEvent(productivity, onProductivityIndexesUpdate);
        clCProductivity_SetOnCalibrationProgressUpdateEvent(productivity, onProductivityCalibrationProgress);
        clCProductivity_SetOnIndividualNFBUpdateEvent(productivity, onProductivityIndividualNFBUpdate);
    }

    ps = clCPhysiologicalStates_Create(device, &error);
    if (ps != nullptr) {
        clCPhysiologicalStates_SetOnCalibratedEvent(ps, onPhysiologicalStatesCalibrated, &error);
        clCPhysiologicalStates_SetOnStatesUpdateEvent(ps, onPhysiologicalStatesUpdate, &error);
        clCPhysiologicalStates_SetOnIndividualNFBUpdateEvent(ps, onPhysiologicalStatesIndividualNFBUpdate, &error);
    }

    emotions = clCEmotions_Create(device, &error);
    if (error.success) {
        clCEmotions_SetOnEmotionalStatesUpdateEvent(emotions, onEmotionalStatesUpdate);
    }

    // Установка колбэков для EEG данных (без PSD)
    clCDevice_SetOnEEGDataEvent(device, onEEGData);
    clCDevice_SetOnEEGArtifactsEvent(device, onEEGArtifacts);

    clCDevice_Connect(device, true, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Connection failed: %s", error.message);
        jmethodID errorFun = env->GetMethodID(capsuleClass, "onConnectionError", "(Ljava/lang/String;)V");
        if (errorFun) {
            env->CallVoidMethod(javaCapsule, errorFun, env->NewStringUTF(error.message));
        }
        clCDevice_Release(device);
        device = nullptr;
    }

    env->ReleaseStringUTFChars(id, deviceID);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartResistance(
        JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_DEBUG, "CAPSULE", "Start Resistance");
    clCError error;
    clCDevice_Start(device, &error);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSignalAndHR(
        JNIEnv* env, jobject thiz) {
    clCError error;
    clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopSignalAndHR(
        JNIEnv* env, jobject thiz) {
    clCError error;
    clCDevice_Stop(device, &error);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSession(
        JNIEnv* env, jobject thiz) {
    // TODO: Implement start session logic
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopSession(
        JNIEnv* env, jobject thiz) {
    stopRequested = true;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopResistance(
        JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stop resist");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_removeAll(JNIEnv* env, jobject thiz) {
    removeAll();
}