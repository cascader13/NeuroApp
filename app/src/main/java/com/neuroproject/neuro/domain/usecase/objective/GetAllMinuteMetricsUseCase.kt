package com.neuroproject.neuro.domain.usecase.objective

import com.neuroproject.neuro.domain.model.MinuteFatigueData
import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import javax.inject.Inject

class GetAllMinuteMetricsUseCase @Inject constructor(
    private val repository: ObjectiveMetricsRepository
) {
    suspend operator fun invoke(sessionId: Long): List<MinuteFatigueData> {
        return repository.getAllMinuteMetrics(sessionId)
    }
}