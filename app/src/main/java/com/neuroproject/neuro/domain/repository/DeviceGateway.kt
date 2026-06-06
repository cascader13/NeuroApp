package com.neuroproject.neuro.domain.repository


import com.neuroproject.neuro.domain.model.*
import kotlinx.coroutines.flow.Flow

// domain/repository/DeviceGateway.kt
interface DeviceGateway {

    fun init()
    fun searchDevices(): Flow<List<DeviceInfo>>
    suspend fun connect(deviceId: String)
    suspend fun disconnect()
    suspend fun startResistance()
    suspend fun stopResistance()
    suspend fun startSignalAndHR()
    suspend fun stopSignalAndHR()

    suspend fun startProductivity()
    suspend fun startSession()
    suspend fun stopSession()
    suspend fun importCalibration(data: CalibrationSample)
    fun observeConnectionState(): Flow<DeviceConnectionState>
    fun observeCalibrationState(): Flow<CalibrationStage>
    fun observeBatteryCharge(): Flow<BatteryData>
    suspend fun removeAll()
    suspend fun importPhysiologicalCalibration(
        alpha: Float,
        beta: Float,
        alphaGravity: Float,
        betaGravity: Float,
        concentration: Float
    )

    suspend fun importProductivityCalibration(
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    )

    fun observeResistance(): Flow<ResistanceData>
    fun startResistanceCheck()
    fun stopResistanceCheck()
}