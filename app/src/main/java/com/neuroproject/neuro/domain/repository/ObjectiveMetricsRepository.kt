package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.FatigueResult
import com.neuroproject.neuro.domain.model.MinuteFatigueData

interface ObjectiveMetricsRepository {
    suspend fun getMinuteMetrics(sessionId: Long, minuteIndex: Int): MinuteFatigueData?
    suspend fun getAllMinuteMetrics(sessionId: Long): List<MinuteFatigueData>
    suspend fun saveMinuteFatigueResult(result: FatigueResult)
    suspend fun getSessionFatigueResults(sessionId: Long): List<FatigueResult>
    suspend fun getAvailableMinutesCount(sessionId: Long): Int
}