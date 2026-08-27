package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import javax.inject.Inject

/**
 * Отменяет текущую Capsule-калибровку,
 * останавливая передачу сигнала и ЧСС.
 */
class CancelCalibrationUseCase @Inject constructor(
    private val deviceGateway: CapsuleDeviceGateway
) {

    suspend operator fun invoke() {
        deviceGateway.stopSignalAndHR()
    }
}