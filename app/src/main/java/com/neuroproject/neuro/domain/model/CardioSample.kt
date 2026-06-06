package com.neuroproject.neuro.domain.model

/**
 * Кардиологические данные (ЧСС и связанные показатели)
 *
 * @property timestamp временная метка в миллисекундах
 * @property heartRate частота сердечных сокращений (уд/мин)
 * @property hasArtifacts наличие артефактов в сигнале
 * @property kaplanIndex индекс Каплана (вариабельность сердечного ритма)
 * @property metricsAvailable доступность метрик
 * @property motionArtifacts артефакты движения
 * @property skinContact качество контакта с кожей
 * @property stress уровень стресса на основе кардиоданных
 */
data class CardioSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val heartRate: Float = 0f,
    val hasArtifacts: Boolean = false,
    val kaplanIndex: Float = 0f,
    val metricsAvailable: Boolean = false,
    val motionArtifacts: Boolean = false,
    val skinContact: Boolean = false,
    val stress: Float = 0f,
) : SensorSample()
