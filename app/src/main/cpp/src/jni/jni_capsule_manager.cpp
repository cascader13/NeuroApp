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
clCDeviceLocator locator = nullptr; // объект для поиска устройства
clCDevice device = nullptr; // объект устройства(хранит всю информацию о нём и к нему подключаются все коллбэки
clCNFBCalibrator calibrator = nullptr; // объект для калибровки и получения доступа к некоторым метрикам
clCPhysiologicalStates physioStates = nullptr; // объект для получения физиологических метрик
clCNFB nfb = nullptr; //калибровка для получения уникальной метрики(вроде)
clCCardio cardio = nullptr; // объект для получения кардио метрик
clCMEMS mems = nullptr; // объект для получения значений акселерометра и гироскопа
clCProductivity productivity = nullptr; // Объект для получения значений продуктивности
clCPhysiologicalStates ps = nullptr; // Объект для получения физиологический значений
clCEmotions emotions = nullptr; // Объект для получения эмоциональных значений
clCIndividualNFBCalibrationStage stage = clCIndividualNFBCalibrationStage_1; // состояние калибровки
//также надо добавить некоторые другие метрики(как RAW и RAW filthered)
JavaVM* javaVM = nullptr;


// java объекты
jobject javaCapsule;
jclass capsuleClass;
jclass deviceInfo;

// callbacks


// При изменении состояния подключения
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


// При ошибке соединения или работы с устройством
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

// Получение списка устройств
void onDeviceList(clCDeviceLocator, clCDeviceInfoList devices, clCDeviceLocator_FailReason fail_reason) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Locator event");
    // device connected
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

    if (clCDeviceInfoList_GetCount(devices, &error) == 0) {
        // clCDeviceLocator_RequestDevices(locator, 10);
    } else {
        while (szSensors--)
        {
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

// При получение новых значений сопротивления
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

// при получение данных сердцебиения
void onCardioIndexesUpdate(clCCardio, const clCCardio_Data* cardioData) noexcept{
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_CARDIO", "HeartRate: %f", cardioData->heartRate);
    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID cardioFun = env ->GetMethodID(capsuleClass, "onCardioReceived", "(JFZFZZZF)V");
    env->CallVoidMethod(javaCapsule, cardioFun, static_cast<jlong>(cardioData->timestampMilli),
                                                    static_cast<jfloat>(cardioData->heartRate),
                                                    static_cast<jboolean>(cardioData->hasArtifacts),
                                                    static_cast<jfloat>(cardioData->kaplanIndex),
                                                    static_cast<jboolean>(cardioData->metricsAvailable),
                                                    static_cast<jboolean>(cardioData->motionArtifacts),
                                                    static_cast<jboolean>(cardioData->skinContact),
                                                    static_cast<jboolean>(cardioData->stressIndex));
}

// при окончании калибровки устройства
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
    clCPhysiologicalStates_StartBaselineCalibration(ps); // Первым делом заканчиваем основную калибровку
    clCProductivity_StartBaselineCalibration(productivity);
}

// При окончании одного из состояний калибровки(кроме 4-го, так как при окончании его вызывается onCalibrated)
void onCalibrationStageFinishedEvent(clCNFBCalibrator) noexcept {
    clCError error;
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Event");
    switch (stage){
        case clCIndividualNFBCalibrationStage_1:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 1");
            stage = clCIndividualNFBCalibrationStage_2;
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage,&error);
            break;
        case clCIndividualNFBCalibrationStage_2:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 2");
            stage = clCIndividualNFBCalibrationStage_3;
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage,&error);
            break;
        case clCIndividualNFBCalibrationStage_3:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 3");
            stage = clCIndividualNFBCalibrationStage_4;
            clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage,&error);
            break;
        case clCIndividualNFBCalibrationStage_4:
            __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_INFB", "Stage 4");
        //Добавить связь для того, чтобы знать какое у нас состояние калибровки


    }
}

// Обновление частот
void onUpdateUserState(clCNFB, const clCNFB_UserState* userState) noexcept {
    // Getting NFB user data
    // if artifacts or weak resistance on the electrodes are observed,
    // the data will not be changed
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

// Ошибка при обновлении частот
void onNFBErrorEvent(clCNFB, const char* error) noexcept {
    __android_log_print(ANDROID_LOG_ERROR, "CAPSULE_NFB", "NFB error: %s", error);
}

// Обновление данных акселерометра и гироскопа
void onMEMSUpdate(clCMEMS, clCMEMSTimedData data) noexcept {
    const int32_t count = clCMEMSTimedData_GetCount(data);
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_MEMS", "MEMS update: showing 1 of %d values", count);
    const clCPoint3d accelerometer = clCMEMSTimedData_GetAccelerometer(data, 0);
    const clCPoint3d gyroscope = clCMEMSTimedData_GetGyroscope(data, 0);
    const auto timestamp = clCMEMSTimedData_GetTimestampMilli(data, 0);
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_MEMS", "\taccelerometer: X:%f, Y:%f, Z:%f", accelerometer.x, accelerometer.y, accelerometer.z);
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_MEMS", "\tgyroscope: X:%f, Y:%f, Z:%f", gyroscope.x, gyroscope.y, gyroscope.z);
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_MEMS", "\ttime^ %s", std::to_string(timestamp).c_str());
    JNIEnv* env = nullptr;
    
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID MEMSFun = env->GetMethodID(capsuleClass, "onMEMSReceived", "(JFFFFFF)V");
    env->CallVoidMethod(javaCapsule, MEMSFun, static_cast<jlong>(clCMEMSTimedData_GetTimestampMilli(
            data, 0)),
                        static_cast<jfloat>(accelerometer.x),
                        static_cast<jfloat>(accelerometer.y),
                        static_cast<jfloat>(accelerometer.z),
                        static_cast<jfloat>(gyroscope.x),
                        static_cast<jfloat>(gyroscope.y),
                        static_cast<jfloat>(gyroscope.z));
}

// После калибровки метрик продуктивности
void onProductivityBaselineUpdate(clCProductivity, const clCProductivity_Baselines* baselines) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity baselines update:\n\tTimestamp: %ld\n\tGravity: %f\n\tProductivity: %f\n\tFatigue: %f\n\tReverse Fatigue: %f\n\tRelaxation: %f\n\tConcentration: %f", baselines->timestampMilli, baselines->gravity,baselines->productivity, baselines->fatigue, baselines->reverseFatigue, baselines->relaxation, baselines->concentration);
}

// Обновление значений метрик продуктивности
void onProductivityMetricsUpdate(clCProductivity, const clCProductivity_Metrics* metrics) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity score update: %f", metrics->currentValue);
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity baselines update:\n\tTimestamp: %ld\n\tGravity: %f\n\tProductivity: %f\n\tFatigue: %f\n\tReverse Fatigue: %f\n\tRelaxation: %f\n\tConcentration: %f", metrics->timestampMilli, metrics->gravityScore,metrics->productivityScore, metrics->fatigueScore, metrics->reverseFatigueScore, metrics->relaxationScore, metrics->concentrationScore);
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
                                                            static_cast<jfloat>(metrics->concentrationScore)
                                                            );
}
// Обновление значений индексов продуктивности
void onProductivityIndexesUpdate(clCProductivity, const clCProductivity_Indexes* indexes) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity indexes update:\n\tStress: %u\n\tRelaxation: %u", indexes->stress, indexes->relaxation);
}
// Отображение прогресса калибровки
void onProductivityCalibrationProgress(clCProductivity, float progress) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity baseline calibration progress: %f", progress);
}

// При получение индивидуальных значений
void onProductivityIndividualNFBUpdate(clCProductivity) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PROD", "Productivity individual nfb data has been updated");
}
// При окончании калибровки
void onPhysiologicalStatesCalibrated(clCPhysiologicalStates, const clCPhysiologicalStates_Baselines* baselines) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PHYS",
                        "Physiological states baselines calibrated:\n\tTimestamp: %ld\n\tAlpha: %f\n\tBeta: %f\n\tConcentration: %f\n\tAlpha Gravity: %f\n\tBeta Gravity: %f\n",
                        baselines->timestampMilli, baselines->alpha, baselines->beta,
                        baselines->concentration, baselines->alphaGravity, baselines->betaGravity);
}
// при обновлении данных
void onPhysiologicalStatesUpdate(clCPhysiologicalStates, const clCPhysiologicalStates_Value* value) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PHYS", "Physiological states update:\n\"\\tTimestamp: %ld\n\tRelaxation: %f\n\tFatigue: %f\n\tNone: %f\n\tConcentration: %f\n\tInvolvement: %f\n\tStress: %f\n\tNfb Artifacts: %b\n\tCardio Artifacts: %b",value->timestampMilli, value->relaxation, value->fatigue, value->none, value->concentration, value->involvement, value->stress, value->nfbArtifacts, value->cardioArtifacts );
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
                        static_cast<jboolean >(value->cardioArtifacts));
}
// При получение индивидуальных значений
void onPhysiologicalStatesIndividualNFBUpdate(clCPhysiologicalStates) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_PHYS", "Physiological states individual nfb data has been updated");
}
// при обновлении данных
void onEmotionalStatesUpdate(clCEmotions, const clCEmotions_States* states) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_EMOT",
                        "Emotional states update:\n\tAttention: %f\n\tRelaxation: %f\n\tCognitive Load: %f\n\tCognitive Control: %f\n\tSelfControl: %f",
                        states->attention, states->relaxation, states->cognitiveLoad,
                        states->cognitiveControl, states->selfControl);
    JNIEnv *env = nullptr;
    javaVM->AttachCurrentThread(&env, nullptr);
    jmethodID EmFun = env->GetMethodID(capsuleClass, "onEmotionReceived", "(JFFFFF)V");
    env->CallVoidMethod(javaCapsule, EmFun, static_cast<jlong>(states->timestampMilli),
                        static_cast<jfloat>(states->attention),
                        static_cast<jfloat>(states->relaxation),
                        static_cast<jfloat>(states->cognitiveLoad),
                        static_cast<jfloat>(states->cognitiveControl),
                        static_cast<jfloat>(states->selfControl));
}
// При окончании калибровки
void onCardioCalibrated(clCCardio) noexcept{
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES_CARDIO", "Calibrated");
}


void removeAll(){
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
    if(physioStates){
        physioStates = nullptr;
    }
    if(locator){
        clCDeviceLocator_Destroy(locator);
        locator = nullptr;
    }
    if(cardio){
        cardio = nullptr;
    }
    if(productivity){
        productivity = nullptr;
    }
    if(ps){
        ps = nullptr;
    }

}


// **callbacks**


// MAIN LOOP
bool stopRequested = false;
void Loop() {
    while (!stopRequested) {
        std::this_thread::sleep_for(50ms);
    }
}

////////////////////////
//загрузка JNI(выполняется первее всех. Есть ещё UnLoad, как раз таки чистка и удаление)
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *aReserved)
{
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
        JNIEnv *env, jobject thiz, jobject impl) {
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

    jmethodID  fun = env->GetMethodID(capsuleClass, "onCapsuleStateChanged", "(I)V");
    env->CallVoidMethod(javaCapsule, fun, 1); //Initialized


    Loop();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSearch(
        JNIEnv *env, jobject thiz) {
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
    //Надо будет переделать(добавть повторный поиск устройства)

    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Search failed: %s", error.message);

    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeConnect(
        JNIEnv *env, jobject thiz, jstring id) {
    clCError error;
    const char* deviceID = env->GetStringUTFChars(id, nullptr);

    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Connecting to device: %s", deviceID);

    // Создаем устройство
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

    // колбэки(не все)
    clCDevice_SetOnConnectionStatusChangedEvent(device, onConnectionStatusChanged);
    clCDevice_SetOnResistanceUpdateEvent(device, onDeviceResistanceUpdate);
    clCDevice_SetOnErrorEvent(device, onDeviceError);

    // Здесь также надо инициализировать все объекты метрик, которые будут использоваться

    calibrator = clCNFBCalibrator_CreateOrGet(device);
    clCNFBCalibrator_SetOnCalibratedEvent(calibrator, onCalibrated);
    clCNFBCalibrator_SetOnCalibrationStageFinishedEvent(calibrator, onCalibrationStageFinishedEvent);

    nfb = clCNFB_Create(device, &error);
    if (nfb == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create NFB object: %s", error.message);
        stopRequested = true;
        return;
    }
    clCNFB_SetOnUserStateChangedEvent(nfb, onUpdateUserState);
    clCNFB_SetOnErrorEvent(nfb, onNFBErrorEvent);

    cardio = clCCardio_Create(device, &error);
    clCCardio_SetOnCalibratedEvent(cardio, onCardioCalibrated, &error);
    if (cardio == nullptr) {
        if (error.code == clCError_ModuleIsNotSupported) {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create cardio object: %s", error.message);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Unexpected error in creation cardio object: %s", error.message);
            stopRequested = true;
            return;
        }
    }
    if (cardio != nullptr) {
        clCCardio_SetOnIndexesUpdateEvent(cardio, onCardioIndexesUpdate, &error);
    }

    mems = clCMEMS_Create(device, &error);
    if (mems == nullptr) {
        if (error.code == clCError_ModuleIsNotSupported) {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create mems object: %s", error.message);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Unexpected error in creation mems object: %s", error.message);
            stopRequested = true;
            return;
        }
    }
    if (mems != nullptr) {
        clCMEMS_SetOnMEMSTimedDataUpdateEvent(mems, onMEMSUpdate, &error);
    }

    productivity = clCProductivity_Create(device, &error);
    if (productivity == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create productivity object: %s", error.message);
        stopRequested = true;
        return;
    }

    clCProductivity_SetOnBaselineUpdateEvent(productivity, onProductivityBaselineUpdate);
    clCProductivity_SetOnMetricsUpdateEvent(productivity, onProductivityMetricsUpdate);
    clCProductivity_SetOnIndexesUpdateEvent(productivity, onProductivityIndexesUpdate);
    clCProductivity_SetOnCalibrationProgressUpdateEvent(productivity, onProductivityCalibrationProgress);
    clCProductivity_SetOnIndividualNFBUpdateEvent(productivity, onProductivityIndividualNFBUpdate);

    ps = clCPhysiologicalStates_Create(device, &error);
    if (ps == nullptr) {
        if (error.code == clCError_ModuleIsNotSupported) {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create ps object: %s", error.message);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Unexpected error in creation ps object: %s", error.message);
            stopRequested = true;
            return;
        }
    }
    if (ps != nullptr) {
        clCPhysiologicalStates_SetOnCalibratedEvent(ps, onPhysiologicalStatesCalibrated, &error);
        clCPhysiologicalStates_SetOnStatesUpdateEvent(ps, onPhysiologicalStatesUpdate, &error);
        clCPhysiologicalStates_SetOnIndividualNFBUpdateEvent(ps, onPhysiologicalStatesIndividualNFBUpdate, &error);
    }

    emotions = clCEmotions_Create(device, &error);
    if (!error.success) {
        __android_log_print(ANDROID_LOG_ERROR, "CAPSULE", "Failed to create emotion object: %s", error.message);
        stopRequested = true;
        return;
    }
    clCEmotions_SetOnEmotionalStatesUpdateEvent(emotions, onEmotionalStatesUpdate);







    clCDevice_Connect(device,   true, &error);
    if(clCDevice_IsConnected(device, &error)){
        __android_log_print(ANDROID_LOG_DEBUG, "CAPSULE", "Connected to device: %s", deviceID);
    }
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
        JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_DEBUG, "CAPSULE", "Start Resistance");
    clCError error;
    clCDevice_Start(device, &error);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSignalAndHR(
        JNIEnv *env, jobject thiz) {
    clCError error;
    clCNFBCalibrator_CalibrateIndividualNFB(calibrator, stage, &error);


}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopSignalAndHR(
        JNIEnv *env, jobject thiz) {
    clCError error;
    clCDevice_Stop(device, &error);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSession(
        JNIEnv *env, jobject thiz) {
}
extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopSession(
        JNIEnv *env, jobject thiz) {
    stopRequested = true;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopResistance(
        JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE", "Stop resist");
}
extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_removeAll(JNIEnv *env, jobject thiz) {
    removeAll();
}
