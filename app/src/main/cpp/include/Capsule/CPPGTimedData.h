// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief Array of PPG data with timestamps.
 *
 * Contains samples of PPG sensor and their timestamps
 */
CLC_STRUCT_WNN(PPGTimedData, clCPPGTimedData, cardio);

/**
 * \brief Get total number of PPG values.
 *
 * \param ppgTimedData PPG handle
 * \returns number of values
 */
CL_DLL int32_t clCPPGTimedData_GetCount(clCPPGTimedData ppgTimedData) NOEXCEPT;
/**
 * \brief Get PPG value by index.
 *
 * \param ppgTimedData PPG handle
 * \param index index
 * \returns ppg value
 */
CL_DLL float clCPPGTimedData_GetValue(clCPPGTimedData ppgTimedData, int32_t index) NOEXCEPT;
/**
 * \brief Get PPG timestamp by index.
 *
 * \param ppgTimedData PPG handle
 * \param index index
 * \returns timestamp in milliseconds since epoch
 */
CL_DLL uint64_t clCPPGTimedData_GetTimestampMilli(clCPPGTimedData ppgTimedData, int32_t index) NOEXCEPT;

#ifdef __cplusplus
}
#endif
