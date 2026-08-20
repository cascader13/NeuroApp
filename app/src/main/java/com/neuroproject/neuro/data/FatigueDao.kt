package com.neuroproject.neuro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.neuroproject.neuro.data.entity.FatigueResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с результатами расчёта утомления.
 *
 * Хранит результаты по минутам для каждой сессии.
 */
@Dao
interface FatigueDao {

    /** Вставить результат расчёта утомления */
    @Insert
    suspend fun insertFatiqueResult(result: FatigueResultEntity)

    /** Получить результаты утомления для сессии (реактивный поток) */
    @Query("SELECT * FROM fatigue_results WHERE sessionId = :sessionId ORDER BY minuteIndex ASC")
    fun getFatigueResultsForSession(sessionId: Long): Flow<List<FatigueResultEntity>>

    /** Получить результаты утомления для сессии (одноразово) */
    @Query("SELECT * FROM fatigue_results WHERE sessionId = :sessionId ORDER BY minuteIndex ASC")
    suspend fun getFatigueResultsForSessionSync(sessionId: Long): List<FatigueResultEntity>

    /** Удалить все результаты утомления для сессии */
    @Query("DELETE FROM fatigue_results WHERE sessionId = :sessionId")
    suspend fun deleteFatigueResultsForSession(sessionId: Long)
}