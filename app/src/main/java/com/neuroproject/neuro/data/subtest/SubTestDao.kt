package com.neuroproject.neuro.data.subtest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SubTestDao {

    @Insert
    suspend fun insert(result: SubTestResultEntity)

    @Query("SELECT * FROM subtest_results ORDER BY timestamp DESC")
    suspend fun getAll(): List<SubTestResultEntity>

    @Query("SELECT * FROM subtest_results WHERE timestamp BETWEEN :from AND :to")
    suspend fun getBetween(from: Long, to: Long): List<SubTestResultEntity>
}
