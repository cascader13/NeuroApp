package com.neuroproject.neuro.data.device

import com.neuroproject.neuro.domain.model.BatteryData
import com.neuroproject.neuro.domain.model.CalibrationSample
import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.model.DeviceInfo
import com.neuroproject.neuro.domain.model.ResistanceData
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Фейковая реализация DeviceGateway для тестирования.
 * Не требует реального устройства или JNI.
 */
class FakeDeviceAdapter @Inject constructor() : DeviceGateway {

    // Состояния (для эмуляции)
    private val _connectionState = MutableStateFlow(DeviceConnectionState.disconnected)
    override fun observeConnectionState(): Flow<DeviceConnectionState> = _connectionState.asStateFlow()

    private val _calibrationState = MutableStateFlow(com.neuroproject.neuro.domain.model.CalibrationStage.CALIBRATOR_UNKNOWN_STAGE)
    override fun observeCalibrationState(): Flow<com.neuroproject.neuro.domain.model.CalibrationStage> = _calibrationState.asStateFlow()

    private val _batteryCharge = MutableStateFlow(BatteryData(100f))
    override fun observeBatteryCharge(): Flow<BatteryData> = _batteryCharge.asStateFlow()

    // Эмулируемые устройства
    private val fakeDevices = listOf(
        DeviceInfo(name = "Fake Capsule 1", address = "AA:BB:CC:DD:EE:01"),
        DeviceInfo(name = "Fake Capsule 2", address = "AA:BB:CC:DD:EE:02"),
        DeviceInfo(name = "Fake Capsule 3", address = "AA:BB:CC:DD:EE:03")
    )

    override fun init() {
        println("FakeDeviceAdapter: init called")
    }

    override fun searchDevices(): Flow<List<DeviceInfo>> = flow {
        println("FakeDeviceAdapter: searchDevices started")
        delay(1000)  // Имитируем задержку поиска

        // Эмулируем постепенное нахождение устройств
        emit(listOf(fakeDevices[0]))
        delay(1000)
        emit(listOf(fakeDevices[0], fakeDevices[1]))
        delay(1000)
        emit(fakeDevices)  // все устройства
    }

    override suspend fun connect(deviceId: String) {
        println("FakeDeviceAdapter: connecting to $deviceId")
        _connectionState.value = DeviceConnectionState.connecting
        delay(2000)  // Имитируем время подключения

        if (deviceId.contains("02")) {
            // Эмулируем ошибку подключения для второго устройства
            _connectionState.value = DeviceConnectionState.error
            throw Exception("Connection failed for $deviceId")
        } else {
            _connectionState.value = DeviceConnectionState.connected
            println("FakeDeviceAdapter: connected to $deviceId")
        }
    }

    override suspend fun disconnect() {
        println("FakeDeviceAdapter: disconnecting")
        _connectionState.value = DeviceConnectionState.disconnecting
        delay(500)
        _connectionState.value = DeviceConnectionState.disconnected
        println("FakeDeviceAdapter: disconnected")
    }

    override suspend fun startResistance() {
        println("FakeDeviceAdapter: startResistance")
    }

    override suspend fun stopResistance() {
        println("FakeDeviceAdapter: stopResistance")
    }

    override suspend fun startSignalAndHR() {
        println("FakeDeviceAdapter: startSignalAndHR")
    }

    override suspend fun stopSignalAndHR() {
        println("FakeDeviceAdapter: stopSignalAndHR")
    }

    override suspend fun startProductivity() {
        println("FakeDeviceAdapter: startProductivity")
    }

    override suspend fun startSession() {
        println("FakeDeviceAdapter: startSession")
    }

    override suspend fun stopSession() {
        println("FakeDeviceAdapter: stopSession")
    }

    override suspend fun importCalibration(data: CalibrationSample) {
        println("FakeDeviceAdapter: importCalibration")
    }

    override suspend fun removeAll() {
        println("FakeDeviceAdapter: removeAll")
    }

    override suspend fun importPhysiologicalCalibration(
        alpha: Float,
        beta: Float,
        alphaGravity: Float,
        betaGravity: Float,
        concentration: Float
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun importProductivityCalibration(
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    ) {
        TODO("Not yet implemented")
    }

    override fun observeResistance(): Flow<ResistanceData> {
        TODO("Not yet implemented")
    }

    override fun observeCalibrationResult(): Flow<CalibrationSample> = flow {
        emit(CalibrationSample())
    }

    override fun startResistanceCheck() {
        TODO("Not yet implemented")
    }

    override fun stopResistanceCheck() {
        TODO("Not yet implemented")
    }
}