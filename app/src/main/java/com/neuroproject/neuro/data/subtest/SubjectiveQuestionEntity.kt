package com.neuroproject.neuro.data.subtest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjective_questions")
data class SubjectiveQuestionEntity(
    @PrimaryKey
    val id: Int,  // 1-15
    val text: String,
    val category: String, // "cognitive", "emotional", "physical"
    val displayOrder: Int // порядок отображения
)