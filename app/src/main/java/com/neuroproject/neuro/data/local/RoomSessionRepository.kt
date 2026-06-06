package com.neuroproject.neuro.data.local

import com.neuroproject.neuro.data.mapper.SessionMapper
import com.neuroproject.neuro.data.session.SessionDao
import com.neuroproject.neuro.data.session.SessionEntity
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-реализация SessionRepository.
 *
 * Здесь локализованы все зависимости от Room Entity/DAO. Наружу репозиторий отдает
 * только доменные модели, чтобы use cases и UI не знали о структуре таблиц.
 */
@Singleton
class RoomSessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun createSession(
        sessionId: Long,
        durationMinutes: Int,
        category: SessionCategory,
        userId: String,
        expeditionId: String
    ): Session {
        val entity = SessionEntity(
            sessionId = sessionId,
            id = userId,
            expedition_id = expeditionId,
            durationMinutes = durationMinutes,
            sessionCategory = category,
            endTime = null,
            passingPrematurely = false
        )
        sessionDao.insert(entity)
        return SessionMapper.toDomain(entity)
    }

    override suspend fun updateSession(session: Session) {
        val currentEntity = sessionDao.getSession(session.sessionId) ?: return
        val updatedEntity = SessionMapper.toEntity(session).copy(
            // Сохраняем поля, которые не представлены в domain-модели.
            isMarked = currentEntity.isMarked
        )
        sessionDao.update(updatedEntity)
    }

    override suspend fun getSession(sessionId: Long): Session? {
        return sessionDao.getSession(sessionId)?.let(SessionMapper::toDomain)
    }

    override suspend fun getSessionBySessionId(sessionId: Long): Session? = getSession(sessionId)
    override suspend fun getSessionsByUserId(UserId: String): List<Session> = sessionDao.getSessionsByUserId(UserId).map(SessionMapper::toDomain)


    override suspend fun deleteSession(sessionId: Long) = sessionDao.deleteSession(sessionId)

    override suspend fun getSessions(): List<Session> {
        return sessionDao.getAllSessions().map(SessionMapper::toDomain)
    }


    override fun observeSessions(): Flow<List<Session>> = flow {
        emit(getSessions())
    }

    override suspend fun finishSession(
        sessionId: Long,
        endTime: Long,
        comment: String?,
        passedPrematurely: Boolean
    ) {
        val entity = sessionDao.getSession(sessionId) ?: return
        sessionDao.update(
            entity.copy(
                endTime = endTime,
                comment = comment,
                passingPrematurely = passedPrematurely
            )
        )
    }
}
