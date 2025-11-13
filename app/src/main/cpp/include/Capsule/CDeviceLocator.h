// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CDeviceInfoList.h"
#include "CError.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief Device locator error
 */
typedef enum clCDeviceLocator_FailReason {
    clCDeviceLocator_FailReason_OK = 0,            /**< No error. */
    clCDeviceLocator_FailReason_BluetoothDisabled, /**< Bluetooth adapter not found or disabled. */
    clCDeviceLocator_FailReason_Unknown,           /**< Unknown error. */
} clCDeviceLocator_FailReason;

typedef enum clCCapsule_LogLevel {
    clCCapsule_LogLevel_Trace,
    clCCapsule_LogLevel_Debug,
    clCCapsule_LogLevel_Info,
    clCCapsule_LogLevel_Warn,
    clCCapsule_LogLevel_Err,
    clCCapsule_LogLevel_Critical,
    clCCapsule_LogLevel_Off
} clCCapsule_LogLevel;

CL_DLL const char* clCCapsule_GetVersionString() NOEXCEPT;

/**
 * By default Capsule API operates in multi threaded mode - event callbacks are fired in a background thread.
 * Call this function once before working with Capsule to initiate single-thread mode. In order for Capsule
 * to work in this mode, `clCDeviceLocator_Update` must be called periodically on the main thread.
 * The event callbacks will be called inside `clCDeviceLocator_Update`.
 */
CL_DLL void clCCapsule_SetSingleThreaded(bool singleThreaded) NOEXCEPT;

/**
 * Set log level to filter Capsule's log messages
 */
CL_DLL void clCCapsule_SetLogLevel(clCCapsule_LogLevel logLevel) NOEXCEPT;
/**
 * Get current filter log level of Capsule
 * @return log level
 */
CL_DLL clCCapsule_LogLevel clCCapsule_GetLogLevel() NOEXCEPT;

/**
 * \brief Device locator.
 */
CLC_CLASS_WN(DeviceLocatorPrivate, clCDeviceLocator);

/**
 * Create device locator. Log files will not be created
 * @param error out error parameter
 * @return device locator handle
 */
CL_DLL clCDeviceLocator clCDeviceLocator_Create(clCError* error) NOEXCEPT;

/**
 * Create device locator with directory to store log files of Capsule
 * @param error out error parameter
 * @param logDirectory path to a folder where Capsule logs will be stored
 * @return device locator handle
 */
CL_DLL clCDeviceLocator clCDeviceLocator_CreateWithLogDirectory(const char* logDirectory, clCError* error) NOEXCEPT;

/**
 * This function should ONLY be called in single-threaded mode. In this mode it should be called
 * periodically from the main thread to fire event callbacks.
 * @param locator device locator handle
 */
CL_DLL void clCDeviceLocator_Update(clCDeviceLocator locator) NOEXCEPT;

/**
 * Release device locator.
 *
 * \param locator device locator handle
 */
CL_DLL void clCDeviceLocator_Destroy(clCDeviceLocator locator) NOEXCEPT;

/**
 * \brief Find available devices. Search time is in seconds.
 * Searches for available devices of chosen type.
 * Non-blocking - OnDevices event is fired when timeout expires.
 *
 * \param locator device locator handle
 * \param deviceType device type
 * \param searchTime search time in seconds
 * \param error out parameter for error
 */
CL_DLL void clCDeviceLocator_RequestDevices(clCDeviceLocator locator, clCDeviceType deviceType, int32_t searchTime, clCError* error) NOEXCEPT;

/**
 * \brief Device callback for the list of device info.
 */
typedef void (*clCDeviceLocator_DeviceListHandler)(clCDeviceLocator, clCDeviceInfoList, clCDeviceLocator_FailReason) NOEXCEPT;
/**
 * Subscribe for device list
 * \param locator device locator handle
 */
CL_DLL void clCDeviceLocator_SetOnDeviceListEvent(clCDeviceLocator locator, clCDeviceLocator_DeviceListHandler handler) NOEXCEPT;

#ifdef __cplusplus
}
#endif
