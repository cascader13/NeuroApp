package com.neuroproject.neuro.domain.usecase.objective

import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import javax.inject.Inject

/**
 * Получает метрики уtomления для конкретной минуты сессии.
 *
 * @param sessionId идентификатор сессии
 * @param minuteIndex индекс минуты (0-based)
 * @return данные уtomления для указанной минуты или null, если данных нет
 */
class GetMinuteMetricsUseCase @Inject constructor(
    private val repository: ObjectiveMetricsRepository
) {
    suspend operator fun invoke(sessionId: Long, minuteIndex: Int): MinuteFatigueData? {
        return repository.getMinuteMetrics(sessionId, minuteIndex)
    }
}