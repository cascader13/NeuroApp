package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.SessionCategory
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий управления сессиями записи данных.
 *
 * Сессия — основная единица работы приложения: запись ЭЭГ, физиологии,
 * субъективных ответов и расчёт показателей утомления.
 *
 * Presentation/usecase слои знают только этот интерфейс и доменную модель Session;
 * Room Entity/DAO остаются деталями data-слоя.
 */
interface SessionRepository {
    /**
     * Создаёт новую сессию.
     *
     * @param sessionId внешний идентификатор (из JNI).
     * @param durationMinutes планируемая длительность в минутах.
     * @param category категория сессии (утро, день, вечер и т.д.).
     * @param userId ID пользователя.
     * @param expeditionId ID экспедиции.
     * @return созданная [Session].
     */
    suspend fun createSession(
        sessionId: Long,
        durationMinutes: Int,
        category: SessionCategory,
        userId: String,
        expeditionId: String
    ): Session

    /** Обновляет данные сессии. */
    suspend fun updateSession(session: Session)

    /** Возвращает сессию по её ID или null, если не найдена. */
    suspend fun getSession(sessionId: Long): Session?

    /** Возвращает сессию по внешнему sessionId (из JNI). */
    suspend fun getSessionBySessionId(sessionId: Long): Session?

    /** Возвращает все сессии пользователя. */
    suspend fun getSessionsByUserId(UserId: String): List<Session>

    /** Возвращает все сессии. */
    suspend fun getSessions(): List<Session>

    /** Вовзращает результат того была ли отправлена сессия */
    suspend fun isMarkedSession(sessionId: Long): Boolean

    /** Удаляет сессию по ID. */
    suspend fun deleteSession(sessionId: Long)

    /** Наблюдает за списком всех сессий (обновления в реальном времени). */
    fun observeSessions(): Flow<List<Session>>

    /**
     * Завершает сессию.
     *
     * @param sessionId ID сессии.
     * @param endTime время окончания (мс).
     * @param comment комментарий пользователя (опционально).
     * @param passedPrematurely true, если сессия завершена досрочно.
     */
    suspend fun finishSession(
        sessionId: Long,
        endTime: Long,
        comment: String?,
        passedPrematurely: Boolean
    )
}
