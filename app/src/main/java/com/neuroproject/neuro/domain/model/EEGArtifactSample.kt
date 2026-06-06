package com.neuroproject.neuro.domain.model

/**
 * Информация об артефактах ЭЭГ
 *
 * @property timestamp временная метка в миллисекундах
 * @property artifactChannel1 наличие артефактов на первом канале
 * @property artifactChannel2 наличие артефактов на втором канале
 * @property qualityChannel1 качество сигнала первого канала (0-1)
 * @property qualityChannel2 качество сигнала второго канала (0-1)
 */
data class EEGArtifactSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val artifactChannel1: Boolean = false,
    val artifactChannel2: Boolean = false,
    val qualityChannel1: Float = 0f,
    val qualityChannel2: Float = 0f
): SensorSample()
