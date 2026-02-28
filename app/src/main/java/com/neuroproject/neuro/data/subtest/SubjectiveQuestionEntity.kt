package com.neuroproject.neuro.data.subtest

import androidx.room.Entity
import androidx.room.PrimaryKey


enum class BlockType {
    COGNITIVE,
    EMOTIONAL,
    PHYSICAL
}
@Entity(tableName = "subjective_questions")
data class SubjectiveQuestionEntity(
    @PrimaryKey
    val id: Int,
    val text: String,
    val blockType: BlockType,
    val displayOrder: Int, // порядок отображения
    val isReversed: Boolean,

    val minValue: Int = 1,
    val maxValue: Int = 10
)