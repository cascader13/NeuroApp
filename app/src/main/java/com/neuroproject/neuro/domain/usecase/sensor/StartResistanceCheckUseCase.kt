package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.repository.DeviceGateway
import javax.inject.Inject

class StartResistanceCheckUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    suspend operator fun invoke() {
        deviceGateway.startResistanceCheck()
    }
}