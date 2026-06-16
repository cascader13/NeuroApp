package com.neuroproject.neuro.domain.repository


import com.neuroproject.neuro.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Шлюз взаимодействия с нейро-гарнитурой Capsule.
 *
 * Предоставляет единый интерфейс для управления подключением,
 * калибровкой, записью данных и мониторингом состояния устройства.
 *
 * Реализуется на уровне data-слоя ([CapsuleDeviceAdapter]).
 *
 * Потоки данных (сопротивление, батарея, состояние калибровки)
 * предоставляются через [Flow] и эмитятся в реальном времени.
 */
interface DeviceGateway {

    /** Инициализирует менеджер устройства. Вызывать один раз при старте приложения. */
    fun init()

    /**
     * Запускает поиск доступных устройств.
     *
     * @return [Flow] со списком найденных [DeviceInfo].
     * Эмитит обновления при обнаружении новых устройств или потере старых.
     */
    fun searchDevices(): Flow<List<DeviceInfo>>

    /**
     * Устанавливает соединение с устройством по его идентификатору.
     *
     * @param deviceId UUID найденного устройства.
     * @throws Exception при ошибке подключения.
     */
    suspend fun connect(deviceId: String)

    /** Разрывает текущее соединение с устройством. */
    suspend fun disconnect()

    /**
     * Запускает измерение сопротивления электродов.
     * Результаты доступны через [observeResistance].
     */
    suspend fun startResistance()

    /** Останавливает измерение сопротивления. */
    suspend fun stopResistance()

    /**
     * Запускает передачу сигнала ЭЭГ и данных сердечного ритма.
     * Используется при новой калибровке.
     */
    suspend fun startSignalAndHR()

    /** Останавливает передачу сигнала ЭЭГ и данных сердечного ритма. */
    suspend fun stopSignalAndHR()

    /** Запускает сбор данных продуктивности. */
    suspend fun startProductivity()

    /**
     * Запускает основную запись сессии (ЭЭГ, физиология, MEMS и т.д.).
     * Данные поступают через [SensorStreamGateway].
     */
    suspend fun startSession()

    /** Останавливает запись сессии. */
    suspend fun stopSession()

    /**
     * Импортирует индивидуальную калибровку ЭЭГ в устройство.
     *
     * @param data параметры калибровки, полученные из предыдущей сессии.
     */
    suspend fun importCalibration(data: CalibrationSample)

    /**
     * Импортирует калибровку физиологических метрик.
     *
     * @param alpha альфа-ритм
     * @param beta бета-ритм
     * @param alphaGravity вес альфа-ритма
     * @param betaGravity вес бета-ритма
     * @param concentration концентрация
     */
    suspend fun importPhysiologicalCalibration(
        alpha: Float,
        beta: Float,
        alphaGravity: Float,
        betaGravity: Float,
        concentration: Float
    )

    /**
     * Импортирует калибровку метрик продуктивности.
     *
     * @param gravity гравитация (вес показателя)
     * @param productivity продуктивность
     * @param fatigue усталость
     * @param reverseFatigue обратная усталость
     * @param relaxation расслабление
     * @param concentration концентрация
     */
    suspend fun importProductivityCalibration(
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    )

    /**
     * Наблюдает за состоянием подключения к устройству.
     *
     * @return [Flow] с текущим [DeviceConnectionState].
     */
    fun observeConnectionState(): Flow<DeviceConnectionState>

    /**
     * Наблюдает за прогрессом калибровки.
     *
     * @return [Flow] с текущим [CalibrationStage].
     */
    fun observeCalibrationState(): Flow<CalibrationStage>

    /**
     * Наблюдает за уровнем заряда батареи.
     *
     * @return [Flow] с данными [BatteryData].
     */
    fun observeBatteryCharge(): Flow<BatteryData>

    /**
     * Наблюдает за сопротивлением электродов.
     *
     * @return [Flow] с данными [ResistanceData] (O1, O2, T3, T4).
     */
    fun observeResistance(): Flow<ResistanceData>

    /** Запускает проверку сопротивления (непрерывный режим). */
    fun startResistanceCheck()

    /** Останавливает проверку сопротивления. */
    fun stopResistanceCheck()

    /** Удаляет все ресурсы устройства (настройки, калибровку). */
    suspend fun removeAll()
}