package com.neuroproject.neuro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fatigue_results",
    indices = [Index(value = ["sessionId"])]
)
data class FatigueResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val minuteIndex: Int,
    val cognitiveResult: Float,
    val physiologicalResult: Float,
    val psychologicalResult: Float
)
