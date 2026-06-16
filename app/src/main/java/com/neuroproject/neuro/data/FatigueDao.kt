package com.neuroproject.neuro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.neuroproject.neuro.data.entity.FatigueResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FatigueDao {
    @Insert
    suspend fun insertFatiqueResult(result: FatigueResultEntity)

    @Query("SELECT * FROM fatigue_results WHERE sessionId = :sessionId ORDER BY minuteIndex ASC")
    fun getFatigueResultsForSession(sessionId: Long): Flow<List<FatigueResultEntity>>

    @Query("SELECT * FROM fatigue_results WHERE sessionId = :sessionId ORDER BY minuteIndex ASC")
    suspend fun getFatigueResultsForSessionSync(sessionId: Long): List<FatigueResultEntity>

    @Query("DELETE FROM fatigue_results WHERE sessionId = :sessionId")
    suspend fun deleteFatigueResultsForSession(sessionId: Long)
}