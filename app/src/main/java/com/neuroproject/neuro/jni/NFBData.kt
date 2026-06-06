package com.neuroproject.neuro.jni

/**
 * DTO для последнего значения NFB callback.
 *
 * Оставлен в jni-пакете как совместимый тип для старых экранов/сервисов. Новая запись
 * метрик должна идти через domain SensorSample и соответствующие use case.
 */
data class NFBData(
    val time: Long = 0L,
    val alpha: Float = 0f,
    val beta: Float = 0f,
    val theta: Float = 0f,
    val delta: Float = 0f,
    val smr: Float = 0f
)
