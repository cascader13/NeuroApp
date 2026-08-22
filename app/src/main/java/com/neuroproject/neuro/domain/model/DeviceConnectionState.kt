package com.neuroproject.neuro.domain.model

/**
 * Состояние подключения устройства
 *
 * Определяет текущий статус соединения с нейро-гарнитурой
 */
enum class DeviceConnectionState {
    /** Процесс подключения */
    connecting,
    /** Устройство подключено и готово к работе */
    connected,
    /** Процесс отключения */
    disconnecting,
    /** Устройство отключено */
    disconnected,
    /** Ошибка подключения */
    error
}