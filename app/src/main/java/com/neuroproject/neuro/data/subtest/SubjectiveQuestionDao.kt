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

    @Query("SELECT * FROM subjective_questions WHERE category = :category ORDER BY displayOrder")
    suspend fun getByCategory(category: String): List<SubjectiveQuestionEntity>
}