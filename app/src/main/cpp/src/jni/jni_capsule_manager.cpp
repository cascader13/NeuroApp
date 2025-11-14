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
clCMEMS MEMS = nullptr; // объект для получения значений акселерометра и гироскопа
//также надо добавить некоторые другие метрики(как RAW и RAW filthered)
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

void onDeviceList(clCDeviceLocator clClocator, clCDeviceInfoList devices, clCDeviceLocator_FailReason fail_reason) noexcept {
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

void onDeviceResistanceUpdate(clCDevice device, clCResistance resistance) noexcept {
    __android_log_print(ANDROID_LOG_INFO, "CAPSULE_RES", "Resistances: %d", clCResistance_GetCount(resistance));
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


void removeAll(){
    if (MEMS) {
        MEMS = nullptr;
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
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStopSignalAndHR(
        JNIEnv *env, jobject thiz) {
    // TODO: implement nativeStopSignalAndHR()
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
}
