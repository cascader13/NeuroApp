package com.neuroproject.neuro.data.remote

import com.google.gson.annotations.SerializedName

data class UploadRequest(
    // Uncompressed данные
    @SerializedName("nfbMetrics")
    val nfbMetrics: List<NfbMetricDto>? = emptyList(),

    @SerializedName("physiologicalMetrics")
    val physiologicalMetrics: List<PhysiologicalMetricDto>? = emptyList(),

    @SerializedName("eegRawMetrics")
    val EEGRawMetrics: List<EEGRawMetricDto>? = emptyList(),

    @SerializedName("eegProceedMetrics")
    val EEGProceedMetrics: List<EEGProceedMetricDto>? = emptyList(),

    @SerializedName("eegArtifactsMetrics")
    val EEGArtifactsMetrics: List<EEGArtifactMetricDto>? = emptyList(),

    @SerializedName("memsMetrics")
    val memsMetrics: List<MemsMetricDto>? = emptyList(),

    @SerializedName("productivityMetrics")
    val productivityMetrics: List<ProductivityMetricDto>? = emptyList(),

    @SerializedName("emotionalMetrics")
    val emotionalMetrics: List<EmotionalMetricDto>? = emptyList(),

    @SerializedName("cardioMetrics")
    val cardioMetrics: List<CardioMetricDto>? = emptyList(),

    // Compressed данные
    @SerializedName("nfbMetricsCompressed")
    val nfbMetricsCompressed: List<NfbMetricCompressedDto>? = emptyList(),

    @SerializedName("physiologicalMetricsCompressed")
    val physiologicalMetricsCompressed: List<PhysiologicalMetricCompressedDto>? = emptyList(),

    @SerializedName("eegRawMetricsCompressed")
    val EEGRawMetricsCompressed: List<EEGRawMetricCompressedDto>? = emptyList(),

    @SerializedName("eegProceedMetricsCompressed")
    val EEGProceedMetricsCompressed: List<EEGProceedMetricCompressedDto>? = emptyList(),

    @SerializedName("eegArtifactsMetricsCompressed")
    val EEGArtifactsMetricsCompressed: List<EEGArtifactMetricCompressedDto>? = emptyList(),

    @SerializedName("memsMetricsCompressed")
    val memsMetricsCompressed: List<MemsMetricCompressedDto>? = emptyList(),

    @SerializedName("productivityMetricsCompressed")
    val productivityMetricsCompressed: List<ProductivityMetricCompressedDto>? = emptyList(),

    @SerializedName("emotionalMetricsCompressed")
    val emotionalMetricsCompressed: List<EmotionalMetricCompressedDto>? = emptyList(),

    @SerializedName("cardioMetricsCompressed")
    val cardioMetricsCompressed: List<CardioMetricCompressedDto>? = emptyList(),

    // Baseline данные
    @SerializedName("physiologicalBaselines")
    val physiologicalBaseline: List<PhysiologicalBaselineDto>? = emptyList(),

    @SerializedName("productivityBaselines")
    val productivityBaseline: List<ProductivityBaselineDto>? = emptyList(),

    @SerializedName("productivityIndexes")
    val productivityIndex: List<ProductivityIndexDto>? = emptyList()
)

// NFB Metrics
data class NfbMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("alpha")
    val alpha: Double,

    @SerializedName("beta")
    val beta: Double,

    @SerializedName("theta")
    val theta: Double,

    @SerializedName("delta")
    val delta: Double,

    @SerializedName("smr")
    val smr: Double
)

data class NfbMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("alpha")
    val alpha: Double,

    @SerializedName("beta")
    val beta: Double,

    @SerializedName("theta")
    val theta: Double,

    @SerializedName("delta")
    val delta: Double,

    @SerializedName("smr")
    val smr: Double
)

// Physiological Metrics
data class PhysiologicalMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("relax")
    val relax: Double,

    @SerializedName("fatigue")
    val fatigue: Double,

    @SerializedName("none")
    val none: Double,

    @SerializedName("concentration")
    val concentration: Double,

    @SerializedName("involvement")
    val involvement: Double,

    @SerializedName("stress")
    val stress: Double,

    @SerializedName("nfbArtifacts")
    val nfbArtifacts: Int,

    @SerializedName("cardioArtifacts")
    val cardioArtifacts: Int
)

data class PhysiologicalMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("relax")
    val relax: Double,

    @SerializedName("fatigue")
    val fatigue: Double,

    @SerializedName("none")
    val none: Double,

    @SerializedName("concentration")
    val concentration: Double,

    @SerializedName("involvement")
    val involvement: Double,

    @SerializedName("stress")
    val stress: Double,

    @SerializedName("nfbArtifacts")
    val nfbArtifacts: Int,

    @SerializedName("cardioArtifacts")
    val cardioArtifacts: Int
)

// EEG Raw Metrics
data class EEGRawMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("channel1")
    val channel1: Float,

    @SerializedName("channel2")
    val channel2: Float
)

data class EEGRawMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("channel1")
    val channel1: Float,

    @SerializedName("channel2")
    val channel2: Float
)

// EEG Proceed Metrics
data class EEGProceedMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("channel1")
    val channel1: Float,

    @SerializedName("channel2")
    val channel2: Float
)

data class EEGProceedMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("channel1")
    val channel1: Float,

    @SerializedName("channel2")
    val channel2: Float
)

// EEG Artifacts Metrics
data class EEGArtifactMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("artifactsChannel1")
    val artifactsChannel1: Boolean,

    @SerializedName("artifactsChannel2")
    val artifactsChannel2: Boolean,

    @SerializedName("qualityChannel1")
    val qualityChannel1: Float,

    @SerializedName("qualityChannel2")
    val qualityChannel2: Float
)

data class EEGArtifactMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("artifactsChannel1")
    val artifactsChannel1: Boolean,

    @SerializedName("artifactsChannel2")
    val artifactsChannel2: Boolean,

    @SerializedName("qualityChannel1")
    val qualityChannel1: Float,

    @SerializedName("qualityChannel2")
    val qualityChannel2: Float
)

// MEMS Metrics
data class MemsMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("accelerometerX")
    val accelerometerX: Double,

    @SerializedName("accelerometerY")
    val accelerometerY: Double,

    @SerializedName("accelerometerZ")
    val accelerometerZ: Double,

    @SerializedName("gyroscopeX")
    val gyroscopeX: Double,

    @SerializedName("gyroscopeY")
    val gyroscopeY: Double,

    @SerializedName("gyroscopeZ")
    val gyroscopeZ: Double
)

data class MemsMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("accelerometerX")
    val accelerometerX: Double,

    @SerializedName("accelerometerY")
    val accelerometerY: Double,

    @SerializedName("accelerometerZ")
    val accelerometerZ: Double,

    @SerializedName("gyroscopeX")
    val gyroscopeX: Double,

    @SerializedName("gyroscopeY")
    val gyroscopeY: Double,

    @SerializedName("gyroscopeZ")
    val gyroscopeZ: Double
)

// Productivity Metrics
data class ProductivityMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("gravity")
    val gravity: Double,

    @SerializedName("productivity")
    val productivity: Double,

    @SerializedName("fatigue")
    val fatigue: Double,

    @SerializedName("reverseFatigue")
    val reverseFatigue: Double,

    @SerializedName("relaxation")
    val relaxation: Double,

    @SerializedName("concentration")
    val concentration: Double
)

data class ProductivityMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("gravity")
    val gravity: Double,

    @SerializedName("productivity")
    val productivity: Double,

    @SerializedName("fatigue")
    val fatigue: Double,

    @SerializedName("reverseFatigue")
    val reverseFatigue: Double,

    @SerializedName("relaxation")
    val relaxation: Double,

    @SerializedName("concentration")
    val concentration: Double
)

// Emotional Metrics
data class EmotionalMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("attention")
    val attention: Double,

    @SerializedName("relaxation")
    val relaxation: Double,

    @SerializedName("cognitiveLoad")
    val cognitiveLoad: Double,

    @SerializedName("cognitiveControl")
    val cognitiveControl: Double,

    @SerializedName("selfControl")
    val selfControl: Double
)

data class EmotionalMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("attention")
    val attention: Double,

    @SerializedName("relaxation")
    val relaxation: Double,

    @SerializedName("cognitiveLoad")
    val cognitiveLoad: Double,

    @SerializedName("cognitiveControl")
    val cognitiveControl: Double,

    @SerializedName("selfControl")
    val selfControl: Double
)

// Cardio Metrics
data class CardioMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("heartRate")
    val heartRate: Double,

    @SerializedName("hasArtifacts")
    val hasArtifacts: Int,

    @SerializedName("kaplanIndex")
    val kaplanIndex: Double,

    @SerializedName("metricsAvailable")
    val metricsAvailable: Int,

    @SerializedName("motionArtifacts")
    val motionArtifacts: Int,

    @SerializedName("skinContact")
    val skinContact: Int,

    @SerializedName("stressIndex")
    val stressIndex: Double
)

data class CardioMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("heartRate")
    val heartRate: Double,

    @SerializedName("hasArtifacts")
    val hasArtifacts: Int,

    @SerializedName("kaplanIndex")
    val kaplanIndex: Double,

    @SerializedName("metricsAvailable")
    val metricsAvailable: Int,

    @SerializedName("motionArtifacts")
    val motionArtifacts: Int,

    @SerializedName("skinContact")
    val skinContact: Int,

    @SerializedName("stressIndex")
    val stressIndex: Double
)

// Baseline и Indexes DTO
data class PhysiologicalBaselineDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("alpha")
    val alpha: Double,

    @SerializedName("beta")
    val beta: Double,

    @SerializedName("alphaGravity")
    val alphaGravity: Double,

    @SerializedName("betaGravity")
    val betaGravity: Double,

    @SerializedName("concentration")
    val concentration: Double
)

data class ProductivityBaselineDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("gravity")
    val gravity: Double,

    @SerializedName("productivity")
    val productivity: Double,

    @SerializedName("fatigue")
    val fatigue: Double,

    @SerializedName("reverseFatigue")
    val reverseFatigue: Double,

    @SerializedName("relaxation")
    val relaxation: Double,

    @SerializedName("concentration")
    val concentration: Double
)

data class ProductivityIndexDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("expeditionId")
    val expeditionId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int,

    @SerializedName("relaxation")
    val relaxation: String,

    @SerializedName("stress")
    val stress: String,

    @SerializedName("gravityBaseline")
    val gravityBaseline: Double,

    @SerializedName("productivityBaseline")
    val productivityBaseline: Double,

    @SerializedName("fatigueBaseline")
    val fatigueBaseline: Double,

    @SerializedName("reverseFatigueBaseline")
    val reverseFatigueBaseline: Double,

    @SerializedName("relaxationBaseline")
    val relaxationBaseline: Double,

    @SerializedName("concentrationBaseline")
    val concentrationBaseline: Double,

    @SerializedName("hasArtifacts")
    val hasArtifacts: Boolean
)