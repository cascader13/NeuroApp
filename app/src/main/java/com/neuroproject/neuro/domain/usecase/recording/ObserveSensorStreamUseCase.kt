package com.neuroproject.neuro.domain.usecase.recording

import com.neuroproject.neuro.domain.model.SensorSample
import com.neuroproject.neuro.domain.repository.SensorEvent
import com.neuroproject.neuro.domain.repository.SensorStreamGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Наблюдает за потоком данных со всех сенсоров устройства.
 *
 * @return [Flow] с событиями сенсоров
 */
class ObserveSensorStreamUseCase @Inject constructor(
    private val sensorStreamGateway: SensorStreamGateway
) {
    operator fun invoke(): Flow<SensorEvent> = sensorStreamGateway.observeAll()
}