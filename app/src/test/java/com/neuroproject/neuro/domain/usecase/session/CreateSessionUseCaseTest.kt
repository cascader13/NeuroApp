package com.neuroproject.neuro.domain.usecase.session

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CreateSessionUseCaseTest {

    private lateinit var useCase: CreateSessionUseCase

    private val sessionRepository: SessionRepository = mock()
    private val authRepository: AuthRepository = mock()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        useCase = CreateSessionUseCase(sessionRepository, authRepository)
    }

    @Test
    fun `given valid data when invoke then creates session with correct parameters`() = runTest {
        // Given
        val duration = 15
        val category = SessionCategory.H6_9
        val userId = "user_123"
        val expeditionId = "exp_001"
        val expectedSession = Session(
            sessionId = 123456789L,
            userId = userId,
            expeditionId = expeditionId,
            durationMinutes = duration,
            category = category
        )

        whenever(authRepository.getUserId()).thenReturn(userId)
        whenever(authRepository.getExpeditionId()).thenReturn(expeditionId)
        whenever(sessionRepository.createSession(any(), any(), any(), any(), any()))
            .thenReturn(expectedSession)

        // When
        val result = useCase.invoke(durationMinutes = duration, category = category)

        // Then
        assertThat(result).isEqualTo(expectedSession)

        verify(authRepository).getUserId()
        verify(authRepository).getExpeditionId()
        verify(sessionRepository).createSession(
            sessionId = any(),
            durationMinutes = eq(duration),
            category = eq(category),
            userId = eq(userId),
            expeditionId = eq(expeditionId)
        )
    }

    @Test
    fun `given empty userId when invoke then still creates session`() = runTest {
        val duration = 10
        val category = SessionCategory.H12_15

        whenever(authRepository.getUserId()).thenReturn("")
        whenever(authRepository.getExpeditionId()).thenReturn("")

        useCase.invoke(duration, category)

        verify(sessionRepository).createSession(
            any(), eq(duration), eq(category), eq(""), eq("")
        )
    }
}
