package com.neuroproject.neuro.data.subtest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SubjectiveQuestionDao {
    @Query("SELECT * FROM subjective_questions ORDER BY displayOrder")
    suspend fun getAll(): List<SubjectiveQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<SubjectiveQuestionEntity>)

    @Query("SELECT * FROM subjective_questions WHERE blockType = :blockType ORDER BY displayOrder")
    suspend fun getByCategory(blockType: BlockType): List<SubjectiveQuestionEntity>

    @Query("DELETE FROM subjective_answers WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)
}