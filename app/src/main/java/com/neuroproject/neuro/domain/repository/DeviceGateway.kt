package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.model.DeviceInfo
import kotlinx.coroutines.flow.Flow

/**
 * Базовый шлюз взаимодействия с нейро-устройством.
 *
 * Содержит только операции, которые должны быть общими
 * для разных производителей устройств.
 *
 * Vendor-specific возможности должны находиться
 * в специализированных интерфейсах, например
 * [CapsuleDeviceGateway].
 */
interface DeviceGateway {

    /**
     * Инициализирует SDK / менеджер устройства.
     *
     * Обычно вызывается один раз перед началом работы.
     */
    fun init()

    /**
     * Запускает поиск доступных устройств.
     *
     * @return поток найденных устройств.
     */
    fun searchDevices(): Flow<List<DeviceInfo>>

    /**
     * Подключается к устройству.
     *
     * @param deviceId идентификатор устройства,
     * формат которого определяется конкретным gateway.
     */
    suspend fun connect(deviceId: String)

    /**
     * Отключается от текущего устройства.
     */
    suspend fun disconnect()

    /**
     * Наблюдает за состоянием подключения.
     */
    fun observeConnectionState(): Flow<DeviceConnectionState>

    /**
     * Запускает основную сессию получения данных.
     *
     * Конкретная реализация определяет,
     * какие потоки необходимо активировать.
     */
    suspend fun startSession()

    /**
     * Останавливает текущую сессию получения данных.
     */
    suspend fun stopSession()
}