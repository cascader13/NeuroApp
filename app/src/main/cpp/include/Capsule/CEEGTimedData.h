// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CError.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief Array of EEG data with timestamps.
 *
 * Contains EEG channels and timestamps
 */
CLC_STRUCT_WNN(EEGTimedData, clCEEGTimedData, device);

/**
 * Get number of EEG channels.
 *
 * \param eegTimedData EEG handle
 * \returns number of channels
 */
CL_DLL int32_t clCEEGTimedData_GetChannelsCount(clCEEGTimedData eegTimedData, clCError* error) NOEXCEPT;
/**
 * Get number of EEG samples in a channel.
 *
 * \param eegTimedData EEG handle
 * \returns number of samples in a channel
 */
CL_DLL int32_t clCEEGTimedData_GetSamplesCount(clCEEGTimedData eegTimedData, clCError* error) NOEXCEPT;

/**
 * Get raw EEG value by channel index and sample index.
 *
 * \param eegTimedData eeg handle
 * \param channelIndex index of a channel
 * \param sampleIndex index of a sample in a channel
 * \returns eeg value
 */
CL_DLL float clCEEGTimedData_GetRawValue(clCEEGTimedData eegTimedData, int32_t channelIndex, int32_t sampleIndex, clCError* error) NOEXCEPT;

/**
 * Get processed EEG value by channel index and sample index.
 *
 * \param eegTimedData eeg handle
 * \param channelIndex index of a channel
 * \param sampleIndex index of a sample in a channel
 * \returns eeg value
 */
CL_DLL float clCEEGTimedData_GetProcessedValue(clCEEGTimedData eegTimedData, int32_t channelIndex, int32_t sampleIndex, clCError* error) NOEXCEPT;
/**
 * Get EEG timestamp by index.
 *
 * \param eegTimedData EEG handle
 * \param sampleIndex index of a sample
 * \returns timestamp in milliseconds since epoch
 */
CL_DLL uint64_t clCEEGTimedData_GetTimestampMilli(clCEEGTimedData eegTimedData, int32_t sampleIndex, clCError* error) NOEXCEPT;

#ifdef __cplusplus
}
#endif
