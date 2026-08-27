package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import javax.inject.Inject

/**
 * Останавливает проверку сопротивления электродов Capsule.
 */
class StopResistanceCheckUseCase @Inject constructor(
    private val deviceGateway: CapsuleDeviceGateway
) {

    operator fun invoke() {
        deviceGateway.stopResistanceCheck()
    }
}