package com.neuroproject.neuro.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.model.DeviceInfo
import com.neuroproject.neuro.domain.repository.DeviceGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

@Singleton
@SuppressLint("MissingPermission")
class NeuroPlayDeviceGateway @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceGateway {
    private val manager get() = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val connectionState = MutableStateFlow(DeviceConnectionState.disconnected)
    private var gatt: BluetoothGatt? = null
    private var eegData: BluetoothGattCharacteristic? = null
    private var eegControl: BluetoothGattCharacteristic? = null

    override fun init() {
        checkBluetooth()
    }

    override fun observeConnectionState(): Flow<DeviceConnectionState> = connectionState.asStateFlow()

    override fun searchDevices(): Flow<List<DeviceInfo>> = callbackFlow {
        checkBluetooth()
        val scanner = manager.adapter.bluetoothLeScanner
            ?: throw IllegalStateException("Bluetooth LE scanner is unavailable")
        val devices = linkedMapOf<String, DeviceInfo>()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val address = result.device.address
                devices[address] = DeviceInfo(result.device.name ?: "NeuroPlay", address)
                trySend(devices.values.toList())
            }

            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException("Bluetooth scan failed: $errorCode"))
            }
        }
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(EEG_SERVICE_UUID)).build()
        scanner.startScan(listOf(filter), ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), callback)
        awaitClose { scanner.stopScan(callback) }
    }

    override suspend fun connect(deviceId: String) {
        checkBluetooth()
        disconnect()
        connectionState.value = DeviceConnectionState.connecting
        try {
            withTimeout(30_000) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val callback = object : BluetoothGattCallback() {
                        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
                            when (newState) {
                                BluetoothProfile.STATE_CONNECTED -> g.discoverServices()
                                BluetoothProfile.STATE_DISCONNECTED -> {
                                    connectionState.value = if (status == BluetoothGatt.GATT_SUCCESS) DeviceConnectionState.disconnected else DeviceConnectionState.error
                                    if (continuation.isActive) continuation.resumeWithException(IllegalStateException("NeuroPlay disconnected, status=$status"))
                                }
                            }
                        }

                        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                            val service: BluetoothGattService? = g.getService(EEG_SERVICE_UUID)
                            eegData = service?.getCharacteristic(EEG_DATA_UUID)
                            eegControl = service?.getCharacteristic(EEG_CONTROL_UUID)
                            if (status == BluetoothGatt.GATT_SUCCESS && eegData != null && eegControl != null) {
                                connectionState.value = DeviceConnectionState.connected
                                if (continuation.isActive) continuation.resume(Unit)
                            } else if (continuation.isActive) {
                                continuation.resumeWithException(IllegalStateException("NeuroPlay EEG service is unavailable"))
                            }
                        }
                    }
                    gatt = manager.adapter.getRemoteDevice(deviceId).connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
                    continuation.invokeOnCancellation { gatt?.close() }
                }
            }
        } catch (error: Throwable) {
            connectionState.value = DeviceConnectionState.error
            gatt?.close()
            gatt = null
            throw error
        }
    }

    override suspend fun disconnect() {
        val current = gatt ?: return
        connectionState.value = DeviceConnectionState.disconnecting
        current.disconnect()
        current.close()
        gatt = null
        eegData = null
        eegControl = null
        connectionState.value = DeviceConnectionState.disconnected
    }

    override suspend fun startSession() {
        val currentGatt = requireNotNull(gatt) { "NeuroPlay is not connected" }
        val data = requireNotNull(eegData) { "NeuroPlay EEG data characteristic is unavailable" }
        val control = requireNotNull(eegControl) { "NeuroPlay EEG control characteristic is unavailable" }
        currentGatt.setCharacteristicNotification(data, true)
        writeDescriptor(currentGatt, data.getDescriptor(CCCD_UUID), byteArrayOf(0x01, 0x00))
        writeCharacteristic(currentGatt, control, byteArrayOf(0x01, 0x01)) // 4/6/8 channels, 125 Hz
    }

    override suspend fun stopSession() {
        val currentGatt = gatt ?: return
        eegControl?.let { writeCharacteristic(currentGatt, it, byteArrayOf(0x00, 0x00)) }
        eegData?.let {
            currentGatt.setCharacteristicNotification(it, false)
            it.getDescriptor(CCCD_UUID)?.let { descriptor -> writeDescriptor(currentGatt, descriptor, byteArrayOf(0x00, 0x00)) }
        }
    }

    private fun checkBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            val connect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            if (!scan || !connect) throw SecurityException("Bluetooth permissions are not granted")
        }
        if (!manager.adapter.isEnabled) throw IllegalStateException("Bluetooth is disabled")
    }

    private fun writeCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        if (Build.VERSION.SDK_INT >= 33) gatt.writeCharacteristic(characteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        else { characteristic.value = value; gatt.writeCharacteristic(characteristic) }
    }

    private fun writeDescriptor(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor?, value: ByteArray) {
        requireNotNull(descriptor) { "NeuroPlay notification descriptor is unavailable" }
        if (Build.VERSION.SDK_INT >= 33) gatt.writeDescriptor(descriptor, value)
        else { descriptor.value = value; gatt.writeDescriptor(descriptor) }
    }

    companion object {
        val EEG_SERVICE_UUID: UUID = UUID.fromString("f0001298-0451-4000-b000-000000000000")
        val EEG_DATA_UUID: UUID = UUID.fromString("f0001299-0451-4000-b000-000000000000")
        val EEG_CONTROL_UUID: UUID = UUID.fromString("f000129a-0451-4000-b000-000000000000")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
