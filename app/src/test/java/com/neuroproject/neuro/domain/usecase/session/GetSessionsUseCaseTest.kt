package com.neuroproject.neuro.domain.usecase.session

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.SessionRepository
import com.neuroproject.neuro.domain.usecase.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GetSessionsUseCaseTest {

    private lateinit var useCase: GetSessionsUseCase

    private val sessionRepository: SessionRepository = mock()
    private val authRepository: AuthRepository = mock()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        useCase = GetSessionsUseCase(
            sessionRepository = sessionRepository,
            authRepository = authRepository
        )
    }

    @Test
    fun `given valid userId when invoke then returns success with sessions`() = runTest {
        // Given
        val userId = "user_123"
        val expectedSessions = listOf(
            Session(
                sessionId = 1_728_000_000L,
                userId = userId,
                expeditionId = "exp_001",
                startTime = 1_728_000_000L,
                durationMinutes = 15,
                category = SessionCategory.H6_9,
                comment = "Хорошая сессия",
                passedPrematurely = false
            ),
            Session(
                sessionId = 1_728_001_200L,
                userId = userId,
                expeditionId = "exp_001",
                startTime = 1_728_001_200L,
                durationMinutes = 10,
                category = SessionCategory.H12_15
            )
        )

        whenever(authRepository.getUserId()).thenReturn(userId)
        whenever(sessionRepository.getSessionsByUserId(userId)).thenReturn(expectedSessions)

        // When
        val result = useCase.invoke()

        // Then
        Truth.assertThat(result).isInstanceOf(Result.Success::class.java)
        Truth.assertThat((result as Result.Success).data).isEqualTo(expectedSessions)
    }

    @Test
    fun `given empty userId when invoke then returns Error`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("")

        val result = useCase.invoke()

        Truth.assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = result as Result.Error
        Truth.assertThat(error.message).isEqualTo("Пользователь не авторизован")
    }

    @Test
    fun `given repository throws exception when invoke then returns Error`() = runTest {
        val userId = "user_123"
        val exception = RuntimeException("Database connection failed")

        whenever(authRepository.getUserId()).thenReturn(userId)
        whenever(sessionRepository.getSessionsByUserId(userId)).thenThrow(exception)

        val result = useCase.invoke()

        Truth.assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = result as Result.Error
        Truth.assertThat(error.message).contains("Не удалось загрузить сессии")
    }
}
