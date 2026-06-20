package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.SubjectiveAnswer
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий субъективного теста (опросника утомления).
 *
 * Управляет вопросами и ответами пользователя.
 * Вопросы разбиты на блоки: когнитивный, эмоциональный, физический.
 * Ответы привязываются к сессии и используются для расчёта субъективных показателей.
 */
interface SubjectiveTestRepository {
    /** Импортирует вопросы из assets (вызывается при первом запуске или пустой БД). */
    suspend fun importQuestions()

    /** Возвращает список вопросов из БД. */
    suspend fun getQuestions(): List<SubjectiveQuestion>

    /**
     * Сохраняет ответы на вопросы для сессии.
     *
     * @param sessionId ID сессии.
     * @param answers список ответов пользователя.
     */
    suspend fun saveAnswers(sessionId: Long, answers: List<SubjectiveAnswer>)

    /**
     * Сохраняет или обновляет один ответ на вопрос для сессии (upsert).
     *
     * @param sessionId ID сессии.
     * @param answer ответ пользователя.
     */
    suspend fun saveAnswer(sessionId: Long, answer: SubjectiveAnswer)

    /** Возвращает ответы для сессии. */
    suspend fun getAnswers(sessionId: Long): List<SubjectiveAnswer>

    /** Сохраняет комментарий пользователя к сессии. */
    suspend fun saveComment(sessionId: Long, comment: String)

    /**
     * Возвращает последний ответ пользователя на вопрос по его ID.
     *
     * @param id ID вопроса.
     * @param userId ID пользователя.
     * @return оценка (1-10) или null, если ответа нет.
     */
    suspend fun getLastAnswerScoreById(id: Int, userId: String): Int?
}