package com.neuroproject.neuro.domain.model

/**
 * Индексы продуктивности с рекомендациями
 *
 * @property time временная метка
 * @property relaxation рекомендация по расслаблению
 * @property stress уровень стресса текстовым описанием
 * @property gravityBaseline базовый уровень гравитации
 * @property productivityBaseline базовый уровень продуктивности
 * @property fatigueBaseline базовый уровень утомления
 * @property reverseFatiqueBaseline обратный базовый уровень утомления
 * @property relaxationBaseline базовый уровень расслабления
 * @property concentrationBaseline базовый уровень концентрации
 * @property hasArtifacts наличие артефактов
 */
data class ProductivityIndexSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val relaxation: String = "NoRecommendation",
    val stress: String = "NoStress",
    val gravityBaseline: Float = 1f,
    val productivityBaseline: Float = 1f,
    val fatigueBaseline: Float = 1f,
    val reverseFatiqueBaseline: Float = 1f,
    val relaxationBaseline: Float = 1f,
    val concentrationBaseline: Float = 1f,
    val hasArtifacts: Boolean = false
): SensorSample()
