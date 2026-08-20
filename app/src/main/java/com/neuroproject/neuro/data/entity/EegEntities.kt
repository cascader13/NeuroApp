package com.neuroproject.neuro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность сырых данных ЭЭГ.
 */
@Entity(tableName = "EEG_Raw_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class EEGRawMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val channel1: Float,
    val channel2: Float,
    val isMarked: Boolean
)

/**
 * Сущность сжатых сырых данных ЭЭГ.
 */
@Entity(tableName = "EEG_Raw_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class EEGRawMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val channel1: Float,
    val channel2: Float,
    val isMarked: Boolean
)

/**
 * Сущность обработанных данных ЭЭГ.
 */
@Entity(tableName = "EEG_Proceed_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class EEGProceedMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val channel1: Float,
    val channel2: Float,
    val isMarked: Boolean
)

/**
 * Сущность сжатых обработанных данных ЭЭГ.
 */
@Entity(tableName = "EEG_Proceed_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class EEGProceedMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val channel1: Float,
    val channel2: Float,
    val isMarked: Boolean
)

/**
 * Сущность данных об артефактах ЭЭГ.
 */
@Entity(tableName = "EEG_Artifacts_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class EEGArtifactsMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val artifactsChannel1: Boolean,
    val artifactsChannel2: Boolean,
    val qualityChannel1: Float,
    val qualityChannel2: Float,
    val isMarked: Boolean
)

/**
 * Сущность сжатых данных об артефактах ЭЭГ.
 */
@Entity(tableName = "EEG_Artifacts_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class EEGArtifactsMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val artifactsChannel1: Boolean,
    val artifactsChannel2: Boolean,
    val qualityChannel1: Float,
    val qualityChannel2: Float,
    val isMarked: Boolean
)
