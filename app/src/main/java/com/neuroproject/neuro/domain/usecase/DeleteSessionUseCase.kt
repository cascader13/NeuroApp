// domain/usecase/DeleteSessionUseCase.kt
package com.neuroproject.neuro.domain.usecase

import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.repository.MetricsRepository
import com.neuroproject.neuro.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Удаляет сессию и связанные с ней метрики.
 *
 * @param sessionId идентификатор сессии для удаления
 * @return [Result.Success] при успешном удалении, [Result.Error] при ошибке
 */
class DeleteSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val metricsRepository: MetricsRepository
    ) {
    suspend operator fun invoke(sessionId: Long): Result<Unit> {
        return try {
            sessionRepository.deleteSession(sessionId)
            metricsRepository.clearAllMetricsBySessionId(sessionId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Не удалось удалить сессию: ${e.message}")
        }
    }
}