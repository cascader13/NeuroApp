package com.neuroproject.neuro.data.subtest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SubjectiveAnswerDao {

    @Insert
    suspend fun insert(answer: SubjectiveAnswerEntity)

    @Query("SELECT * FROM subjective_answers WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: Long): List<SubjectiveAnswerEntity>
}