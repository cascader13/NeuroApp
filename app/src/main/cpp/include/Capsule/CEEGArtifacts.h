// Copyright. 2019 - 2025 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CError.h"

#ifdef __cplusplus
extern "C" {
#endif

CLC_STRUCT_WNN(EEGArtifacts, clCEEGArtifacts, device);

/**
 * Get EEG timestamp.
 *
 * \param eegArtifacts EEG artifacts handle
 * \param error out error parameter
 * \returns timestamp in milliseconds since epoch
 */
CL_DLL uint64_t clCEEGArtifacts_GetTimestampMilli(clCEEGArtifacts eegArtifacts, clCError* error) NOEXCEPT;
/**
 * Get number of EEG artifact channels.
 *
 * \param eegArtifacts EEG artifacts handle
 * \returns number of artifact channels
 */
CL_DLL int32_t clCEEGArtifacts_GetChannelsCount(clCEEGArtifacts eegArtifacts, clCError* error) NOEXCEPT;
CL_DLL uint8_t clCEEGArtifacts_GetArtifactByChannel(clCEEGArtifacts eegArtifacts, int32_t channelIndex, clCError* error) NOEXCEPT;
CL_DLL float clCEEGArtifacts_GetEEGQuality(clCEEGArtifacts eegArtifacts, int32_t channelIndex, clCError* error) NOEXCEPT;

#ifdef __cplusplus
}
#endif
