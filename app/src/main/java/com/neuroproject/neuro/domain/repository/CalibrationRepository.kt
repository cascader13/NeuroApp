package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.CalibrationSample
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий хранения данных калибровки.
 *
 * Калибровка включает индивидуальные параметры ЭЭГ (частоты, полосы пропускания),
 * а также калибровочные данные продуктивности и физиологии.
 * Данные привязаны к пользователю и могут быть переиспользованы при следующей сессии.
 */
interface CalibrationRepository {
    /**
     * Возвращает последнюю калибровку пользователя.
     *
     * @param userId ID пользователя.
     * @return [CalibrationSample] или null, если калибровки нет.
     */
    suspend fun getPreviousCalibration(userId: String): CalibrationSample?

    /**
     * Сохраняет данные калибровки.
     *
     * @param userId ID пользователя.
     * @param data параметры калибровки.
     */
    suspend fun saveCalibration(userId: String, data: CalibrationSample)

    /**
     * Проверяет, есть ли предыдущая калибровка для пользователя.
     *
     * @param userId ID пользователя.
     * @return true, если калибровка существует.
     */
    suspend fun hasPreviousCalibration(userId: String): Boolean

    suspend fun updateProductivityCalibration(
        userId: String,
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    )

    suspend fun updatePhysiologicalCalibration(
        userId: String,
        alpha: Float,
        beta: Float,
        alphaGravity: Float,
        betaGravity: Float,
        concentration: Float
    )

    /**
     * Наблюдает за этапом калибровки.
     *
     * @return [Flow] с числовым значением этапа (см. [CalibrationStage]).
     */
    fun observeCalibrationStage(): Flow<Int>
}