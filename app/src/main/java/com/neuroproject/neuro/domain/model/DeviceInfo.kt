package com.neuroproject.neuro.domain.model

/**
 * Информация о найденном устройстве.
 *
 * @property name имя устройства (например, "Capsule-12345")
 * @property address MAC-адрес или UUID устройства
 */
class DeviceInfo(val name: String, val address: String)