package com.neuroproject.neuro.domain.usecase.session

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.FatigueSummary
import com.neuroproject.neuro.domain.model.ObjectiveFatigueResult
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.domain.model.SubjectiveResult
import com.neuroproject.neuro.domain.model.TotalFatigueResult
import com.neuroproject.neuro.domain.repository.SessionRepository
import com.neuroproject.neuro.domain.usecase.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class FinishSessionUseCaseTest {

    private lateinit var useCase: FinishSessionUseCase
    private val sessionRepository: SessionRepository = mock()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        useCase = FinishSessionUseCase(sessionRepository)
    }

    @Test
    fun `given valid session and fatigue summary when invoke then updates session with correct data`() = runTest {
        // Given
        val originalSession = Session(
            sessionId = 1728000000L,
            userId = "user_123",
            expeditionId = "exp_001",
            startTime = 1728000000L,
            durationMinutes = 15,
            category = SessionCategory.MORNING
        )

        val fatigueSummary = createTestFatigueSummary()

        val comment = "Хорошая концентрация, но усталость к концу"
        val passedPrematurely = false

        val updatedSessionCaptor = argumentCaptor<Session>()

        // When
        useCase.invoke(
            session = originalSession,
            fatigueSummary = fatigueSummary,
            comment = comment,
            passedPrematurely = passedPrematurely
        )

        // Then
        verify(sessionRepository).updateSession(updatedSessionCaptor.capture())

        val updatedSession = updatedSessionCaptor.firstValue

        assertThat(updatedSession.sessionId).isEqualTo(originalSession.sessionId)
        assertThat(updatedSession.endTime).isNotNull()
        assertThat(updatedSession.comment).isEqualTo(comment)
        assertThat(updatedSession.passedPrematurely).isFalse()

        assertThat(updatedSession.subjectiveResult).isEqualTo(fatigueSummary.subjective)
        assertThat(updatedSession.objectiveResult).isEqualTo(fatigueSummary.objective)
        assertThat(updatedSession.totalResult).isEqualTo(fatigueSummary.total)
    }

    @Test
    fun `given passedPrematurely true when invoke then sets flag correctly`() = runTest {
        val session = Session(sessionId = 100L, category = SessionCategory.TECHNICAL)
        val summary = createTestFatigueSummary()

        val captor = argumentCaptor<Session>()

        useCase.invoke(session, summary, null, true)

        verify(sessionRepository).updateSession(captor.capture())
        assertThat(captor.firstValue.passedPrematurely).isTrue()
    }


    private fun createTestFatigueSummary(): FatigueSummary {
        return FatigueSummary(
            subjective = SubjectiveResult(
                cognitiveIndex = 75,
                emotionalIndex = 68,
                physicalIndex = 82,
                averageIndex = 75
            ),
            objective = ObjectiveFatigueResult(
                cognitiveIndex = 62,
                psychologicalIndex = 70,
                physiologicalIndex = 55,
                averageIndex = 62,
                fatigueLevel = "Средний",
                stressLevel = "Низкий"
            ),
            total = TotalFatigueResult(
                cognitiveIndex = 70,
                psychologicalIndex = 69,
                physiologicalIndex = 68,
                averageIndex = 69
            )
        )
    }
}