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
//также надо добавить mems(это  гироскоп + акселерометр) и некоторые другие метрики(как RAW и RAW filthered)


// java объекты
jobject javaCapsule;
jclass capsuleClass;
jclass deviceInfo;

// callbacks(здесь будут написаны все коллбэки для связи с устройством)



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
    // Create client
    Loop();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartSearch(
        JNIEnv *env, jobject thiz) {
}
extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeConnect(
        JNIEnv *env, jobject thiz, jstring id) {
}
extern "C"
JNIEXPORT void JNICALL
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_nativeStartResistance(
        JNIEnv *env, jobject thiz) {
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
Java_com_neuroproject_neuro_services_CapsuleDeviceManager_00024Companion_removeAll(JNIEnv *env,
                                                                                     jobject thiz) {
}
