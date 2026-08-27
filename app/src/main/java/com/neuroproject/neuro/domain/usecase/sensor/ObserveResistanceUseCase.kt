package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.model.ResistanceData
import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Наблюдает за сопротивлением электродов Capsule.
 */
class ObserveResistanceUseCase @Inject constructor(
    private val deviceGateway: CapsuleDeviceGateway
) {

    operator fun invoke(): Flow<ResistanceData> =
        deviceGateway.observeResistance()
}