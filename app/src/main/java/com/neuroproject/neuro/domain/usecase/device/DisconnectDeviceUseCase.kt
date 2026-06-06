package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.repository.DeviceGateway
import javax.inject.Inject

class DisconnectDeviceUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    suspend operator fun invoke() = deviceGateway.disconnect()
}