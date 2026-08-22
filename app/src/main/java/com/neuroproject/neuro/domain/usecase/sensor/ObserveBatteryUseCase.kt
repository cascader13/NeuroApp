package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.model.BatteryData
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Наблюдает за уровнем заряда батареи устройства.
 *
 * @return [Flow] с данными о заряде батареи
 */
class ObserveBatteryUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    operator fun invoke(): Flow<BatteryData> = deviceGateway.observeBatteryCharge()
}