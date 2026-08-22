package com.neuroproject.neuro.domain.model

/**
 * Сырые данные ЭЭГ
 *
 * @property timestamp временная метка в миллисекундах
 * @property channel1 значение с первого канала
 * @property channel2 значение со второго канала
 */
data class EEGRawSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val channel1: Float = 0f,
    val channel2: Float = 0f
): SensorSample()
