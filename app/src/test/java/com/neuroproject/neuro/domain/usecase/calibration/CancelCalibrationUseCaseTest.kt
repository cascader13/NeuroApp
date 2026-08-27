package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class CancelCalibrationUseCaseTest {

    private lateinit var useCase: CancelCalibrationUseCase
    private val deviceGateway: CapsuleDeviceGateway = mock()

    @Before
    fun setup() {
        useCase = CancelCalibrationUseCase(deviceGateway)
    }

    @Test
    fun `invoke calls stopSignalAndHR on deviceGateway`() = runTest {
        useCase()
        verify(deviceGateway).stopSignalAndHR()
    }
}
