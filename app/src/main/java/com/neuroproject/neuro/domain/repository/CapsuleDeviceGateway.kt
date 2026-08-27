package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.BatteryData
import com.neuroproject.neuro.domain.model.CalibrationSample
import com.neuroproject.neuro.domain.model.CalibrationStage
import com.neuroproject.neuro.domain.model.ResistanceData
import kotlinx.coroutines.flow.Flow

/**
 * Расширение базового [DeviceGateway] возможностями,
 * специфичными для Capsule.
 *
 * Код, которому нужны только поиск, подключение
 * и управление сессией, должен зависеть от [DeviceGateway].
 *
 * Код, использующий сопротивление, Capsule-калибровку,
 * productivity и другие специфичные возможности,
 * должен зависеть от [CapsuleDeviceGateway].
 */
interface CapsuleDeviceGateway : DeviceGateway {

    /**
     * Запускает измерение сопротивления электродов.
     */
    suspend fun startResistance()

    /**
     * Останавливает измерение сопротивления электродов.
     */
    suspend fun stopResistance()

    /**
     * Запускает передачу EEG и HR,
     * используемую Capsule при калибровке.
     */
    suspend fun startSignalAndHR()

    /**
     * Останавливает передачу EEG и HR.
     */
    suspend fun stopSignalAndHR()

    /**
     * Запускает расчёт / получение productivity-метрик Capsule.
     */
    suspend fun startProductivity()

    /**
     * Импортирует EEG-калибровку Capsule.
     */
    suspend fun importCalibration(
        data: CalibrationSample
    )

    /**
     * Импортирует физиологическую калибровку Capsule.
     */
    suspend fun importPhysiologicalCalibration(
        alpha: Float,
        beta: Float,
        alphaGravity: Float,
        betaGravity: Float,
        concentration: Float
    )

    /**
     * Импортирует productivity-калибровку Capsule.
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
     * Наблюдает за состоянием Capsule-калибровки.
     */
    fun observeCalibrationState(): Flow<CalibrationStage>

    /**
     * Наблюдает за уровнем заряда Capsule.
     */
    fun observeBatteryCharge(): Flow<BatteryData>

    /**
     * Наблюдает за сопротивлением электродов Capsule.
     */
    fun observeResistance(): Flow<ResistanceData>

    /**
     * Наблюдает за результатом EEG-калибровки Capsule.
     */
    fun observeCalibrationResult(): Flow<CalibrationSample>

    /**
     * Запускает непрерывную проверку сопротивления.
     */
    fun startResistanceCheck()

    /**
     * Останавливает непрерывную проверку сопротивления.
     */
    fun stopResistanceCheck()

    /**
     * Освобождает vendor-specific ресурсы Capsule.
     */
    suspend fun removeAll()
}