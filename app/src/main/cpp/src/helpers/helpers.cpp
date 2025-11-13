//
// Created by aseatari on 17.09.2024.
//

#include "helpers.h"
#include <vector>

JNIEnvironment::JNIEnvironment(JNIEnv *env, std::thread::id id, bool isAttached) :
        jniEnv(env),
        threadId(id),
        hasBeenAttached(isAttached)
{
}

void JniResolver::SetJVM(JavaVM *vm)
{
    mJvm = vm;
}

std::shared_ptr<JNIEnvironment> JniResolver::GetEnv()
{
    // lock mutex
    std::unique_lock lock(mResolverMutex);
    auto currentThreadId = std::this_thread::get_id();

    auto envIter = mRegisteredEnvironments.find(currentThreadId);
    if(envIter != mRegisteredEnvironments.end())
    {
        auto weakEnvPtr =envIter->second;
        auto sharedEnvPtr = weakEnvPtr.lock();
        // if current thread has been already attached
        if(sharedEnvPtr)
            return sharedEnvPtr;
    }


    bool hasBeenAttached = false;
    JNIEnv* jniEnv = nullptr;
    int getEnvStat = mJvm->GetEnv((void **) &jniEnv, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED)
    {
        if (mJvm->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK)
        {
            throw std::runtime_error("Unable to attach thread to JVM");
        }
        hasBeenAttached = true;
    }


    auto environment = std::shared_ptr<JNIEnvironment>(new JNIEnvironment(jniEnv, currentThreadId, hasBeenAttached),
                                                       [&](JNIEnvironment* env)
                                                       {
                                                           auto threadId = env->threadId;
                                                           auto hasBeenAttached = env->hasBeenAttached;
                                                           {
                                                               std::unique_lock lock(mResolverMutex);
                                                               mRegisteredEnvironments.erase(threadId);
                                                           }
                                                           delete env;

                                                           if(hasBeenAttached)
                                                           {
                                                               mJvm->DetachCurrentThread();
                                                           }
                                                       });
    mRegisteredEnvironments.insert_or_assign(currentThreadId,environment);
    return environment;
}
