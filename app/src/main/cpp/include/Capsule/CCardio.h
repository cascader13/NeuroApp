// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CDevice.h"
#include "CNFBCalibrator.h"
#include "CPPGTimedData.h"

#ifdef __cplusplus
extern "C" {
#endif

CLC_CLASS_WN(ClassificationCardioPrivate, clCCardio);

typedef struct clCCardio_Data {
    int64_t timestampMilli = -1;
    float heartRate = 0.F;
    float stressIndex = 0.F;
    float kaplanIndex = 0.F;
    bool hasArtifacts = 0.F;
    bool skinContact = 0.F;
    bool motionArtifacts = 0.F;
    bool metricsAvailable = 0.F;
} clCCardio_Data;

CL_DLL clCCardio clCCardio_Create(clCDevice device, clCError* error) NOEXCEPT;
CL_DLL clCCardio clCCardio_CreateCalibrated(clCDevice device, clCNFBCalibrator calibrator, clCError* error) NOEXCEPT;

typedef void (*clCCardio_IndexesUpdateHandler)(clCCardio, const clCCardio_Data*) NOEXCEPT;
CL_DLL void clCCardio_SetOnIndexesUpdateEvent(clCCardio cardio, clCCardio_IndexesUpdateHandler handler, clCError* error) NOEXCEPT;

typedef void (*clCCardio_CalibratedHandler)(clCCardio) NOEXCEPT;
CL_DLL void clCCardio_SetOnCalibratedEvent(clCCardio cardio, clCCardio_CalibratedHandler handler, clCError* error) NOEXCEPT;

typedef void (*clCCardio_PPGDataHandler)(clCCardio, clCPPGTimedData) NOEXCEPT;
CL_DLL void clCCardio_SetOnPPGDataEvent(clCCardio cardio, clCCardio_PPGDataHandler handler, clCError* error) NOEXCEPT;

#ifdef __cplusplus
}
#endif
