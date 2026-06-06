package com.neuroproject.neuro.domain.usecase.sensor

import com.neuroproject.neuro.domain.model.ResistanceData
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveResistanceUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    operator fun invoke(): Flow<ResistanceData> = deviceGateway.observeResistance()
}