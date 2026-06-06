package com.neuroproject.neuro.data.subtest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

data class SubjectiveAnswerValue(
    val value: Int
)

/**
 * DAO для работы с ответами субъективного тестирования
 *
 * Предоставляет методы для сохранения и получения ответов пользователя
 * на вопросы субъективного тестирования.
 *
 * ## Использование:
 * ```kotlin
 * class SubjectiveTestRepository @Inject constructor(
 *     private val answerDao: SubjectiveAnswerDao,
 *     private val questionDao: SubjectiveQuestionDao
 * ) {
 *     suspend fun saveAnswers(sessionId: Long, answers: Map<Int, Int>) {
 *         answers.forEach { (questionId, value) ->
 *             answerDao.insert(SubjectiveAnswerEntity(
 *                 sessionId = sessionId,
 *                 questionId = questionId,
 *                 value = value
 *             ))
 *         }
 *     }
 *
 *     suspend fun getAnswersBySession(sessionId: Long): List<SubjectiveAnswerEntity> {
 *         return answerDao.getBySession(sessionId)
 *     }
 * }
 * ```
 *
 * @see SubjectiveAnswerEntity
 */
@Dao
interface SubjectiveAnswerDao {

    /**
     * Сохранение ответа на вопрос
     *
     * @param answer Объект ответа для сохранения
     */
    @Insert
    suspend fun insert(answer: SubjectiveAnswerEntity)


    @Insert
    suspend fun insertAll(entities: List<SubjectiveAnswerEntity>)

    /**
     * Получение всех ответов для конкретной сессии
     *
     * Используется для:
     * - Отображения результатов тестирования
     * - Подготовки данных для отправки на сервер
     * - Расчета итоговых индексов сессии
     *
     * @param sessionId ID сессии
     * @return Список ответов для указанной сессии
     */
    @Query("SELECT * FROM subjective_answers WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: Long): List<SubjectiveAnswerEntity>

    @Query("""
    SELECT sa.value 
    FROM subjective_answers sa 
    LEFT JOIN sessions ss ON sa.sessionId = ss.sessionId  
    WHERE sa.questionId = :id AND ss.id = :userId 
    ORDER BY sa.sessionId DESC 
    LIMIT 1
""")
    suspend fun getLastById(id: Int, userId: String): SubjectiveAnswerValue?
}