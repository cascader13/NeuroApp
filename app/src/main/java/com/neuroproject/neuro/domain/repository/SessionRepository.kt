package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.SessionCategory
import kotlinx.coroutines.flow.Flow

/**
 * Доменный контракт для работы с сессиями.
 *
 * Presentation/usecase слои знают только этот интерфейс и доменную модель Session;
 * Room Entity/DAO остаются деталями data-слоя.
 */
interface SessionRepository {
    suspend fun createSession(
        sessionId: Long,
        durationMinutes: Int,
        category: SessionCategory,
        userId: String,
        expeditionId: String
    ): Session

    suspend fun updateSession(session: Session)

    suspend fun getSession(sessionId: Long): Session?

    suspend fun getSessionBySessionId(sessionId: Long): Session?

    suspend fun getSessionsByUserId(UserId: String): List<Session>

    suspend fun getSessions(): List<Session>

    suspend fun deleteSession(sessionId: Long)

    fun observeSessions(): Flow<List<Session>>

    suspend fun finishSession(
        sessionId: Long,
        endTime: Long,
        comment: String?,
        passedPrematurely: Boolean
    )
}
