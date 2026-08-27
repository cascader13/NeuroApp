package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import javax.inject.Inject

/**
 * Запускает проверку сопротивления электродов Capsule.
 */
class StartResistanceCheckUseCase @Inject constructor(
    private val deviceGateway: CapsuleDeviceGateway
) {

    operator fun invoke() {
        deviceGateway.startResistanceCheck()
    }
}