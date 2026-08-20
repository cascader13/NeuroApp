package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.repository.DeviceGateway
import javax.inject.Inject

/**
 * Запускает проверку сопротивления электродов.
 */
class StartResistanceCheckUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    suspend operator fun invoke() {
        deviceGateway.startResistanceCheck()
    }
}