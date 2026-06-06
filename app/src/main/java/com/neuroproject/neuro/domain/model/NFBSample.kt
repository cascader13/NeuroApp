package com.neuroproject.neuro.domain.model

/**
 * Данные нейрофидбека (НФБ)
 *
 * Содержит спектральные характеристики ЭЭГ сигнала
 *
 * @property timestamp временная метка в миллисекундах
 * @property alpha альфа-ритм (8-13 Гц) - состояние покоя
 * @property beta бета-ритм (13-30 Гц) - активное мышление
 * @property theta тета-ритм (4-8 Гц) - дремотное состояние
 * @property delta дельта-ритм (0.5-4 Гц) - глубокий сон
 * @property smr сенсомоторный ритм (12-15 Гц)
 */
data class NFBSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val alpha: Float = 0f,
    val beta: Float = 0f,
    val theta: Float = 0f,
    val delta: Float = 0f,
    val smr: Float = 0f
): SensorSample()