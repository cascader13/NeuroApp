package com.neuroproject.neuro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность индексов продуктивности.
 */
@Entity(tableName = "productivity_indexes", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class ProductivityIndexesEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val relaxation: String,
    val stress: String,
    val gravityBaseline: Float,
    val productivityBaseline: Float,
    val fatigueBaseline: Float,
    val reverseFatigueBaseline: Float,
    val relaxationBaseline: Float,
    val concentrationBaseline: Float,
    val hasArtifacts: Boolean,
    val isMarked: Boolean
)

/**
 * Сущность метрик продуктивности.
 */
@Entity(tableName = "productivity_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class ProductivityMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val gravity: Float,
    val productivity: Float,
    val fatigue: Float,
    val reverseFatigue: Float,
    val relaxation: Float,
    val concentration: Float,
    val isMarked: Boolean
)

/**
 * Сущность базовых значений продуктивности.
 */
@Entity(tableName = "productivity_baselines", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class ProductivityBaselinesEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val gravity: Float,
    val productivity: Float,
    val fatigue: Float,
    val reverseFatigue: Float,
    val relaxation: Float,
    val concentration: Float,
    val isMarked: Boolean
)

/**
 * Сущность сжатых метрик продуктивности.
 */
@Entity(tableName = "productivity_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class ProductivityMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val gravity: Float,
    val productivity: Float,
    val fatigue: Float,
    val reverseFatigue: Float,
    val relaxation: Float,
    val concentration: Float,
    val isMarked: Boolean
)
