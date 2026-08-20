package com.neuroproject.neuro.domain.usecase.device

import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Наблюдает за состоянием подключения к устройству.
 *
 * @return [Flow] с текущим состоянием подключения
 */
class ObserveConnectionStateUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    operator fun invoke(): Flow<DeviceConnectionState> = deviceGateway.observeConnectionState()
}