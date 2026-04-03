package com.neuroproject.neuro.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "fatigue_results")
data class FatigueResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val minuteIndex: Int,
    val cognitiveResult:  Float,
    val physioligicalResult: Float,
    val psychologicalResultval : Float
)

@Entity(tableName = "session_fatigue_summary")
data class SessionFatigueSummaryEntity(
    @PrimaryKey val sessionId: Long,
    val averageCognitive: Float,
    val averagePhysiological: Float,
    val averagePsychological: Float,
    val objective_result: Long
)

@Dao
interface FatigueDao {
    @Insert
    suspend fun insertFatiqueResult(result: FatigueResultEntity)

    @Insert
    suspend fun insertSessionSummary(summary: SessionFatigueSummaryEntity)

    @Query("SELECT * FROM fatigue_results WHERE sessionId = :sessionId ORDER BY minuteIndex ASC")
    fun getFatigueResultsForSession(sessionId: Long): Flow<List<FatigueResultEntity>>

    @Query("SELECT * FROM fatigue_results WHERE sessionId = :sessionId ORDER BY minuteIndex ASC")
    suspend fun getFatigueResultsForSessionSync(sessionId: Long): List<FatigueResultEntity>

    @Query("SELECT * FROM session_fatigue_summary WHERE sessionId = :sessionId")
    suspend fun getSessionSummary(sessionId: Long): SessionFatigueSummaryEntity?

    @Query("DELETE FROM fatigue_results WHERE sessionId = :sessionId")
    suspend fun deleteFatigueResultsForSession(sessionId: Long)

    @Query("DELETE FROM session_fatigue_summary WHERE sessionId = :sessionId")
    suspend fun deleteSessionSummary(sessionId: Long)
}