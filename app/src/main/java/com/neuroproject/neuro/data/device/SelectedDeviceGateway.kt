package com.neuroproject.neuro.data.device

import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.model.DeviceInfo
import com.neuroproject.neuro.domain.model.DeviceType
import com.neuroproject.neuro.domain.repository.DeviceGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Singleton

/** Delegates all common device operations to the vendor selected on the search screen. */
@Singleton
class SelectedDeviceGateway(
    private val neiryGateway: CapsuleDeviceAdapter,
    private val neuroPlayGateway: NeuroPlayDeviceGateway
) : DeviceGateway {
    private val selectedType = MutableStateFlow(DeviceType.NEIRY)

    val currentType: DeviceType get() = selectedType.value

    fun select(type: DeviceType) {
        selectedType.value = type
    }

    private fun selected(): DeviceGateway = when (selectedType.value) {
        DeviceType.NEIRY -> neiryGateway
        DeviceType.NEUROPLAY -> neuroPlayGateway
    }

    override fun init() = selected().init()
    override fun searchDevices(): Flow<List<DeviceInfo>> = selected().searchDevices()
    override suspend fun connect(deviceId: String) = selected().connect(deviceId)
    override suspend fun disconnect() = selected().disconnect()
    override suspend fun startSession() = selected().startSession()
    override suspend fun stopSession() = selected().stopSession()
    override fun observeConnectionState(): Flow<DeviceConnectionState> =
        selectedType.flatMapLatest { selected().observeConnectionState() }
}
