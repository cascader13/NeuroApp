package com.neuroproject.neuro.data.remote

import com.google.gson.annotations.SerializedName

data class UploadRequest(
    @SerializedName("cardioMetrics")
    val cardioMetrics: List<CardioMetricDto>? = emptyList(),

    @SerializedName("emotionalMetrics")
    val emotionalMetrics: List<EmotionalMetricDto>? = emptyList(),

    @SerializedName("EEGRawMetrics")
    val EEGRawMetrics: List<EEGRawMetricDto>? = emptyList(),

    @SerializedName("EEGProceedMetrics")
    val EEGProceedMetrics: List<EEGProceedMetricDto>? = emptyList(),

    @SerializedName("EEGArtifactsMetrics")
    val EEGArtifactsMetrics: List<EEGArtifactMetricDto>? = emptyList(),

    @SerializedName("memsMetrics")
    val memsMetrics: List<MemsMetricDto>? = emptyList(),

    @SerializedName("nfbMetrics")
    val nfbMetrics: List<NfbMetricDto>? = emptyList(),

    @SerializedName("physiologicalMetrics")
    val physiologicalMetrics: List<PhysiologicalMetricDto>? = emptyList(),

    @SerializedName("productivityMetrics")
    val productivityMetrics: List<ProductivityMetricDto>? = emptyList(),

    // Новые поля для compressed данных
    @SerializedName("nfbMetricsCompressed")
    val nfbMetricsCompressed: List<NfbMetricCompressedDto>? = emptyList(),

    @SerializedName("physiologicalMetricsCompressed")
    val physiologicalMetricsCompressed: List<PhysiologicalMetricCompressedDto>? = emptyList(),

    @SerializedName("memsMetricsCompressed")
    val memsMetricsCompressed: List<MemsMetricCompressedDto>? = emptyList(),

    @SerializedName("productivityMetricsCompressed")
    val productivityMetricsCompressed: List<ProductivityMetricCompressedDto>? = emptyList(),

    @SerializedName("emotionalMetricsCompressed")
    val emotionalMetricsCompressed: List<EmotionalMetricCompressedDto>? = emptyList(),

    @SerializedName("cardioMetricsCompressed")
    val cardioMetricsCompressed: List<CardioMetricCompressedDto>? = emptyList(),

    @SerializedName("EEGRawMetricsCompressed")
    val EEGRawMetricsCompressed: List<EEGRawMetricCompressedDto>? = emptyList(),

    @SerializedName("EEGProceedMetricsCompressed")
    val EEGProceedMetricsCompressed: List<EEGProceedMetricCompressedDto>? = emptyList(),

    @SerializedName("EEGArtifactsMetricsCompressed")
    val EEGArtifactsMetricsCompressed: List<EEGArtifactMetricCompressedDto>? = emptyList()
)

// DTO для compressed версий (можно добавить в этот же файл)

data class NfbMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("alpha")
    val alpha: Double? = null,

    @SerializedName("beta")
    val beta: Double? = null,

    @SerializedName("theta")
    val theta: Double? = null,

    @SerializedName("delta")
    val delta: Double? = null,

    @SerializedName("smr")
    val smr: Double? = null
)

data class PhysiologicalMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("relax")
    val relax: Double? = null,

    @SerializedName("fatigue")
    val fatigue: Double? = null,

    @SerializedName("none")
    val none: Double? = null,

    @SerializedName("concentration")
    val concentration: Double? = null,

    @SerializedName("involvement")
    val involvement: Double? = null,

    @SerializedName("stress")
    val stress: Double? = null,

    @SerializedName("nfbArtifacts")
    val nfbArtifacts: Int? = null,

    @SerializedName("cardioArtifacts")
    val cardioArtifacts: Int? = null
)

data class EEGRawMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("channel1")
    val channel1: Float,

    @SerializedName("channel2")
    val channel2: Float
)

data class EEGProceedMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("channel1")
    val channel1: Float,

    @SerializedName("channel2")
    val channel2: Float
)

data class EEGArtifactMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("artifactsChannel1")
    val artifactsChannel1: Boolean,

    @SerializedName("artifactsChannel2")
    val artifactsChannel2: Boolean,

    @SerializedName("qualityChannel1")
    val qualityChannel1: Float,

    @SerializedName("qualityChannel2")
    val qualityChannel2: Float
)

data class MemsMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("accelerometerX")
    val accelerometerX: Double? = null,

    @SerializedName("accelerometerY")
    val accelerometerY: Double? = null,

    @SerializedName("accelerometerZ")
    val accelerometerZ: Double? = null,

    @SerializedName("gyroscopeX")
    val gyroscopeX: Double? = null,

    @SerializedName("gyroscopeY")
    val gyroscopeY: Double? = null,

    @SerializedName("gyroscopeZ")
    val gyroscopeZ: Double? = null
)

data class ProductivityMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("gravity")
    val gravity: Double? = null,

    @SerializedName("productivity")
    val productivity: Double? = null,

    @SerializedName("fatigue")
    val fatigue: Double? = null,

    @SerializedName("reverseFatigue")
    val reverseFatigue: Double? = null,

    @SerializedName("relaxation")
    val relaxation: Double? = null,

    @SerializedName("concentration")
    val concentration: Double? = null
)

data class EmotionalMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("attention")
    val attention: Double? = null,

    @SerializedName("relaxation")
    val relaxation: Double? = null,

    @SerializedName("cognitiveLoad")
    val cognitiveLoad: Double? = null,

    @SerializedName("cognitiveControl")
    val cognitiveControl: Double? = null,

    @SerializedName("selfControl")
    val selfControl: Double? = null
)

data class CardioMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("heartRate")
    val heartRate: Double? = null,

    @SerializedName("hasArtifacts")
    val hasArtifacts: Int? = null,

    @SerializedName("kaplanIndex")
    val kaplanIndex: Double? = null,

    @SerializedName("metricsAvailable")
    val metricsAvailable: Int? = null,

    @SerializedName("motionArtifacts")
    val motionArtifacts: Int? = null,

    @SerializedName("skinContact")
    val skinContact: Int? = null,

    @SerializedName("stressIndex")
    val stressIndex: Double? = null
)

/**
 * DTO для кардио метрик (оригинал)
 */
data class CardioMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("heartRate")
    val heartRate: Double? = null,

    @SerializedName("hasArtifacts")
    val hasArtifacts: Int? = null,

    @SerializedName("kaplanIndex")
    val kaplanIndex: Double? = null,

    @SerializedName("metricsAvailable")
    val metricsAvailable: Int? = null,

    @SerializedName("motionArtifacts")
    val motionArtifacts: Int? = null,

    @SerializedName("skinContact")
    val skinContact: Int? = null,

    @SerializedName("stressIndex")
    val stressIndex: Double? = null
)

/**
 * DTO для эмоциональных метрик (оригинал)
 */
data class EmotionalMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("attention")
    val attention: Double? = null,

    @SerializedName("relaxation")
    val relaxation: Double? = null,

    @SerializedName("cognitiveLoad")
    val cognitiveLoad: Double? = null,

    @SerializedName("cognitiveControl")
    val cognitiveControl: Double? = null,

    @SerializedName("selfControl")
    val selfControl: Double? = null
)

/**
 * DTO для EEG RAW метрик (оригинал)
 */
data class EEGRawMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("channel1")
    val channel1: Float,

    @SerializedName("channel2")
    val channel2: Float
)

/**
 * DTO для EEG PROCEED метрик (оригинал)
 */
data class EEGProceedMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("channel1")
    val channel1: Float,

    @SerializedName("channel2")
    val channel2: Float
)

/**
 * DTO для EEG artifact метрик (оригинал)
 */
data class EEGArtifactMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("artifactsChannel1")
    val artifactsChannel1: Boolean,

    @SerializedName("artifactsChannel2")
    val artifactsChannel2: Boolean,

    @SerializedName("qualityChannel1")
    val qualityChannel1: Float,

    @SerializedName("qualityChannel2")
    val qualityChannel2: Float
)

/**
 * DTO для MEMS метрик (оригинал)
 */
data class MemsMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("accelerometerX")
    val accelerometerX: Double? = null,

    @SerializedName("accelerometerY")
    val accelerometerY: Double? = null,

    @SerializedName("accelerometerZ")
    val accelerometerZ: Double? = null,

    @SerializedName("gyroscopeX")
    val gyroscopeX: Double? = null,

    @SerializedName("gyroscopeY")
    val gyroscopeY: Double? = null,

    @SerializedName("gyroscopeZ")
    val gyroscopeZ: Double? = null
)

/**
 * DTO для NFB метрик (оригинал)
 */
data class NfbMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("alpha")
    val alpha: Double? = null,

    @SerializedName("beta")
    val beta: Double? = null,

    @SerializedName("theta")
    val theta: Double? = null,

    @SerializedName("delta")
    val delta: Double? = null,

    @SerializedName("smr")
    val smr: Double? = null
)

/**
 * DTO для физиологических метрик (оригинал)
 */
data class PhysiologicalMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("relax")
    val relax: Double? = null,

    @SerializedName("fatigue")
    val fatigue: Double? = null,

    @SerializedName("none")
    val none: Double? = null,

    @SerializedName("concentration")
    val concentration: Double? = null,

    @SerializedName("involvement")
    val involvement: Double? = null,

    @SerializedName("stress")
    val stress: Double? = null,

    @SerializedName("nfbArtifacts")
    val nfbArtifacts: Int? = null,

    @SerializedName("cardioArtifacts")
    val cardioArtifacts: Int? = null
)

/**
 * DTO для метрик продуктивности (оригинал)
 */
data class ProductivityMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session")
    val session: Int? = null,

    @SerializedName("gravity")
    val gravity: Double? = null,

    @SerializedName("productivity")
    val productivity: Double? = null,

    @SerializedName("fatigue")
    val fatigue: Double? = null,

    @SerializedName("reverseFatigue")
    val reverseFatigue: Double? = null,

    @SerializedName("relaxation")
    val relaxation: Double? = null,

    @SerializedName("concentration")
    val concentration: Double? = null
)