package com.neuroproject.neuro.models

data class FatigueMinuteData(
    val minuteIndex: Int,
    val cognitiveMetrics: CognitiveFatigueMetrics,
    val physiologicalMetrics: PhysiologicalFatigueMetrics,
    val psychologicalMetrics: PsychologicalFatigueMetrics
)

data class CognitiveFatigueMetrics(
    val fatigue: Float, // из productivity_metrics
    val concentration: Float,  // из productivity_metrics
    val productivity: Float,   // из productivity_metrics
    val cognitiveLoad: Float   // из emotional_metrics
)

data class PhysiologicalFatigueMetrics(
    val fatigue: Float,   // из physiological_metrics
    val stress: Float,    // из physiological_metrics
    val relax: Float,     // из physiological_metrics
    val involvement: Float // из physiological_metrics
)

data class PsychologicalFatigueMetrics(
    val cognitiveLoad: Float,  // из emotional_metrics
    val relaxation: Float,     // из emotional_metrics
    val selfControl: Float,    // из emotional_metrics
    val cognitiveControl: Float // из emotional_metrics
)

data class FatigueResult(
    val minuteIndex: Int,
    val cognitive: Float,      // 0-100
    val physiological: Float,  // 0-100
    val psychological: Float,  // 0-100
    val sessionId: Long
)

data class SessionFatigueResult(
    val sessionId: Long,
    val results: List<FatigueResult>,
    val averageCognitive: Float,
    val averagePhysiological: Float,
    val averagePsychological: Float,
    val durationMinutes: Int
)