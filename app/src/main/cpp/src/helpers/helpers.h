//
// Created by aseatari on 17.09.2024.
//

#ifndef STRESSCALENDAR_HELPERS_H
#define STRESSCALENDAR_HELPERS_H

#include <jni.h>
#include "memory"
#include "unordered_map"
#include "thread"
#include <mutex>

struct JNIEnvironment
{
    explicit JNIEnvironment(JNIEnv *env, std::thread::id id, bool);

    JNIEnv *jniEnv;
    std::thread::id threadId;
    bool hasBeenAttached;
};

class JniResolver {
public:
    static JniResolver* Instance() {
        static JniResolver inst;
        return &inst;
    }
    void SetJVM(JavaVM* vm);

    std::shared_ptr<JNIEnvironment> GetEnv();

private:
    JavaVM * mJvm;
    std::unordered_map<std::thread::id, std::weak_ptr<JNIEnvironment>> mRegisteredEnvironments;
    std::mutex mResolverMutex;

    JniResolver() {}
};

#endif //STRESSCALENDAR_HELPERS_H
