package com.neuroproject.neuro.domain.usecase.objective

import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import javax.inject.Inject

class GetMinuteMetricsUseCase @Inject constructor(
    private val repository: ObjectiveMetricsRepository
) {
    suspend operator fun invoke(sessionId: Long, minuteIndex: Int): MinuteFatigueData? {
        return repository.getMinuteMetrics(sessionId, minuteIndex)
    }
}