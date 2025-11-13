// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CError.h"
#include "CNFBCalibrator.h"

#ifdef __cplusplus
#include <cstddef>
extern "C" {
#else
#include <stddef.h>
#endif

CLC_CLASS_WN(ClassificationProductivityPrivate, clCProductivity);

typedef enum clCProductivity_FatigueGrowthRate {
    clCProductivity_FatigueGrowthRate_None,
    clCProductivity_FatigueGrowthRate_Low,
    clCProductivity_FatigueGrowthRate_Medium,
    clCProductivity_FatigueGrowthRate_High
} clCProductivity_FatigueGrowthRate;

typedef struct clCProductivity_Metrics {
    int64_t timestampMilli = -1;
    float fatigueScore = -1.F;
    float reverseFatigueScore = -1.F;
    float gravityScore = -1.F;
    float relaxationScore = -1.F;
    float concentrationScore = -1.F;
    float productivityScore = -1.F;
    float currentValue = -1.F;
    float alpha = -1.F;
    float productivityBaseline = -1.F;
    float accumulatedFatigue = -1.F;
    clCProductivity_FatigueGrowthRate fatigueGrowthRate = clCProductivity_FatigueGrowthRate_None;
    const uint8_t* artifactsData = NULL;
    uint64_t artifactsSize = 0U;
} clCProductivity_Metrics;

typedef enum clCProductivity_RecommendationValue {
    clCProductivity_RecommendationValue_NoRecommendation,
    clCProductivity_RecommendationValue_Involvement,
    clCProductivity_RecommendationValue_Relaxation,
    clCProductivity_RecommendationValue_SlightFatigue,
    clCProductivity_RecommendationValue_SevereFatigue,
    clCProductivity_RecommendationValue_ChronicFatigue
} clCProductivity_RecommendationValue;

typedef enum clCProductivity_StressValue {
    clCProductivity_StressValue_NoStress,
    clCProductivity_StressValue_Anxiety,
    clCProductivity_StressValue_Stress
} clCProductivity_StressValue;

typedef struct clCProductivity_Indexes {
    int64_t timestampMilli = -1;
    clCProductivity_RecommendationValue relaxation = clCProductivity_RecommendationValue_NoRecommendation;
    clCProductivity_StressValue stress = clCProductivity_StressValue_NoStress;
    float gravityBaseline = -1.F;
    float productivityBaseline = -1.F;
    float fatigueBaseline = -1.F;
    float reverseFatigueBaseline = -1.F;
    float relaxationBaseline = -1.F;
    float concentrationBaseline = -1.F;
    bool hasArtifacts = false;
} clCProductivity_Indexes;

typedef struct clCProductivity_Baselines {
    int64_t timestampMilli = -1;
    float gravity = -1.F;
    float productivity = -1.F;
    float fatigue = -1.F;
    float reverseFatigue = -1.F;
    float relaxation = -1.F;
    float concentration = -1.F;
} clCProductivity_Baselines;

CL_DLL clCProductivity clCProductivity_Create(clCDevice device, clCError* error) NOEXCEPT;
CL_DLL clCProductivity clCProductivity_CreateWithIndividualData(clCDevice device, const clCIndividualNFBData* data, clCError* error) NOEXCEPT;

CL_DLL void clCProductivity_ImportBaselines(clCProductivity productivity, const clCProductivity_Baselines* baselines, clCError* error) NOEXCEPT;
CL_DLL void clCProductivity_ResetAccumulatedFatigue(clCProductivity productivity, clCError* error) NOEXCEPT;
CL_DLL void clCProductivity_StartBaselineCalibration(clCProductivity productivity) NOEXCEPT;

typedef void (*clCProductivity_BaselineCalibratedHandler)(clCProductivity, const clCProductivity_Baselines*) NOEXCEPT;
CL_DLL void clCProductivity_SetOnBaselineUpdateEvent(clCProductivity productivityPtr, clCProductivity_BaselineCalibratedHandler handler) NOEXCEPT;

typedef void (*clCProductivity_MetricsUpdateHandler)(clCProductivity, const clCProductivity_Metrics*) NOEXCEPT;
CL_DLL void clCProductivity_SetOnMetricsUpdateEvent(clCProductivity productivityPtr, clCProductivity_MetricsUpdateHandler handler) NOEXCEPT;

typedef void (*clCProductivity_IndexesStateUpdateHandler)(clCProductivity, const clCProductivity_Indexes*) NOEXCEPT;
CL_DLL void clCProductivity_SetOnIndexesUpdateEvent(clCProductivity productivityPtr, clCProductivity_IndexesStateUpdateHandler handler) NOEXCEPT;

typedef void (*clCProductivity_CalibrationProgressUpdateHandler)(clCProductivity, float) NOEXCEPT;
CL_DLL void clCProductivity_SetOnCalibrationProgressUpdateEvent(clCProductivity productivity, clCProductivity_CalibrationProgressUpdateHandler handler) NOEXCEPT;

typedef void (*clCProductivity_IndividualNFBUpdateHandler)(clCProductivity) NOEXCEPT;
CL_DLL void clCProductivity_SetOnIndividualNFBUpdateEvent(clCProductivity productivity, clCProductivity_IndividualNFBUpdateHandler handler) NOEXCEPT;

#ifdef __cplusplus
}
#endif
