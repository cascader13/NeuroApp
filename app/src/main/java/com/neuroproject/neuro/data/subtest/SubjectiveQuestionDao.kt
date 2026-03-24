package com.neuroproject.neuro.data.subtest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO для работы с вопросами субъективного тестирования
 *
 * Предоставляет методы для получения вопросов теста, их инициализации
 * и удаления ответов при необходимости.
 *
 * ## Инициализация данных:
 * При первом запуске приложения вопросы загружаются из [SubjectiveQuestionsProvider]
 * через метод [insertAll]. При последующих запусках вопросы читаются из базы данных.
 *
 * ## Использование:
 * ```kotlin
 * class SubjectiveTestViewModel @Inject constructor(
 *     private val questionDao: SubjectiveQuestionDao
 * ) : ViewModel() {
 *
 *     val questions = questionDao.getAll().asLiveData()
 *
 *     fun getCognitiveQuestions(): LiveData<List<SubjectiveQuestionEntity>> {
 *         return questionDao.getByCategory(BlockType.COGNITIVE).asLiveData()
 *     }
 * }
 * ```
 *
 * @see SubjectiveQuestionEntity
 * @see BlockType
 */
@Dao
interface SubjectiveQuestionDao {

    /**
     * Получение всех вопросов теста
     *
     * Вопросы возвращаются в порядке [displayOrder] для правильного отображения в UI.
     *
     * @return Список всех вопросов в порядке отображения
     */
    @Query("SELECT * FROM subjective_questions ORDER BY displayOrder")
    suspend fun getAll(): List<SubjectiveQuestionEntity>

    /**
     * Инициализация базы данных вопросами
     *
     * Использует стратегию [OnConflictStrategy.REPLACE] - если вопрос с таким ID
     * уже существует, он будет заменен новым (полезно для обновления вопросов).
     *
     * @param questions Список вопросов для вставки
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<SubjectiveQuestionEntity>)

    /**
     * Получение вопросов по категории
     *
     * Возвращает вопросы только указанного блока (когнитивный, эмоциональный или физический).
     *
     * @param blockType Тип блока для фильтрации
     * @return Список вопросов указанной категории в порядке отображения
     */
    @Query("SELECT * FROM subjective_questions WHERE blockType = :blockType ORDER BY displayOrder")
    suspend fun getByCategory(blockType: BlockType): List<SubjectiveQuestionEntity>

    /**
     * Удаление ответов для конкретной сессии
     *
     * Используется при:
     * - Отмене тестирования
     * - Очистке данных перед повторным прохождением
     * - Синхронизации с сервером
     *
     * @param sessionId ID сессии, ответы которой нужно удалить
     */
    @Query("DELETE FROM subjective_answers WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)
}