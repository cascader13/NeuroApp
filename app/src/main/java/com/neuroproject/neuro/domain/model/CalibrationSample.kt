package com.neuroproject.neuro.domain.model

data class CalibrationSample(
    val individualFrequency: Float = 10f,
    val individualPeakFrequency: Float = 10f,
    val individualPeakFrequencyPower: Float = 10f,
    val individualPeakFrequencySuppression: Float = 2f,
    val individualBandwidth: Float = 6f,
    val individualNormalizedPower: Float = 0.5f,
    val lowerFrequency: Float = 7f,
    val upperFrequency: Float = 13f,
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
)