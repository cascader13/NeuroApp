package com.neuroproject.neuro.domain.model

/**
 * Данные продуктивности
 *
 * Комплексный показатель эффективности когнитивной деятельности
 *
 * @property timestamp временная метка в миллисекундах
 * @property timestampProd альтернативная временная метка
 * @property gravity гравитационная составляющая
 * @property productivity уровень продуктивности
 * @property fatigue уровень утомления
 * @property reverseFatigue обратный показатель утомления
 * @property relaxation уровень расслабления
 * @property concentration уровень концентрации
 */
data class ProductivitySample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val timestampProd: Double = 0.0,
    val gravity: Float = 0f,
    val productivity: Float = 0f,
    val fatigue: Float = 0f,
    val reverseFatigue: Float = 0f,
    val relaxation: Float = 0f,
    val concentration: Float = 0f
): SensorSample()
