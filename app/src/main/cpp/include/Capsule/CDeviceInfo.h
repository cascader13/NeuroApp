// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief Device type.
 */
typedef enum clCDeviceType {
    clCDeviceType_Headband = 0,
    clCDeviceType_Buds = 1,
    clCDeviceType_Headphones = 2,
    clCDeviceType_Impulse = 3,
    clCDeviceType_Any = 4,
    clCDeviceType_BrainBit = 6,
    clCDeviceType_SinWave = 100,
    clCDeviceType_Noise = 101
} clCDeviceType;

/**
 * \brief Device info list.
 *
 * Contains device information: name, id (serial number) and type
 */
CLC_STRUCT_WNN(DeviceInfo, clCDeviceInfo, device);

/**
 * Get device ID (serial number).
 *
 * \param device device info handle
 * \returns device ID string
 */
CL_DLL const char* clCDeviceInfo_GetSerial(clCDeviceInfo device) NOEXCEPT;
/**
 * Get device name.
 *
 * \param device device info handle
 * \returns device name string
 */
CL_DLL const char* clCDeviceInfo_GetName(clCDeviceInfo device) NOEXCEPT;
/**
 * Get device type.
 *
 * \param device device info handle
 * \returns device type
 */
CL_DLL clCDeviceType clCDeviceInfo_GetType(clCDeviceInfo device) NOEXCEPT;

#ifdef __cplusplus
}
#endif
