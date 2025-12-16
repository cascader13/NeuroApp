// [file name]: UploadDto.kt
package com.neuroproject.neuro.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Основной DTO для отправки на сервер
 */
data class UploadRequest(
    @SerializedName("cardioMetrics")
    val cardioMetrics: List<CardioMetricDto>,

    @SerializedName("emotionalMetrics")
    val emotionalMetrics: List<EmotionalMetricDto>,

    @SerializedName("memsMetrics")
    val memsMetrics: List<MemsMetricDto>,

    @SerializedName("nfbMetrics")
    val nfbMetrics: List<NfbMetricDto>,

    @SerializedName("physiologicalMetrics")
    val physiologicalMetrics: List<PhysiologicalMetricDto>,

    @SerializedName("productivityMetrics")
    val productivityMetrics: List<ProductivityMetricDto>
)

/**
 * DTO для кардио метрик
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
 * DTO для эмоциональных метрик
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
 * DTO для MEMS метрик
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
 * DTO для NFB метрик
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
 * DTO для физиологических метрик
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
 * DTO для метрик продуктивности
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