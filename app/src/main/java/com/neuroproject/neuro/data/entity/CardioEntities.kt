package com.neuroproject.neuro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность кардиометрических данных.
 */
@Entity(tableName = "cardio_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class CardioMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val heartRate: Float,
    val hasArtifacts: Boolean,
    val kaplanIndex: Float,
    val metricsAvailable: Boolean,
    val motionArtifacts: Boolean,
    val skinContact: Boolean,
    val stressIndex: Float,
    val isMarked: Boolean
)

/**
 * Сущность сжатых кардиометрических данных.
 */
@Entity(tableName = "cardio_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class CardioMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val heartRate: Float,
    val hasArtifacts: Boolean,
    val kaplanIndex: Float,
    val metricsAvailable: Boolean,
    val motionArtifacts: Boolean,
    val skinContact: Boolean,
    val stressIndex: Float,
    val isMarked: Boolean
)
