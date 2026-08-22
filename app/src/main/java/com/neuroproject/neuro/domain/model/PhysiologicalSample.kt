package com.neuroproject.neuro.domain.model

/**
 * Физиологические данные пользователя
 *
 * Содержит комплексные показатели психофизиологического состояния
 *
 * @property timestamp временная метка в миллисекундах
 * @property relaxation уровень расслабления (0-1)
 * @property fatigue уровень утомления (0-1)
 * @property none нейтральное состояние
 * @property concentration уровень концентрации (0-1)
 * @property involvement уровень вовлеченности (0-1)
 * @property stress уровень стресса (0-1)
 * @property nfbArtifacts наличие артефактов в нейрофидбек-сигнале
 * @property cardioArtifacts наличие артефактов в кардиосигнале
 */
data class PhysiologicalSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val relaxation: Float = 0f,
    val fatigue: Float = 0f,
    val none: Float = 0f,
    val concentration: Float = 0f,
    val involvement: Float = 0f,
    val stress: Float = 0f,
    val nfbArtifacts: Boolean = true,
    val cardioArtifacts: Boolean = true
): SensorSample()