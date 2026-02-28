package com.neuroproject.neuro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp


@Entity(tableName = "Calibration_History")
data class CalibrationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user_id: String?,
    val individualFrequency: Float,
    val individualPeakFrequency: Float,
    val individualPeakFrequencyPower: Float,
    val individualPeakFrequencySuppression: Float,
    val individualBandwidth: Float,
    val individualNormalizedPower: Float,
    val lowerFrequency: Float,
    val upperFrequency: Float
)

@Entity(tableName = "EEG_Raw_metrics")
data class EEGRawMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val channel1: Float,
    val channel2: Float,
    val isMarked: Boolean
)

@Entity(tableName = "EEG_Raw_metrics_compressed")
data class EEGRawMetricCompressedEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val channel1: Float,
    val channel2: Float,
    val isMarked: Boolean
)

@Entity(tableName = "EEG_Proceed_metrics")
data class EEGProceedMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val channel1: Float,
    val channel2: Float,
    val isMarked: Boolean
)

@Entity(tableName = "EEG_Proceed_metrics_compressed")
data class EEGProceedMetricCompressedEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val channel1: Float,
    val channel2: Float,
    val isMarked: Boolean
)

@Entity(tableName = "EEG_Artifacts_metrics")
data class EEGArtifactsMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val artifactsChannel1: Boolean,
    val artifactsChannel2: Boolean,
    val qualityChannel1: Float,
    val qualityChannel2: Float,
    val isMarked: Boolean
)

@Entity(tableName = "EEG_Artifacts_metrics_compressed")
data class EEGArtifactsMetricCompressedEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val artifactsChannel1: Boolean,
    val artifactsChannel2: Boolean,
    val qualityChannel1: Float,
    val qualityChannel2: Float,
    val isMarked: Boolean
)

@Entity(tableName = "nfb_metrics")
data class NFBMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val alpha: Float,
    val beta: Float,
    val theta: Float,
    val delta: Float,
    val smr: Float,
    val isMarked: Boolean
)

@Entity(tableName = "nfb_metrics_compressed")
data class NFBMetricCompressedEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
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
    val expedition_id: String,
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

@Entity(tableName = "physiological_metrics_compressed")
data class PhysiologicalMetricCompressedEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
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

@Entity(tableName = "physiological_baselines")
data class PhysiologicalBaselinesEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val alpha  : Float,
    val beta: Float,
    val alphaGravity: Float,
    val betaGravity: Float,
    val concentration: Float,
    val isMarked: Boolean
)

@Entity(tableName = "mems_metrics")
data class MEMSMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float,
    val gyroscopeY: Float,
    val gyroscopeZ: Float,
    val isMarked: Boolean
)

@Entity(tableName = "mems_metrics_compressed")
data class MEMSMetricCompressedEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float,
    val gyroscopeY: Float,
    val gyroscopeZ: Float,
    val isMarked: Boolean
)

@Entity(tableName = "productivity_indexes")

data class ProductivityIndexesEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session: java.sql.Timestamp,
    val relaxation: String,
    val stress: String,
    val gravityBaseline: Float,
    val productivityBaseline: Float,
    val fatiqueBaseline: Float,
    val reverseFatiqueBaseline: Float,
    val relaxationBaselines: Float,
    val concentrationBaselines: Float,
    val hasArtifacts: Boolean,
    val isMarked: Boolean
)

@Entity(tableName = "productivity_metrics")
data class ProductivityMetricEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val gravity: Float,
    val productivity: Float,
    val fatigue: Float,
    val reverseFatigue: Float,
    val relaxation: Float,
    val concentration: Float,
    val isMarked: Boolean
)

@Entity(tableName = "productivity_baselines")
data class ProductivityBaselinesEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val gravity: Float,
    val productivity: Float,
    val fatigue: Float,
    val reverseFatigue: Float,
    val relaxation: Float,
    val concentration: Float,
    val isMarked: Boolean
)

@Entity(tableName = "productivity_metrics_compressed")
data class ProductivityMetricCompressedEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
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
    val expedition_id: String,
    val session : java.sql.Timestamp,
    val attention: Float,
    val relaxation: Float,
    val cognitiveLoad: Float,
    val cognitiveControl: Float,
    val selfControl: Float,
    val isMarked: Boolean
)

@Entity(tableName = "emotional_metrics_compressed")
data class EmotionalMetricCompressedEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
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
    val expedition_id: String,
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

@Entity(tableName = "cardio_metrics_compressed")
data class CardioMetricCompressedEntity(
    @PrimaryKey val timestamp: Long,
    val id: String,
    val expedition_id: String,
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