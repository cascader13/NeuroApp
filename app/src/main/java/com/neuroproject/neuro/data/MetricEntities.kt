package com.neuroproject.neuro.data


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date



@Entity(tableName = "Users")

data class UsersEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user_name : String,
    val user_password : String,
    val user_id : String
)
@Entity(tableName = "nfb_metrics")
data class NFBMetricEntity(
@PrimaryKey val timestamp: Long,
    val id: String,
    val session : java.sql.Timestamp,
    val alpha: Float,
    val beta: Float,
    val theta: Float,
    val delta: Float,
    val smr: Float,
    val isMarked: Boolean
)

@Entity(tableName = "physiological_metrics")
data class PhysiologicalMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val session : java.sql.Timestamp,
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

@Entity(tableName = "mems_metrics")
data class MEMSMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val session : java.sql.Timestamp,
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float,
    val gyroscopeY: Float,
    val gyroscopeZ: Float,
    val isMarked: Boolean
)

@Entity(tableName = "productivity_metrics")
data class ProductivityMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val session : java.sql.Timestamp,
    val gravity: Float,
    val productivity: Float,
    val fatigue: Float,
    val reverseFatigue: Float,
    val relaxation: Float,
    val concentration: Float,
    val isMarked: Boolean
)

@Entity(tableName = "emotional_metrics")
data class EmotionalMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val session : java.sql.Timestamp,
    val attention: Float,
    val relaxation: Float,
    val cognitiveLoad: Float,
    val cognitiveControl: Float,
    val selfControl: Float,
    val isMarked: Boolean
)

@Entity(tableName = "cardio_metrics")
data class CardioMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val session : java.sql.Timestamp,
    val heartRate: Float,
    val hasArtifacts: Boolean,
    val kaplanIndex: Float,
    val metricsAvailable: Boolean,
    val motionArtifacts: Boolean,
    val skinContact: Boolean,
    val stressIndex: Float,
    val isMarked: Boolean
)