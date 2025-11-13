// Copyright. 2019 - 2024 PSBD. All rights reserved.

#pragma once

#include "CDefinesPrivate.h"
#include "CDevice.h"
#include "CError.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief Individual NFB calibrator
 */
CLC_CLASS_WN(NFBCalibratorPrivate, clCNFBCalibrator);

typedef enum clCIndividualNFBCalibrationStage {
    clCIndividualNFBCalibrationStage_1 = 0,
    clCIndividualNFBCalibrationStage_2,
    clCIndividualNFBCalibrationStage_3,
    clCIndividualNFBCalibrationStage_4,
} clCIndividualNFBCalibrationStage;

typedef enum clCIndividualNFBCalibrationFailReason {
    clC_IndividualNFBCalibrationFailReason_None = 0,
    clC_IndividualNFBCalibrationFailReason_TooManyArtifacts,
    clC_IndividualNFBCalibrationFailReason_PeakIsABorder,
} clCIndividualNFBCalibrationFailReason;

typedef struct clCIndividualNFBData {
    int64_t timestampMilli = -1;
    clCIndividualNFBCalibrationFailReason failReason = clC_IndividualNFBCalibrationFailReason_None;
    /**
     * \brief Individual NFB frequency.
     */
    float individualFrequency = 10.F;
    /**
     * \brief Individual NFB peak frequency.
     */
    float individualPeakFrequency = 10.F;
    /**
     * \brief Individual NFB peak frequency power.
     */
    float individualPeakFrequencyPower = 10.F;
    /**
     * \brief Individual NFB peak frequency suppression.
     */
    float individualPeakFrequencySuppression = 2.F;
    /**
     * \brief Individual NFB bandwidth.
     */
    float individualBandwidth = 6.F;
    /**
     * \brief NFB power.
     */
    float individualNormalizedPower = 0.5F;
    /**
     * \brief Left frequency bound
     */
    float lowerFrequency = 7.F;
    /**
     * \brief Right frequency bound
     */
    float upperFrequency = 13.F;
} clCIndividualNFBData;

/**
 * \brief Obtain an individual NFB calibrator for the session. If not present, it'll
 * be created.
 *
 * \param device device handle. A valid device must be passed
 *
 * \returns NFB calibrator handle
 */
CL_DLL clCNFBCalibrator clCNFBCalibrator_CreateOrGet(clCDevice device) NOEXCEPT;

/**
 * \brief Start stage of individual NFB calibration
 *
 * \param calibrator calibrator handle
 * \param stage the user must call this method 4 times sequentially
 * changing this argument from Stage1 to Stage4
 * \param error the error out parameter
 */
CL_DLL void clCNFBCalibrator_CalibrateIndividualNFB(clCNFBCalibrator calibrator, clCIndividualNFBCalibrationStage stage, clCError* error) NOEXCEPT;

/**
 * \brief Start stage of quick individual NFB calibration
 *
 * \param calibrator calibrator handle
 * \param error the error out parameter
 */
CL_DLL void clCNFBCalibrator_CalibrateIndividualNFBQuick(clCNFBCalibrator calibrator, clCError* error) NOEXCEPT;

/**
 * \brief Import individual nfb data from external source
 *
 * \param calibrator calibrator handle
 * \param error the error out parameter
 */
CL_DLL void clCNFBCalibrator_ImportIndividualNFBData(clCNFBCalibrator calibrator, const clCIndividualNFBData* individualNfbData, clCError* error) NOEXCEPT;

/**
 * \brief Get individual NFB metrics
 * \param calibrator calibrator handle
 * \param nfbData the individual NFB metric out parameter
 * \param error the error out parameter
 * \returns INFB metric from calibrator via out parameter
 */
CL_DLL void clCNFBCalibrator_GetIndividualNFB(clCNFBCalibrator calibrator, clCIndividualNFBData* nfbData, clCError* error) NOEXCEPT;

/**
 * \brief Is calibration finished
 *
 * \param calibrator calibrator handle
 * \return \c true if calibration has finished
 */
CL_DLL bool clCNFBCalibrator_IsCalibrated(clCNFBCalibrator calibrator) NOEXCEPT;
/**
 * \brief Has calibration failed
 *
 * \param calibrator calibrator handle
 * \return \c true if calibration has failed
 */
CL_DLL bool clCNFBCalibrator_HasCalibrationFailed(clCNFBCalibrator calibrator) NOEXCEPT;

typedef void (*clCNFBCalibrator_CalibrationStageFinishedHandler)(clCNFBCalibrator calibrator) NOEXCEPT;
CL_DLL void clCNFBCalibrator_SetOnCalibrationStageFinishedEvent(clCNFBCalibrator calibrator, clCNFBCalibrator_CalibrationStageFinishedHandler handler) NOEXCEPT;

typedef void (*clCNFBCalibrator_CalibratedHandler)(clCNFBCalibrator calibrator, const clCIndividualNFBData*) NOEXCEPT;
CL_DLL void clCNFBCalibrator_SetOnCalibratedEvent(clCNFBCalibrator calibrator, clCNFBCalibrator_CalibratedHandler handler) NOEXCEPT;

#ifdef __cplusplus
}
#endif
