package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.repository.DeviceGateway
import javax.inject.Inject

class ConnectDeviceUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    suspend operator fun invoke(deviceId: String) = deviceGateway.connect(deviceId)
}