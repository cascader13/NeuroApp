package com.neuroproject.neuro.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.neuroproject.neuro.domain.model.CalibrationSample


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
    val upperFrequency: Float,
    val productivityGravity: Float? = null,
    val productivityProductivity: Float? = null,
    val productivityFatigue: Float? = null,
    val productivityReverseFatigue: Float? = null,
    val productivityRelaxation: Float? = null,
    val productivityConcentration: Float? = null,
    val physiologicalAlpha: Float? = null,
    val physiologicalBeta: Float? = null,
    val physiologicalAlphaGravity: Float? = null,
    val physiologicalBetaGravity: Float? = null,
    val physiologicalConcentration: Float? = null
) {
    companion object {
        fun fromDomain(userId: String, data: com.neuroproject.neuro.domain.model.CalibrationSample): CalibrationHistoryEntity {
            return CalibrationHistoryEntity(
                user_id = userId,
                individualFrequency = data.individualFrequency,
                individualPeakFrequency = data.individualPeakFrequency,
                individualPeakFrequencyPower = data.individualPeakFrequencyPower,
                individualPeakFrequencySuppression = data.individualPeakFrequencySuppression,
                individualBandwidth = data.individualBandwidth,
                individualNormalizedPower = data.individualNormalizedPower,
                lowerFrequency = data.lowerFrequency,
                upperFrequency = data.upperFrequency,
                productivityGravity = data.productivityGravity,
                productivityProductivity = data.productivityProductivity,
                productivityFatigue = data.productivityFatigue,
                productivityReverseFatigue = data.productivityReverseFatigue,
                productivityRelaxation = data.productivityRelaxation,
                productivityConcentration = data.productivityConcentration,
                physiologicalAlpha = data.physiologicalAlpha,
                physiologicalBeta = data.physiologicalBeta,
                physiologicalAlphaGravity = data.physiologicalAlphaGravity,
                physiologicalBetaGravity = data.physiologicalBetaGravity,
                physiologicalConcentration = data.physiologicalConcentration
            )
        }
    }
}

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

@Entity(tableName = "nfb_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class NFBMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val alpha: Float,
    val beta: Float,
    val theta: Float,
    val delta: Float,
    val smr: Float,
    val isMarked: Boolean
)

@Entity(tableName = "nfb_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class NFBMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val alpha: Float,
    val beta: Float,
    val theta: Float,
    val delta: Float,
    val smr: Float,
    val isMarked: Boolean
)

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

@Entity(tableName = "mems_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class MEMSMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float,
    val gyroscopeY: Float,
    val gyroscopeZ: Float,
    val isMarked: Boolean
)

@Entity(tableName = "mems_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class MEMSMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float,
    val gyroscopeY: Float,
    val gyroscopeZ: Float,
    val isMarked: Boolean
)

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