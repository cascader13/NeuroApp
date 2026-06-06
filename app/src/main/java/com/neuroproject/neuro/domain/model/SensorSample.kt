package com.neuroproject.neuro.domain.model

import java.util.Date

/**
 * Базовый sealed класс для всех сэмплов данных сенсоров
 *
 * Используется для унификации всех типов данных, поступающих с устройства,
 * и позволяет работать с ними через единый интерфейс в UseCase-ах.
 */
sealed class SensorSample {
    abstract val timestamp: Long
    abstract val sessionId: String
    abstract val userId: String
    abstract val expeditionId: String
}


/**
 * Расширение для получения читаемого времени из timestamp
 */
fun SensorSample.formattedTime(): String {
    val date = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
    return date.format(java.util.Date(timestamp))
}

/**
 * Расширение для группировки сэмплов по типу
 */
fun List<SensorSample>.groupByType(): Map<Class<out SensorSample>, List<SensorSample>> {
    return this.groupBy { it.javaClass }
}