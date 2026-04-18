package com.neuroproject.neuro.domain.usecase

import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.toDomain
import com.neuroproject.neuro.data.session.SessionDao
import javax.inject.Inject

/**
 * UseCase для получения списка всех сессий
 * 
 * Отвечает за:
 * - Загрузку сессий из базы данных
 * - Конвертацию Entity в Domain Model
 * - Обработку ошибок
 * 
 * Почему это UseCase а не часть Repository:
 * - Это конкретное бизнес-действие ("получить сессии для истории")
 * - Оно может быть переиспользовано разными ViewModel
 * - Легко добавить кэширование, пагинацию, фильтрацию в будущем
 * 
 * Пример использования:
 * ```kotlin
 * class HistoryViewModel @Inject constructor(
 *     private val getSessions: GetSessionsUseCase
 * ) : ViewModel() {
 *     fun loadSessions() {
 *         viewModelScope.launch {
 *             val result = getSessions()
 *             // обработка result
 *         }
 *     }
 * }
 * ```
 */
class GetSessionsUseCase @Inject constructor(
    private val sessionDao: SessionDao
) {
    /**
     * Загрузка всех сессий из базы данных
     * 
     * @return Result со списком Session или ошибкой
     */
    suspend operator fun invoke(): Result<List<Session>> {
        return try {
            val entities = sessionDao.getAllSessions()
            val domainModels = entities.map { it.toDomain() }
            Result.Success(domainModels)
        } catch (e: Exception) {
            Result.Error(e, "Не удалось загрузить сессии: ${e.message}")
        }
    }
}
