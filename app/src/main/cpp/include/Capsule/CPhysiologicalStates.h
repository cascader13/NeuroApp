// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CDevice.h"

#ifdef __cplusplus
extern "C" {
#endif

CLC_CLASS_WN(ClassificationPhysiologicalStatesPrivate, clCPhysiologicalStates);

typedef struct clCPhysiologicalStates_Value {
    int64_t timestampMilli = -1;
    float relaxation = -1.F;
    float fatigue = -1.F;
    float none = -1.F;
    float concentration = -1.F;
    float involvement = -1.F;
    float stress = -1.F;
    bool nfbArtifacts = false;
    bool cardioArtifacts = false;
} clCPhysiologicalStates_Value;

typedef struct clCPhysiologicalStates_Baselines {
    int64_t timestampMilli = -1;
    float alpha = -1.F;
    float beta = -1.F;
    float alphaGravity = -1.F;
    float betaGravity = -1.F;
    float concentration = -1.F;
} clCPhysiologicalStates_Baselines;

CL_DLL clCPhysiologicalStates clCPhysiologicalStates_Create(clCDevice device, clCError* error) NOEXCEPT;
CL_DLL void clCPhysiologicalStates_ImportBaselines(clCPhysiologicalStates states, const clCPhysiologicalStates_Baselines* baselines) NOEXCEPT;
CL_DLL void clCPhysiologicalStates_StartBaselineCalibration(clCPhysiologicalStates states) NOEXCEPT;

typedef void (*clCPhysiologicalStates_StatesUpdateHandler)(clCPhysiologicalStates, const clCPhysiologicalStates_Value*) NOEXCEPT;
CL_DLL void clCPhysiologicalStates_SetOnStatesUpdateEvent(clCPhysiologicalStates states, clCPhysiologicalStates_StatesUpdateHandler handler, clCError* error) NOEXCEPT;

typedef void (*clCPhysiologicalStates_CalibratedHandler)(clCPhysiologicalStates, const clCPhysiologicalStates_Baselines*) NOEXCEPT;
CL_DLL void clCPhysiologicalStates_SetOnCalibratedEvent(clCPhysiologicalStates states, clCPhysiologicalStates_CalibratedHandler handler, clCError* error) NOEXCEPT;

typedef void (*clCPhysiologicalStates_CalibrationProgressUpdateHandler)(clCPhysiologicalStates, float) NOEXCEPT;
CL_DLL void clCPhysiologicalStates_SetOnCalibrationProgressUpdateEvent(clCPhysiologicalStates states, clCPhysiologicalStates_CalibrationProgressUpdateHandler handler, clCError* error) NOEXCEPT;

typedef void (*clCPhysiologicalStates_IndividualNFBUpdateHandler)(clCPhysiologicalStates) NOEXCEPT;
CL_DLL void clCPhysiologicalStates_SetOnIndividualNFBUpdateEvent(clCPhysiologicalStates states, clCPhysiologicalStates_IndividualNFBUpdateHandler handler, clCError* error) NOEXCEPT;

#ifdef __cplusplus
}
#endif
