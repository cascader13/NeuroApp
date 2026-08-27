package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.CalibrationRepository
import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import javax.inject.Inject

/**
 * Use case импорта Capsule-калибровки в устройство.
 *
 * При выборе "использовать прошлую калибровку" загружает
 * сохранённые параметры из БД и импортирует их в Capsule.
 *
 * Если прошлая калибровка отсутствует или выбрана новая,
 * запускает новый процесс Capsule-калибровки.
 */
class ImportCalibrationUseCase @Inject constructor(
    private val deviceGateway: CapsuleDeviceGateway,
    private val calibrationRepository: CalibrationRepository,
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        usePrevious: Boolean
    ) {
        if (usePrevious) {
            val userId =
                authRepository.getUserId()

            val previousData =
                calibrationRepository
                    .getPreviousCalibration(userId)

            if (previousData != null) {
                deviceGateway.importCalibration(
                    previousData
                )

                if (
                    previousData.productivityGravity != null
                ) {
                    deviceGateway
                        .importProductivityCalibration(
                            gravity =
                                previousData
                                    .productivityGravity,

                            productivity =
                                previousData
                                    .productivityProductivity
                                    ?: 0f,

                            fatigue =
                                previousData
                                    .productivityFatigue
                                    ?: 0f,

                            reverseFatigue =
                                previousData
                                    .productivityReverseFatigue
                                    ?: 0f,

                            relaxation =
                                previousData
                                    .productivityRelaxation
                                    ?: 0f,

                            concentration =
                                previousData
                                    .productivityConcentration
                                    ?: 0f
                        )
                }

                if (
                    previousData.physiologicalAlpha != null
                ) {
                    deviceGateway
                        .importPhysiologicalCalibration(
                            alpha =
                                previousData
                                    .physiologicalAlpha,

                            beta =
                                previousData
                                    .physiologicalBeta
                                    ?: 0f,

                            alphaGravity =
                                previousData
                                    .physiologicalAlphaGravity
                                    ?: 0f,

                            betaGravity =
                                previousData
                                    .physiologicalBetaGravity
                                    ?: 0f,

                            concentration =
                                previousData
                                    .physiologicalConcentration
                                    ?: 0f
                        )
                }

                return
            }
        }

        deviceGateway.startSignalAndHR()
    }
}