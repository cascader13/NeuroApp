package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveCalibrationStageUseCase @Inject constructor(
    private val deviceGateway: DeviceGateway
) {
    operator fun invoke(): Flow<Int> = deviceGateway.observeCalibrationState()
        .map { it.value }  // CalibrationStage -> Int
}