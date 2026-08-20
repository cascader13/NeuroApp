package com.neuroproject.neuro.domain.usecase.objective

import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import javax.inject.Inject

/**
 * Получает количество доступных минут с данными для сессии.
 *
 * @param sessionId идентификатор сессии
 * @return количество минут с собранными метриками
 */
class GetAvailableMinutesCountUseCase @Inject constructor(
    private val repository: ObjectiveMetricsRepository
) {
    suspend operator fun invoke(sessionId: Long): Int {
        return repository.getAvailableMinutesCount(sessionId)
    }
}