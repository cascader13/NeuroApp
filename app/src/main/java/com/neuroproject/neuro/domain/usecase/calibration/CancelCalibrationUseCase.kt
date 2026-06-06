package com.neuroproject.neuro.domain.usecase.calibration


import com.neuroproject.neuro.domain.repository.DeviceGateway
import javax.inject.Inject

class CancelCalibrationUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    suspend operator fun invoke() {
        deviceGateway.stopSignalAndHR()
    }
}