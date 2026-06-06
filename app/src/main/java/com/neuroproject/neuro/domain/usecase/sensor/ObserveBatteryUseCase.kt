package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.model.BatteryData
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBatteryUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    operator fun invoke(): Flow<BatteryData> = deviceGateway.observeBatteryCharge()
}