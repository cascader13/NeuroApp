package com.neuroproject.neuro.domain.model

data class ObjectiveFatigueResult(
    val cognitiveIndex: Int,
    val psychologicalIndex: Int,
    val physiologicalIndex: Int,
    val averageIndex: Int,
    val fatigueLevel: String,
    val stressLevel: String
)

data class MinuteFatigueData(
    val minuteIndex: Int,
    val cognitive: CognitiveMetrics,
    val physiological: PhysiologicalMetrics,
    val psychological: PsychologicalMetrics
)

data class CognitiveMetrics(
    val fatigue: Float,
    val concentration: Float,
    val productivity: Float,
    val cognitiveLoad: Float
)

data class PhysiologicalMetrics(
    val fatigue: Float,
    val stress: Float,
    val relax: Float,
    val involvement: Float
)

data class PsychologicalMetrics(
    val cognitiveLoad: Float,
    val relaxation: Float,
    val selfControl: Float,
    val cognitiveControl: Float
)

data class ObjectiveCalculationConfig(
    val cognitiveWeights: CognitiveWeights = CognitiveWeights(),
    val physiologicalWeights: PhysiologicalWeights = PhysiologicalWeights(),
    val psychologicalWeights: PsychologicalWeights = PsychologicalWeights()
)

data class CognitiveWeights(
    val fatigue: Float = 0.30f,
    val concentration: Float = 0.25f,
    val productivity: Float = 0.20f,
    val cognitiveLoad: Float = 0.25f
)

data class PhysiologicalWeights(
    val fatigue: Float = 0.35f,
    val stress: Float = 0.25f,
    val relax: Float = 0.20f,
    val involvement: Float = 0.20f
)

data class PsychologicalWeights(
    val cognitiveLoad: Float = 0.30f,
    val relaxation: Float = 0.25f,
    val selfControl: Float = 0.25f,
    val cognitiveControl: Float = 0.20f
)

enum class FatigueLevel(val range: IntRange, val description: String) {
    LOW(0..20, "Низкий уровень утомления"),
    MODERATE(21..40, "Умеренный уровень утомления"),
    MEDIUM(41..60, "Средний уровень утомления"),
    ELEVATED(61..80, "Повышенный уровень утомления"),
    HIGH(81..100, "Высокий уровень утомления");

    companion object {
        fun fromValue(value: Int): FatigueLevel {
            return values().find { value in it.range } ?: MEDIUM
        }
    }
}

enum class StressLevel(val range: IntRange, val description: String) {
    LOW(0..20, "Низкий уровень стресса"),
    MODERATE(21..40, "Умеренный уровень стресса"),
    MEDIUM(41..60, "Средний уровень стресса"),
    ELEVATED(61..80, "Повышенный уровень стресса"),
    HIGH(81..100, "Высокий уровень стресса");

    companion object {
        fun fromValue(value: Int): StressLevel {
            return values().find { value in it.range } ?: MEDIUM
        }
    }
}