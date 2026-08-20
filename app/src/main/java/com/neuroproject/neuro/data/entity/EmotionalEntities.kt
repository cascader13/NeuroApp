package com.neuroproject.neuro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность эмоциональных метрик.
 */
@Entity(tableName = "emotional_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class EmotionalMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val attention: Float,
    val relaxation: Float,
    val cognitiveLoad: Float,
    val cognitiveControl: Float,
    val selfControl: Float,
    val isMarked: Boolean
)

/**
 * Сущность сжатых эмоциональных метрик.
 */
@Entity(tableName = "emotional_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class EmotionalMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val attention: Float,
    val relaxation: Float,
    val cognitiveLoad: Float,
    val cognitiveControl: Float,
    val selfControl: Float,
    val isMarked: Boolean
)
