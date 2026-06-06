package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.CalibrationSample
import kotlinx.coroutines.flow.Flow

interface CalibrationRepository {
    suspend fun getPreviousCalibration(userId: String): CalibrationSample?
    suspend fun saveCalibration(userId: String, data: CalibrationSample)
    suspend fun hasPreviousCalibration(userId: String): Boolean
    fun observeCalibrationStage(): Flow<Int>
}