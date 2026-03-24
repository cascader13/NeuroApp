package com.neuroproject.neuro.data.subtest

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для управления вопросами субъективного тестирования
 *
 * Обеспечивает единый доступ к вопросам теста с автоматической инициализацией
 * базы данных при первом использовании.
 *
 * ## Паттерн Lazy Initialization:
 * При первом вызове [getAllQuestions] проверяет наличие вопросов в базе данных.
 * Если база пуста, вопросы загружаются из [SubjectiveQuestionsProvider] и сохраняются.
 *
 * ## Использование:
 * ```kotlin
 * @Singleton
 * class SubjectiveTestViewModel @Inject constructor(
 *     private val repository: SubjectiveQuestionRepository
 * ) : ViewModel() {
 *
 *     suspend fun loadQuestions(): List<SubjectiveQuestionEntity> {
 *         return repository.getAllQuestions()
 *     }
 *
 *     suspend fun getQuestionsByCategory(type: BlockType): List<SubjectiveQuestionEntity> {
 *         return repository.getQuestionsByCategory(type)
 *     }
 * }
 * ```
 *
 * @property dao DAO для работы с вопросами
 *
 * @see SubjectiveQuestionDao
 * @see SubjectiveQuestionsProvider
 */
@Singleton
class SubjectiveQuestionRepository @Inject constructor(
    private val dao: SubjectiveQuestionDao
) {

    /**
     * Получение всех вопросов с автоматической инициализацией
     *
     * При первом вызове проверяет наличие вопросов в БД.
     * Если вопросов нет, загружает их из [SubjectiveQuestionsProvider]
     * и сохраняет в базу данных.
     *
     * @return Список всех вопросов в порядке отображения
     */
    suspend fun getAllQuestions(): List<SubjectiveQuestionEntity> {
        val questions = dao.getAll()
        if (questions.isEmpty()) {
            // Инициализация базы данных при первом запуске
            dao.insertAll(SubjectiveQuestionsProvider.getQuestions())
            return dao.getAll()
        }
        return questions
    }

    /**
     * Получение вопросов по категории с автоматической инициализацией
     *
     * @param blockType Тип блока (когнитивный/эмоциональный/физический)
     * @return Список вопросов указанной категории
     */
    suspend fun getQuestionsByCategory(blockType: BlockType): List<SubjectiveQuestionEntity> {
        // Убеждаемся, что база инициализирована
        getAllQuestions()
        return dao.getByCategory(blockType)
    }
}