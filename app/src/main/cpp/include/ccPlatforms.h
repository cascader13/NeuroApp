// Copyright. 2019 - 2023 Neiry. All rights reserved.

#pragma once

//*****************************************************************************
// Platform identification.
//*****************************************************************************
#ifdef _WIN64
#define CC_PLATFORM_WIN 1
#define CC_PLATFORM_WIN64 1
#elif defined _WIN32 // defined for both 32 and 64 (so we check 64 first)
#define CC_PLATFORM_WIN 1
#define CC_PLATFORM_WIN32 1
#elif __ANDROID__
#define CC_PLATFORM_ANDROID 1
#elif __APPLE__
#define CC_PLATFORM_APPLE 1
#include "TargetConditionals.h"
#if TARGET_IPHONE_SIMULATOR
#define CC_PLATFORM_IOS 1
#define CC_PLATFORM_IOS_SIMULATOR 1
#elif TARGET_OS_IPHONE
#define CC_PLATFORM_IOS 1
#elif TARGET_OS_MAC
#define CC_PLATFORM_MAC 1
#else
#define CC_PLATFORM_APPLE_UNKNOWN 1
#endif
#elif __linux__
#define CC_PLATFORM_LINUX 1
#elif __unix__
#define CC_PLATFORM_UNIX 1
#else
#define CC_PLATFORM_UNKNOWN 1
#endif
