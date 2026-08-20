package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.model.DeviceInfo
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Запускает поиск доступных устройств поблизости.
 *
 * @return [Flow] со списком найденных устройств
 */
class SearchDevicesUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    operator fun invoke(): Flow<List<DeviceInfo>> = deviceGateway.searchDevices()
}