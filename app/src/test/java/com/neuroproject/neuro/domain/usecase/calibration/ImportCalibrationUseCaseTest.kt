package com.neuroproject.neuro.domain.usecase.calibration

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.CalibrationSample
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.CalibrationRepository
import com.neuroproject.neuro.domain.repository.CapsuleDeviceGateway
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ImportCalibrationUseCaseTest {

    private lateinit var useCase: ImportCalibrationUseCase
    private val deviceGateway: CapsuleDeviceGateway = mock()
    private val calibrationRepository: CalibrationRepository = mock()
    private val authRepository: AuthRepository = mock()

    @Before
    fun setup() {
        useCase = ImportCalibrationUseCase(deviceGateway, calibrationRepository, authRepository)
    }

    @Test
    fun `given usePrevious true and data exists when invoke then imports previous`() = runTest {
        val previousData = CalibrationSample()
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(calibrationRepository.getPreviousCalibration("user1")).thenReturn(previousData)

        useCase(usePrevious = true)

        verify(deviceGateway).importCalibration(previousData)
        verify(deviceGateway, never()).startSignalAndHR()
    }

    @Test
    fun `given data with productivity when invoke then imports productivity calibration`() = runTest {
        val previousData = CalibrationSample(
            productivityGravity = 1f,
            productivityProductivity = 2f,
            productivityFatigue = 3f,
            productivityReverseFatigue = 4f,
            productivityRelaxation = 5f,
            productivityConcentration = 6f
        )
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(calibrationRepository.getPreviousCalibration("user1")).thenReturn(previousData)

        useCase(usePrevious = true)

        verify(deviceGateway).importCalibration(previousData)
        verify(deviceGateway).importProductivityCalibration(1f, 2f, 3f, 4f, 5f, 6f)
    }

    @Test
    fun `given data with physiological when invoke then imports physiological calibration`() = runTest {
        val previousData = CalibrationSample(
            physiologicalAlpha = 1f,
            physiologicalBeta = 2f,
            physiologicalAlphaGravity = 3f,
            physiologicalBetaGravity = 4f,
            physiologicalConcentration = 5f
        )
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(calibrationRepository.getPreviousCalibration("user1")).thenReturn(previousData)

        useCase(usePrevious = true)

        verify(deviceGateway).importCalibration(previousData)
        verify(deviceGateway).importPhysiologicalCalibration(1f, 2f, 3f, 4f, 5f)
    }

    @Test
    fun `given data with all fields when invoke then imports all calibration types`() = runTest {
        val previousData = CalibrationSample(
            productivityGravity = 1f,
            productivityProductivity = 2f,
            productivityFatigue = 3f,
            productivityReverseFatigue = 4f,
            productivityRelaxation = 5f,
            productivityConcentration = 6f,
            physiologicalAlpha = 7f,
            physiologicalBeta = 8f,
            physiologicalAlphaGravity = 9f,
            physiologicalBetaGravity = 10f,
            physiologicalConcentration = 11f
        )
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(calibrationRepository.getPreviousCalibration("user1")).thenReturn(previousData)

        useCase(usePrevious = true)

        verify(deviceGateway).importCalibration(previousData)
        verify(deviceGateway).importProductivityCalibration(1f, 2f, 3f, 4f, 5f, 6f)
        verify(deviceGateway).importPhysiologicalCalibration(7f, 8f, 9f, 10f, 11f)
    }

    @Test
    fun `given data without productivity or physiological when invoke then skips extra imports`() = runTest {
        val previousData = CalibrationSample()
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(calibrationRepository.getPreviousCalibration("user1")).thenReturn(previousData)

        useCase(usePrevious = true)

        verify(deviceGateway).importCalibration(previousData)
        verify(deviceGateway, never()).importProductivityCalibration(
            org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any(),
            org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any()
        )
        verify(deviceGateway, never()).importPhysiologicalCalibration(
            org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any(),
            org.mockito.kotlin.any(), org.mockito.kotlin.any()
        )
    }

    @Test
    fun `given usePrevious true but no data when invoke then starts new calibration`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(calibrationRepository.getPreviousCalibration("user1")).thenReturn(null)

        useCase(usePrevious = true)

        verify(deviceGateway, never()).importCalibration(org.mockito.kotlin.any())
        verify(deviceGateway).startSignalAndHR()
    }

    @Test
    fun `given usePrevious false when invoke then starts new calibration`() = runTest {
        useCase(usePrevious = false)

        verify(deviceGateway, never()).importCalibration(org.mockito.kotlin.any())
        verify(deviceGateway).startSignalAndHR()
    }
}
