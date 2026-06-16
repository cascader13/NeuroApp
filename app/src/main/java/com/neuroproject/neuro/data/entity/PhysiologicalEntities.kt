package com.neuroproject.neuro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "physiological_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class PhysiologicalMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val relax: Float,
    val fatigue: Float,
    val none: Float,
    val concentration: Float,
    val involvement: Float,
    val stress: Float,
    val nfbArtifacts: Boolean,
    val cardioArtifacts: Boolean,
    val isMarked: Boolean
)

@Entity(tableName = "physiological_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class PhysiologicalMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val relax: Float,
    val fatigue: Float,
    val none: Float,
    val concentration: Float,
    val involvement: Float,
    val stress: Float,
    val nfbArtifacts: Boolean,
    val cardioArtifacts: Boolean,
    val isMarked: Boolean
)

@Entity(tableName = "physiological_baselines", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class PhysiologicalBaselinesEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val alpha: Float,
    val beta: Float,
    val alphaGravity: Float,
    val betaGravity: Float,
    val concentration: Float,
    val isMarked: Boolean
)
