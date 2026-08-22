package com.neuroproject.neuro.domain.model

/**
 * Базовые значения продуктивности
 *
 * @property time временная метка
 * @property gravity гравитационная составляющая
 * @property productivity уровень продуктивности
 * @property fatigue уровень утомления
 * @property reverse_fatique обратный уровень утомления
 * @property relaxation уровень расслабления
 * @property concentration уровень концентрации
 */
data class ProductivityBaselineSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val gravity: Float = 1f,
    val productivity: Float = 1f,
    val fatigue: Float = 1f,
    val reverse_fatique: Float = 1f,
    val relaxation: Float = 1f,
    val concentration: Float = 1f
): SensorSample()
