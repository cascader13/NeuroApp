package com.neuroproject.neuro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.neuroproject.neuro.data.entity.*

/**
 * DAO для доступа к данным калибровки пользователя.
 */
@Dao
interface CalibrationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCalibrationData(history: CalibrationHistoryEntity)

    @Query("SELECT *  FROM calibration_history WHERE user_id = :user_id" +
            " AND productivityConcentration IS NOT NULL AND physiologicalAlpha IS NOT NULL " +
            "  ORDER BY id DESC LIMIT 1")
    suspend fun getCalibration(user_id: String): List<CalibrationHistoryEntity>

    @Query("UPDATE calibration_history SET productivityGravity = :gravity," +
            " productivityProductivity = :productivity," +
            " productivityFatigue = :fatigue," +
            " productivityReverseFatigue = :reverseFatigue," +
            " productivityRelaxation = :relaxation," +
            " productivityConcentration = :concentration WHERE user_id = :userId AND id = (SELECT MAX(id) FROM calibration_history WHERE user_id = :userId)")
    suspend fun insertProductivityCalibration(userId: String, gravity: Float, productivity: Float, fatigue: Float, reverseFatigue: Float, relaxation: Float, concentration: Float)

    @Query("UPDATE calibration_history SET physiologicalAlpha = :alpha," +
            " physiologicalBeta = :beta, " +
            "physiologicalAlphaGravity = :alphaGravity, " +
            "physiologicalBetaGravity = :betaGravity, " +
            "physiologicalConcentration = :concentration WHERE user_id = :userId AND id = (SELECT MAX(id) FROM calibration_history WHERE user_id = :userId)")
    suspend fun insertPhysiologicalCalibration(userId: String, alpha: Float, beta: Float, alphaGravity: Float, betaGravity: Float, concentration: Float)

    @Query("SELECT relaxation FROM productivity_indexes WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun getRelaxationValuesBySession(sessionId: Long): List<String>

    @Query("SELECT stress FROM productivity_indexes WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun getStressValuesBySession(sessionId: Long): List<String>
}
