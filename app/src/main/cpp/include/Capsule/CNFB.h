// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CError.h"
#include "CNFBCalibrator.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief NFB call result.
 */
typedef enum clCNFB_CallResult {
    clCNFB_CallResult_Success,         /**< Call has finished successfully. */
    clCNFB_CallResult_FailedToSendData /**< Failed to send data, session might not be active. */
} clCNFB_CallResult;

/**
 * \brief User state, determined by NFB classifier.
 */
typedef struct clCNFB_UserState {
    int64_t timestampMilli = -1;
    float delta = -1.F;
    float theta = -1.F;
    float alpha = -1.F;
    float smr = -1.F;
    float beta = -1.F;
} clCNFB_UserState;

CLC_CLASS_WN(ClassificationNFBPrivate, clCNFB);
/**
 * Create a NFB classifier.
 *
 * \param device device handle. Valid device must be passed
 * \param[out] error error parameter
 * \returns NFB handle.
 */
CL_DLL clCNFB clCNFB_Create(clCDevice device, clCError* error) NOEXCEPT;
/**
 * Create a NFB classifier after individual NFB has calibrated.
 *
 * \param device device handle. Valid device must be passed
 * \param calibrator NFB calibrator handle. A calibrated calibrator must be passed.
 * \param error out error parameter
 * \returns NFB handle.
 */
CL_DLL clCNFB clCNFB_CreateCalibrated(clCDevice device, clCNFBCalibrator calibrator, clCError* error) NOEXCEPT;

typedef void (*clCNFB_UserStateChangedHandler)(clCNFB, const clCNFB_UserState*) NOEXCEPT;
CL_DLL void clCNFB_SetOnUserStateChangedEvent(clCNFB nfb, clCNFB_UserStateChangedHandler handler) NOEXCEPT;

typedef void (*clCNFB_ErrorHandler)(clCNFB, const char*) NOEXCEPT;
CL_DLL void clCNFB_SetOnErrorEvent(clCNFB nfb, clCNFB_ErrorHandler handler) NOEXCEPT;

#ifdef __cplusplus
}
#endif
