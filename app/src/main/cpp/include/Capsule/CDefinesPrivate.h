// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

// This is a private header with API internal defines. No need to directly use/include this header to your project.

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

// Linkage macro.
#if CC_PLATFORM_WIN
#ifdef CL_DYNAMIC // in case we want a dynamic library
#ifdef CL_EXPORT
#define CL_DLL __declspec(dllexport)
#else
#define CL_DLL __declspec(dllimport)
#endif
#else // if static linkage, we don't export or import anything
#define CL_DLL
#endif
#else // !CC_PLATFORM_WIN
#define CL_DLL
#endif

#define CLC_STRUCT(Name) \
    struct Name##d;      \
    typedef const struct Name##d* Name

#ifdef __cplusplus
#include <cstdint>
#define NOEXCEPT noexcept

#define CLC_CLASS_WN(Wrapped, Name) \
    namespace capsule::client {     \
    class Wrapped;                  \
    }                               \
    typedef class capsule::client::Wrapped* Name

#define CLC_STRUCT_WN(Wrapped, Name) \
    namespace capsule::client {      \
    struct Wrapped;                  \
    }                                \
    typedef struct capsule::client::Wrapped const* Name

#define CLC_STRUCT_WNN(Wrapped, Name, Namespace) \
    namespace capsule::Namespace {               \
    struct Wrapped;                              \
    }                                            \
    typedef struct capsule::Namespace::Wrapped const* Name

#else
#include <stdint.h>
#define NOEXCEPT

#define CLC_CLASS_WN(Wrapped, Name) \
    struct clCWrapped##d;           \
    typedef struct clCWrapped##d* Name

#define CLC_STRUCT_WN(Wrapped, Name) \
    struct clCWrapped##d;            \
    typedef struct clCWrapped##d const* Name

#endif //__cplusplus
