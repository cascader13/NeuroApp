// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CDevice.h"

#ifdef __cplusplus
extern "C" {
#endif

CLC_CLASS_WN(ClassificationEmotionsPrivate, clCEmotions);

typedef struct clCEmotions_States {
    int64_t timestampMilli = -1;
    float attention = -1.F;
    float relaxation = -1.F;
    float cognitiveLoad = -1.F;
    float cognitiveControl = -1.F;
    float selfControl = -1.F;
} clCEmotions_States;

CL_DLL clCEmotions clCEmotions_Create(clCDevice device, clCError* error) NOEXCEPT;

typedef void (*clCEmotions_EmotionalStatesUpdateEvent)(clCEmotions, const clCEmotions_States*) NOEXCEPT;
CL_DLL void clCEmotions_SetOnEmotionalStatesUpdateEvent(clCEmotions emotions, clCEmotions_EmotionalStatesUpdateEvent handler) NOEXCEPT;

typedef void (*clCEmotions_ErrorHandler)(clCEmotions, const char*) NOEXCEPT;
CL_DLL void clCEmotions_SetOnErrorEvent(clCEmotions emotions, clCEmotions_ErrorHandler handler) NOEXCEPT;

#ifdef __cplusplus
}
#endif
