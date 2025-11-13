// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CDevice.h"
#include "CMEMSTimedData.h"
#include "CNFBCalibrator.h"

#ifdef __cplusplus
extern "C" {
#endif

CLC_CLASS_WN(ClassificationMEMSPrivate, clCMEMS);

CL_DLL clCMEMS clCMEMS_Create(clCDevice device, clCError* error) NOEXCEPT;
CL_DLL clCMEMS clCMEMS_CreateCalibrated(clCDevice device, clCNFBCalibrator calibrator, clCError* error) NOEXCEPT;

typedef void (*clCMEMS_TimedDataUpdateHandler)(clCMEMS, clCMEMSTimedData) NOEXCEPT;
CL_DLL void clCMEMS_SetOnMEMSTimedDataUpdateEvent(clCMEMS mems, clCMEMS_TimedDataUpdateHandler handler, clCError* error) NOEXCEPT;

#ifdef __cplusplus
}
#endif
