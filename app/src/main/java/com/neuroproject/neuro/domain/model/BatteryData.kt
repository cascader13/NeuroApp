package com.neuroproject.neuro.domain.model

/**
 * Данные заряда батареи устройства.
 *
 * @property chargePercent уровень заряда в процентах (0-100)
 */
data class BatteryData(
    val chargePercent: Float
)