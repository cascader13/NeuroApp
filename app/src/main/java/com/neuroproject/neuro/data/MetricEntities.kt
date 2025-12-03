package com.neuroproject.neuro.data


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "nfb_metrics")
data class NFBMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val alpha: Float,
    val beta: Float,
    val theta: Float,
    val delta: Float,
    val smr: Float
)

@Entity(tableName = "physiological_metrics")
data class PhysiologicalMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val relax: Float,
    val fatigue: Float,
    val none: Float,
    val concentration: Float,
    val involvement: Float,
    val stress: Float,
    val nfbArtifacts: Boolean,
    val cardioArtifacts: Boolean
)

@Entity(tableName = "mems_metrics")
data class MEMSMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float,
    val gyroscopeY: Float,
    val gyroscopeZ: Float
)

@Entity(tableName = "productivity_metrics")
data class ProductivityMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val gravity: Float,
    val productivity: Float,
    val fatigue: Float,
    val reverseFatigue: Float,
    val relaxation: Float,
    val concentration: Float
)

@Entity(tableName = "emotional_metrics")
data class EmotionalMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val attention: Float,
    val relaxation: Float,
    val cognitiveLoad: Float,
    val cognitiveControl: Float,
    val selfControl: Float
)

@Entity(tableName = "cardio_metrics")
data class CardioMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val heartRate: Float
)