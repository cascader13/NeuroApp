package com.neuroproject.neuro.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.repository.MetricsRepository
import com.neuroproject.neuro.domain.repository.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteSessionUseCaseTest {

    private lateinit var useCase: DeleteSessionUseCase

    private val sessionRepository: SessionRepository = mock()
    private val metricsRepository: MetricsRepository = mock()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        useCase = DeleteSessionUseCase(
            sessionRepository = sessionRepository,
            metricsRepository = metricsRepository
        )
    }

    @Test
    fun `given valid sessionId when invoke then deletes from both repositories and returns Success`() = runTest {
        val sessionId = 12345L
        val result = useCase.invoke(sessionId)
        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(sessionRepository).deleteSession(sessionId)
        verify(metricsRepository).clearAllMetricsBySessionId(sessionId)
    }

    @Test
    fun `given sessionRepository throws exception when invoke then returns Error`() = runTest {
        val sessionId = 12345L
        val exception = RuntimeException("Session delete failed")

        whenever(sessionRepository.deleteSession(sessionId)).thenThrow(exception)
        val result = useCase.invoke(sessionId)
        assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = result as Result.Error
        assertThat(error.message).contains("Не удалось удалить сессию")
        assertThat(error.exception).isEqualTo(exception)
    }

    @Test
    fun `given metricsRepository throws exception when invoke then returns Error`() = runTest {
        val sessionId = 12345L
        val exception = RuntimeException("Metrics clear failed")

        // sessionRepository отрабатывает нормально
        whenever(metricsRepository.clearAllMetricsBySessionId(sessionId)).thenThrow(exception)
        val result = useCase.invoke(sessionId)
        assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = result as Result.Error
        assertThat(error.message).contains("Не удалось удалить сессию")
    }
}