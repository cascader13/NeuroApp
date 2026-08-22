package com.neuroproject.neuro.domain.model

/**
 * Эмоциональные показатели
 *
 * @property timestamp временная метка в миллисекундах
 * @property attention уровень внимания
 * @property relaxation уровень расслабления
 * @property cognitive_load когнитивная нагрузка
 * @property cognitive_control когнитивный контроль
 * @property self_control самоконтроль
 */
data class EmotionalSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val attention: Float = 0f,
    val relaxation: Float = 0f,
    val cognitiveLoad: Float = 0f,
    val cognitiveControl: Float = 0f,
    val selfControl: Float = 0f
): SensorSample()
