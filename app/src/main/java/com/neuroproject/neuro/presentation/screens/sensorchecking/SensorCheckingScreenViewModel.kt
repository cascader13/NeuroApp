// presentation/screens/sensorchecking/SensorCheckingViewModel.kt
package com.neuroproject.neuro.presentation.screens.sensorchecking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.usecase.device.ObserveConnectionStateUseCase
import com.neuroproject.neuro.domain.usecase.sensor.ObserveBatteryUseCase
import com.neuroproject.neuro.domain.usecase.sensor.ObserveResistanceUseCase
import com.neuroproject.neuro.domain.usecase.sensor.StartResistanceCheckUseCase
import com.neuroproject.neuro.domain.usecase.sensor.StopResistanceCheckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorCheckingViewModel @Inject constructor(
    private val observeResistanceUseCase: ObserveResistanceUseCase,
    private val observeBatteryUseCase: ObserveBatteryUseCase,
    private val startResistanceCheckUseCase: StartResistanceCheckUseCase,
    private val stopResistanceCheckUseCase: StopResistanceCheckUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensorCheckingUiState())
    val uiState: StateFlow<SensorCheckingUiState> = _uiState.asStateFlow()

    private val _isDeviceDisconnected = MutableStateFlow(false)
    val isDeviceDisconnected: StateFlow<Boolean> = _isDeviceDisconnected.asStateFlow()

    init {
        observeResistance()
        observeBattery()
        observeConnectionState()
    }

    private fun observeConnectionState() {
        observeConnectionStateUseCase()
            .catch { error ->
                Log.e("SensorChecking", "Error observing connection state", error)
            }
            .onEach { state ->
                if (state == DeviceConnectionState.disconnected || state == DeviceConnectionState.error) {
                    _isDeviceDisconnected.value = true
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeResistance() {
        observeResistanceUseCase()
            .catch { error ->
                Log.e("SensorChecking", "Error observing resistance", error)
                _uiState.value = _uiState.value.copy(errorMessage = error.message)
            }
            .onEach { resistanceData ->
                val electrodeStates = resistanceData.toElectrodeStates()
                _uiState.value = _uiState.value.copy(
                    electrodeStates = electrodeStates,
                    isChecking = !electrodeStates.isAllOk()
                )
                Log.d("SensorChecking", "Resistance: o1=${resistanceData.o1}, o2=${resistanceData.o2}, " +
                        "t3=${resistanceData.t3}, t4=${resistanceData.t4}")
            }
            .launchIn(viewModelScope)
    }

    private fun observeBattery() {
        observeBatteryUseCase()
            .catch { error ->
                Log.e("SensorChecking", "Error observing battery", error)
                _uiState.value = _uiState.value.copy(errorMessage = error.message)
            }
            .onEach { batteryData ->
                _uiState.value = _uiState.value.copy(batteryCharge = batteryData.chargePercent)
                Log.d("SensorChecking", "Battery: ${batteryData.chargePercent}%")
            }
            .launchIn(viewModelScope)
    }

    fun start() {
        viewModelScope.launch {
            try {
                delay(2000)  // Задержка перед началом
                startResistanceCheckUseCase()
                _uiState.value = _uiState.value.copy(isChecking = true)
            } catch (e: Exception) {
                Log.e("SensorChecking", "Error starting resistance check", e)
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun finish() {
        viewModelScope.launch {
            try {
                stopResistanceCheckUseCase()
                _uiState.value = _uiState.value.copy(isChecking = false)
            } catch (e: Exception) {
                Log.e("SensorChecking", "Error stopping resistance check", e)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        finish()
    }
}