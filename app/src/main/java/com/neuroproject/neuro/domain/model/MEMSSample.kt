package com.neuroproject.neuro.domain.model

/**
 * Данные с MEMS-датчиков (акселерометр и гироскоп)
 *
 * Используется для отслеживания движений головы и компенсации артефактов
 *
 * @property timestamp временная метка в миллисекундах
 * @property accelerometerX ускорение по оси X
 * @property accelerometerY ускорение по оси Y
 * @property accelerometerZ ускорение по оси Z
 * @property gyroscopeX угловая скорость по оси X
 * @property gyroscopeY угловая скорость по оси Y
 * @property gyroscopeZ угловая скорость по оси Z
 */
data class MEMSSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val accelerometerZ: Float = 0f,
    val gyroscopeX: Float = 0f,
    val gyroscopeY: Float = 0f,
    val gyroscopeZ: Float = 0f
): SensorSample()