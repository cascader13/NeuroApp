package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.model.ResistanceData
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Наблюдает за уровнем сопротивления электродов устройства.
 *
 * @return [Flow] с данными о сопротивлении
 */
class ObserveResistanceUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    operator fun invoke(): Flow<ResistanceData> = deviceGateway.observeResistance()
}