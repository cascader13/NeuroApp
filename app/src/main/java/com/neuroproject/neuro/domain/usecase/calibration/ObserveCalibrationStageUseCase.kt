package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Наблюдает за текущим этапом Capsule-калибровки.
 *
 * @return Flow с числовым значением этапа калибровки.
 */
class ObserveCalibrationStageUseCase @Inject constructor(
    private val deviceGateway: CapsuleDeviceGateway
) {

    operator fun invoke(): Flow<Int> =
        deviceGateway
            .observeCalibrationState()
            .map { stage ->
                stage.value
            }
}