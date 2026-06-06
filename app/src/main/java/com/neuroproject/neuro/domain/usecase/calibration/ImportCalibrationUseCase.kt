
package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.model.CalibrationSample
import com.neuroproject.neuro.domain.repository.CalibrationRepository
import com.neuroproject.neuro.domain.repository.DeviceGateway
import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject

class ImportCalibrationUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway,
    private val calibrationRepository: CalibrationRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(usePrevious: Boolean) {
        if (usePrevious) {
            val userId = authRepository.getUserId()
            val previousData = calibrationRepository.getPreviousCalibration(userId)
            if (previousData != null) {
                deviceGateway.importCalibration(previousData)
                return
            }
        }
        // Иначе запускаем новую калибровку
        deviceGateway.startSignalAndHR()
    }
}