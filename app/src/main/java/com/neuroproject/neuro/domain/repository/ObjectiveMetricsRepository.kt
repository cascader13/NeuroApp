package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.FatigueResult
import com.neuroproject.neuro.domain.model.MinuteFatigueData

/**
 * Репозиторий объективных метрик утомления.
 *
 * Хранит агрегированные данные по минутам сессии и результаты
 * расчёта показателей утомления (когнитивный, физиологический, психологический).
 *
 * Используется для расчёта объективных показателей в [CalculateObjectiveFatigueUseCase].
 */
interface ObjectiveMetricsRepository {
    /**
     * Возвращает метрики за конкретную минуту сессии.
     *
     * @param sessionId ID сессии.
     * @param minuteIndex номер минуты (с 0).
     * @return [MinuteFatigueData] или null, если данные отсутствуют.
     */
    suspend fun getMinuteMetrics(sessionId: Long, minuteIndex: Int): MinuteFatigueData?

    /**
     * Возвращает метрики за все минуты сессии.
     *
     * @param sessionId ID сессии.
     * @return список [MinuteFatigueData], отсортированный по минутам.
     */
    suspend fun getAllMinuteMetrics(sessionId: Long): List<MinuteFatigueData>

    /**
     * Сохраняет результат расчёта утомления за минуту.
     *
     * @param result [FatigueResult] с индексами за минуту.
     */
    suspend fun saveMinuteFatigueResult(result: FatigueResult)

    /**
     * Возвращает все результаты утомления для сессии.
     *
     * @param sessionId ID сессии.
     */
    suspend fun getSessionFatigueResults(sessionId: Long): List<FatigueResult>

    /**
     * Возвращает количество минут с доступными данными.
     *
     * @param sessionId ID сессии.
     */
    suspend fun getAvailableMinutesCount(sessionId: Long): Int
}