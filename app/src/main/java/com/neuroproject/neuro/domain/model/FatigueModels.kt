package com.neuroproject.neuro.domain.model

/**
 * Данные утомления за одну минуту сессии.
 *
 * @property minuteIndex номер минуты
 * @property cognitiveMetrics когнитивные метрики
 * @property physiologicalMetrics физиологические метрики
 * @property psychologicalMetrics психологические метрики
 */
data class FatigueMinuteData(
    val minuteIndex: Int,
    val cognitiveMetrics: CognitiveFatigueMetrics,
    val physiologicalMetrics: PhysiologicalFatigueMetrics,
    val psychologicalMetrics: PsychologicalFatigueMetrics
)

/**
 * Когнитивные метрики утомления (продуктивность, концентрация, нагрузка).
 */
data class CognitiveFatigueMetrics(
    val fatigue: Float, // из productivity_metrics
    val concentration: Float,  // из productivity_metrics
    val productivity: Float,   // из productivity_metrics
    val cognitiveLoad: Float   // из emotional_metrics
)

/**
 * Физиологические метрики утомления (стресс, расслабление, вовлечённость).
 */
data class PhysiologicalFatigueMetrics(
    val fatigue: Float,   // из physiological_metrics
    val stress: Float,    // из physiological_metrics
    val relax: Float,     // из physiological_metrics
    val involvement: Float // из physiological_metrics
)

/**
 * Психологические метрики утомления (когнитивная нагрузка, контроль, расслабление).
 */
data class PsychologicalFatigueMetrics(
    val cognitiveLoad: Float,  // из emotional_metrics
    val relaxation: Float,     // из emotional_metrics
    val selfControl: Float,    // из emotional_metrics
    val cognitiveControl: Float // из emotional_metrics
)

/**
 * Результат расчёта уtomления за минуту (0-100 по каждому типу).
 */
data class FatigueResult(
    val minuteIndex: Int,
    val cognitive: Float,      // 0-100
    val physiological: Float,  // 0-100
    val psychological: Float,  // 0-100
    val sessionId: Long
)

/**
 * Сводные результаты утомления за всю сессию.
 */
data class SessionFatigueResult(
    val sessionId: Long,
    val results: List<FatigueResult>,
    val averageCognitive: Float,
    val averagePhysiological: Float,
    val averagePsychological: Float,
    val durationMinutes: Int
)