package com.neuroproject.neuro.domain.usecase.calibration

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.CalibrationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CheckPreviousCalibrationUseCaseTest {

    private lateinit var useCase: CheckPreviousCalibrationUseCase
    private val calibrationRepository: CalibrationRepository = mock()
    private val authRepository: AuthRepository = mock()

    @Before
    fun setup() {
        useCase = CheckPreviousCalibrationUseCase(calibrationRepository, authRepository)
    }

    @Test
    fun `given blank userId when invoke then returns false`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("")
        val result = useCase()
        assertThat(result).isFalse()
    }

    @Test
    fun `given has previous calibration when invoke then returns true`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(calibrationRepository.hasPreviousCalibration("user1")).thenReturn(true)
        val result = useCase()
        assertThat(result).isTrue()
    }

    @Test
    fun `given no previous calibration when invoke then returns false`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(calibrationRepository.hasPreviousCalibration("user1")).thenReturn(false)
        val result = useCase()
        assertThat(result).isFalse()
    }
}
