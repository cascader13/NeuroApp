package com.neuroproject.neuro.domain.model

/**
 * Обработанные данные ЭЭГ
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property channel1 обработанное значение первого канала
 * @property channel2 обработанное значение второго канала
 */
data class EEGProcessedSample(

    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val channel1: Float = 0f,
    val channel2: Float = 0f
): SensorSample()
