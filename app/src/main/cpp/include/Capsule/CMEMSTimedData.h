// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CPoint3d.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief Array of MEMS data with timestamps.
 *
 * Contains samples of accelerometer and gyroscope 3D vectors and their timestamps
 */
CLC_STRUCT_WNN(MEMSTimedData, clCMEMSTimedData, mems);

/**
 * Get total number of MEMS values.
 *
 * \param memsTimedData mems handle
 * \returns number of values
 */
CL_DLL int32_t clCMEMSTimedData_GetCount(clCMEMSTimedData memsTimedData) NOEXCEPT;
/**
 * Get accelerometer orientation vector by index.
 *
 * \param memsTimedData mems handle
 * \param index index
 * \returns accelerometer orientation vector
 */
CL_DLL clCPoint3d clCMEMSTimedData_GetAccelerometer(clCMEMSTimedData memsTimedData,
                                                    int32_t index) NOEXCEPT;
/**
 * Get gyroscope orientation vector by index.
 *
 * \param memsTimedData mems handle
 * \param index index
 * \returns gyroscope orientation in 3D vector
 */
CL_DLL clCPoint3d clCMEMSTimedData_GetGyroscope(clCMEMSTimedData memsTimedData,
                                                int32_t index) NOEXCEPT;
/**
 * Get MEMS timestamp by index.
 *
 * \param memsTimedData mems handle
 * \param index index
 * \returns timestamp in milliseconds since epoch
 */
CL_DLL uint64_t clCMEMSTimedData_GetTimestampMilli(clCMEMSTimedData memsTimedData, int32_t index) NOEXCEPT;

#ifdef __cplusplus
}
#endif
