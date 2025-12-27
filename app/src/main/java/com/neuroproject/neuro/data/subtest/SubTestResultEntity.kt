package com.neuroproject.neuro.data.subtest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subtest_results")
data class SubTestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalScore: Int,
    val cognitiveScore: Int,
    val emotionalScore: Int,
    val physicalScore: Int,
    val answersJson: String
)
