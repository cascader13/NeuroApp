// Copyright. 2019 - 2025 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CError.h"

#ifdef __cplusplus
extern "C" {
#endif

CLC_STRUCT_WNN(PSDData, clCPSDData, device);

typedef enum clCPSDData_Band {
    clCPSDData_Band_Delta,
    clCPSDData_Band_Theta,
    clCPSDData_Band_Alpha,
    clCPSDData_Band_SMR,
    clCPSDData_Band_Beta
} clCPSDData_Band;

CL_DLL uint64_t clCPSDData_GetTimestampMilli(clCPSDData psdData, clCError* error) NOEXCEPT;
CL_DLL int32_t clCPSDData_GetFrequenciesCount(clCPSDData psdData, clCError* error) NOEXCEPT;
CL_DLL int32_t clCPSDData_GetChannelsCount(clCPSDData psdData, clCError* error) NOEXCEPT;
CL_DLL double clCPSDData_GetFrequency(clCPSDData psdData, int32_t frequencyIndex, clCError* error) NOEXCEPT;
CL_DLL double clCPSDData_GetPSD(clCPSDData psdData, int32_t channelIndex, int32_t frequencyIndex, clCError* error) NOEXCEPT;
CL_DLL float clCPSDData_GetBandUpper(clCPSDData psdData, clCPSDData_Band band, clCError* error) NOEXCEPT;
CL_DLL float clCPSDData_GetBandLower(clCPSDData psdData, clCPSDData_Band band, clCError* error) NOEXCEPT;
CL_DLL bool clCPSDData_HasIndividualAlpha(clCPSDData psdData, clCError* error) NOEXCEPT;
CL_DLL float clCPSDData_GetIndividualAlphaLower(clCPSDData psdData, clCError* error) NOEXCEPT;
CL_DLL float clCPSDData_GetIndividualAlphaUpper(clCPSDData psdData, clCError* error) NOEXCEPT;
CL_DLL bool clCPSDData_HasIndividualBeta(clCPSDData psdData, clCError* error) NOEXCEPT;
CL_DLL float clCPSDData_GetIndividualBetaLower(clCPSDData psdData, clCError* error) NOEXCEPT;
CL_DLL float clCPSDData_GetIndividualBetaUpper(clCPSDData psdData, clCError* error) NOEXCEPT;

#ifdef __cplusplus
}
#endif
