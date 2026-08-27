package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.model.BatteryData
import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Наблюдает за уровнем заряда Capsule.
 */
class ObserveBatteryUseCase @Inject constructor(
    private val deviceGateway: CapsuleDeviceGateway
) {

    operator fun invoke(): Flow<BatteryData> =
        deviceGateway.observeBatteryCharge()
}