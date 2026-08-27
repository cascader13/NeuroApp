package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.model.CalibrationStage
import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

class ObserveCalibrationStageUseCaseTest {

    private lateinit var useCase: ObserveCalibrationStageUseCase
    private val deviceGateway: CapsuleDeviceGateway = mock()

    @Before
    fun setup() {
        useCase = ObserveCalibrationStageUseCase(deviceGateway)
    }

    @Test
    fun `invoke returns flow of stage values`() = runTest {
        whenever(deviceGateway.observeCalibrationState()).thenReturn(
            flowOf(CalibrationStage.CALIBRATOR_STAGE1, CalibrationStage.CALIBRATOR_STAGE2)
        )
        val stages = useCase().toList()
        assertThat(stages).containsExactly(0, 1)
    }
}
