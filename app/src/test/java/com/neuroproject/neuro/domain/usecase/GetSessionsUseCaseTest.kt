package com.neuroproject.neuro.domain.usecase

import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetSessionsUseCaseTest {

    @Test
    fun invoke_returnsSessionsFromDomainRepository() = runBlocking {
        // UseCase должен работать с SessionRepository, а не с Room DAO — это граница clean architecture.
        val sessions = listOf(Session(sessionId = 1L, durationMinutes = 10, category = SessionCategory.MORNING))
        val useCase = GetSessionsUseCase(FakeSessionRepository(sessions))

        val result = useCase()

        assertTrue(result is Result.Success)
        assertEquals(sessions, (result as Result.Success).data)
    }

    @Test
    fun invoke_wrapsRepositoryExceptionIntoResultError() = runBlocking {
        // UI получает типизированную ошибку Result.Error вместо падения корутины.
        val useCase = GetSessionsUseCase(FakeSessionRepository(error = IllegalStateException("db is closed")))

        val result = useCase()

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.orEmpty().contains("Не удалось загрузить сессии"))
    }

    private class FakeSessionRepository(
        private val sessions: List<Session> = emptyList(),
        private val error: Throwable? = null
    ) : SessionRepository {
        override suspend fun createSession(sessionId: Long, durationMinutes: Int, category: SessionCategory, userId: String, expeditionId: String): Session =
            Session(sessionId = sessionId, durationMinutes = durationMinutes, category = category, userId = userId, expeditionId = expeditionId)

        override suspend fun updateSession(session: Session) = Unit
        override suspend fun getSession(sessionId: Long): Session? = sessions.firstOrNull { it.sessionId == sessionId }
        override suspend fun getSessionBySessionId(sessionId: Long): Session? = getSession(sessionId)
        override suspend fun getSessions(): List<Session> {
            error?.let { throw it }
            return sessions
        }
        override fun observeSessions(): Flow<List<Session>> = flowOf(sessions)
        override suspend fun finishSession(sessionId: Long, endTime: Long, comment: String?, passedPrematurely: Boolean) = Unit
    }
}
