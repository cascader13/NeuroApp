package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.repository.DeviceGateway
import javax.inject.Inject

/**
 * Подключается к устройству по идентификатору.
 *
 * @param deviceId идентификатор устройства для подключения
 */
class ConnectDeviceUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    suspend operator fun invoke(deviceId: String) = deviceGateway.connect(deviceId)
}