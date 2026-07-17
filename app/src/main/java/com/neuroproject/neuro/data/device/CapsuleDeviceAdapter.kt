// data/device/CapsuleDeviceAdapter.kt
package com.neuroproject.neuro.data.device

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.DeviceGateway
import com.neuroproject.neuro.services.CapsuleDeviceManager
import com.neuroproject.neuro.jni.JniCallbackHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Адаптер для команд устройству.
 * Реализует интерфейс com.neuroproject.neuro.domain.repository.DeviceGateway из Domain слоя.
 * Скрывает все нативные вызовы.
 */
@Singleton
class CapsuleDeviceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val capsuleManager: CapsuleDeviceManager,
    private val sensorAdapter: CapsuleSensorStreamAdapter,
    private val authRepository: AuthRepository
) : DeviceGateway {

    // Состояния
    private val _connectionState = MutableStateFlow(DeviceConnectionState.disconnected)
    override fun observeConnectionState(): Flow<DeviceConnectionState> = _connectionState.asStateFlow()

    private val _calibrationState = MutableStateFlow(CalibrationStage.CALIBRATOR_UNKNOWN_STAGE)
    override fun observeCalibrationState(): Flow<CalibrationStage> = _calibrationState.asStateFlow()

    private val _batteryCharge = MutableStateFlow(BatteryData(0f))
    override fun observeBatteryCharge(): Flow<BatteryData> = _batteryCharge.asStateFlow()

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return bluetoothManager?.adapter?.isEnabled == true
    }

    init {
        setupStateCallbacks()
    }

    private fun setupStateCallbacks() {
        JniCallbackHandler.onConnectionStateChanged = { state ->
            _connectionState.value = when (state) {
                0 -> DeviceConnectionState.connecting
                1 -> DeviceConnectionState.connected
                2 -> DeviceConnectionState.disconnecting
                3 -> DeviceConnectionState.disconnected
                else -> DeviceConnectionState.error
            }
        }

        JniCallbackHandler.onCalibrationStateChanged = { stageNum ->
            _calibrationState.value = CalibrationStage.fromInt(stageNum)
        }

        JniCallbackHandler.onBatteryChargeReceived = { data ->
            _batteryCharge.value = BatteryData(data)
        }
    }

    // Команды устройству

    override fun init() {
        capsuleManager.initCapsule()
    }

    override fun searchDevices(): Flow<List<DeviceInfo>> {
        if (!hasBluetoothPermission()) {
            throw SecurityException("BLUETOOTH_CONNECT permission not granted")
        }
        if (!isBluetoothEnabled()) {
            throw IllegalStateException("Bluetooth is disabled")
        }
        capsuleManager.startSearch()
        return sensorAdapter.searchDevices()
    }

    override suspend fun connect(deviceId: String) {
        if (!hasBluetoothPermission()) {
            throw SecurityException("BLUETOOTH_CONNECT permission not granted")
        }
        if (!isBluetoothEnabled()) {
            throw IllegalStateException("Bluetooth is disabled")
        }
        withContext(Dispatchers.IO) {
            withTimeoutOrNull(30_000L) {
                capsuleManager.connect(deviceId)
            } ?: throw IllegalStateException("Connection timed out after 30 seconds")
        }
        authRepository.saveDeviceName(deviceId)
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            capsuleManager.disconnect()
        }
    }

    override suspend fun startResistance() {
        withContext(Dispatchers.IO) {
            capsuleManager.startResistance()
        }
    }

    override suspend fun stopResistance() {
        withContext(Dispatchers.IO) {
            capsuleManager.stopResistance()
        }
    }

    override suspend fun startSignalAndHR() {
        _calibrationState.value = CalibrationStage.CALIBRATOR_UNKNOWN_STAGE
        withContext(Dispatchers.IO) {
            capsuleManager.startSignalAndHR()
        }
    }

    override suspend fun stopSignalAndHR() {
        withContext(Dispatchers.IO) {
            capsuleManager.stopSignalAndHR()
        }
        _calibrationState.value = CalibrationStage.CALIBRATOR_UNKNOWN_STAGE
    }

    override suspend fun startSession() {
        withContext(Dispatchers.IO) {
            capsuleManager.startSession()
        }
    }

    override suspend fun stopSession() {
        withContext(Dispatchers.IO) {
            capsuleManager.stopSession()
        }
    }

    override suspend fun startProductivity() {
        withContext(Dispatchers.IO) {
            capsuleManager.startProductivity()
        }
    }

    override suspend fun importCalibration(data: CalibrationSample) {
        withContext(Dispatchers.IO) {
            capsuleManager.importCalibration(
                data.individualFrequency,
                data.individualPeakFrequency,
                data.individualPeakFrequencyPower,
                data.individualPeakFrequencySuppression,
                data.individualBandwidth,
                data.individualNormalizedPower,
                data.lowerFrequency,
                data.upperFrequency
            )
        }
    }

    override suspend fun importProductivityCalibration(
        gravity: Float, productivity: Float, fatigue: Float,
        reverseFatigue: Float, relaxation: Float, concentration: Float
    ) {
        withContext(Dispatchers.IO) {
            capsuleManager.importProductivityCalibration(
                gravity, productivity, fatigue, reverseFatigue, relaxation, concentration
            )
        }
    }

    override suspend fun importPhysiologicalCalibration(
        alpha: Float, beta: Float, alphaGravity: Float,
        betaGravity: Float, concentration: Float
    ) {
        withContext(Dispatchers.IO) {
            capsuleManager.importPhysiologicalCalibration(
                alpha, beta, alphaGravity, betaGravity, concentration
            )
        }
    }

    override suspend fun removeAll() {
        withContext(Dispatchers.IO) {
            capsuleManager.removeAllResources()
        }
    }

    override fun observeResistance(): Flow<ResistanceData> = callbackFlow {
        JniCallbackHandler.onResistanceReceived = { o1, o2, t3, t4 ->
            trySend(ResistanceData(o1 = o1, o2 = o2, t3 = t3, t4 = t4))
                .onFailure { cause ->
                    android.util.Log.e("DeviceAdapter", "Failed to send resistance data", cause)
                }
        }

        awaitClose {
            JniCallbackHandler.onResistanceReceived = null
        }
    }

    override fun observeCalibrationResult(): Flow<CalibrationSample> =
        sensorAdapter.observeCalibrationResult()

    override fun startResistanceCheck() {
        capsuleManager.startResistance()
    }

    override fun stopResistanceCheck() {
        capsuleManager.stopResistance()
    }
}