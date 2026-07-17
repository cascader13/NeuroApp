// presentation/screens/calibration/CalibrationViewModel.kt
package com.neuroproject.neuro.presentation.screens.calibration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.domain.model.CalibrationSample
import com.neuroproject.neuro.domain.model.CalibrationStage
import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.CalibrationRepository
import com.neuroproject.neuro.domain.repository.DeviceGateway
import com.neuroproject.neuro.domain.usecase.calibration.CancelCalibrationUseCase
import com.neuroproject.neuro.domain.usecase.calibration.CheckPreviousCalibrationUseCase
import com.neuroproject.neuro.domain.usecase.calibration.ImportCalibrationUseCase
import com.neuroproject.neuro.domain.usecase.calibration.ObserveCalibrationStageUseCase
import com.neuroproject.neuro.domain.usecase.device.ObserveConnectionStateUseCase
import com.neuroproject.neuro.domain.usecase.sensor.ObserveResistanceUseCase
import com.neuroproject.neuro.domain.usecase.sensor.StartResistanceCheckUseCase
import com.neuroproject.neuro.domain.usecase.sensor.StopResistanceCheckUseCase
import com.neuroproject.neuro.presentation.screens.sensorchecking.toElectrodeStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
class CalibrationViewModel @Inject constructor(
    private val checkPreviousCalibrationUseCase: CheckPreviousCalibrationUseCase,
    private val importCalibrationUseCase: ImportCalibrationUseCase,
    private val cancelCalibrationUseCase: CancelCalibrationUseCase,
    private val observeCalibrationStageUseCase: ObserveCalibrationStageUseCase,
    private val metronomePlayer: MetronomePlayer,
    private val observeResistanceUseCase: ObserveResistanceUseCase,
    private val startResistanceCheckUseCase: StartResistanceCheckUseCase,
    private val stopResistanceCheckUseCase: StopResistanceCheckUseCase,
    private val deviceGateway: DeviceGateway,
    private val calibrationRepository: CalibrationRepository,
    private val authRepository: AuthRepository,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    private val _isDeviceDisconnected = MutableStateFlow(false)
    val isDeviceDisconnected: StateFlow<Boolean> = _isDeviceDisconnected.asStateFlow()

    private var calibrationJob: Job? = null
    private var isCompleting = false
    private val totalCalibrationTime = 60000L

    // Накопленные данные калибровки
    private var latestCalibration: CalibrationSample = CalibrationSample()

    init {
        checkPreviousCalibration()
        observeCalibrationStage()
        observeResistance()
        observeCalibrationResult()
        startResistanceCheck()
        observeConnectionState()
    }

    private fun observeConnectionState() {
        observeConnectionStateUseCase()
            .catch { error ->
                Log.e("Calibration", "Error observing connection state", error)
            }
            .onEach { state ->
                if (state == DeviceConnectionState.disconnected || state == DeviceConnectionState.error) {
                    if (_uiState.value.isCalibrating) {
                        cancelCalibration()
                    }
                    _isDeviceDisconnected.value = true
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startResistanceCheck() {
        viewModelScope.launch {
            try {
                delay(500)
                startResistanceCheckUseCase()
            } catch (e: Exception) {
                Log.e("Calibration", "Error starting resistance check", e)
            }
        }
    }

    private fun observeResistance() {
        observeResistanceUseCase()
            .catch { error ->
                Log.e("Calibration", "Error observing resistance", error)
            }
            .onEach { resistanceData ->
                _uiState.value = _uiState.value.copy(
                    electrodeStates = resistanceData.toElectrodeStates()
                )
            }
            .launchIn(viewModelScope)
    }

    private fun observeCalibrationResult() {
        deviceGateway.observeCalibrationResult()
            .catch { error ->
                Log.e("Calibration", "Error observing calibration result", error)
            }
            .onEach { calibrationSample ->
                latestCalibration = calibrationSample
                Log.d("Calibration", "Received calibration: freq=${calibrationSample.individualFrequency}")
            }
            .launchIn(viewModelScope)
    }

    private fun checkPreviousCalibration() {
        viewModelScope.launch {
            try {
                val hasPrevious = checkPreviousCalibrationUseCase()
                if (hasPrevious) {
                    _uiState.value = _uiState.value.copy(showPreviousCalibrationDialog = true)
                }
            } catch (e: Exception) {
                Log.e("Calibration", "Error checking previous calibration", e)
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    private fun observeCalibrationStage() {
        viewModelScope.launch {
            observeCalibrationStageUseCase()
                .catch { error ->
                    Log.e("Calibration", "Stage observation error", error)
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
                .collect { stageValue ->
                    Log.d("Calibration", "Stage value: $stageValue")
                    
                    when (stageValue) {
                        6 -> {
                            if (_uiState.value.isCalibrating) {
                                cancelCalibration()
                                _uiState.value = _uiState.value.copy(errorMessage = "Ошибка калибровки")
                            }
                        }
                        4, 5 -> {
                            if (_uiState.value.isCalibrating) {
                                metronomePlayer.stop()
                                completeCalibration()
                            }
                        }
                    }
                }
        }
    }

    fun usePreviousCalibration() {
        viewModelScope.launch {
            try {
                importCalibrationUseCase(usePrevious = true)
                _uiState.value = _uiState.value.copy(
                    showPreviousCalibrationDialog = false,
                    isComplete = true
                )
            } catch (e: Exception) {
                Log.e("Calibration", "Error using previous calibration", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    showPreviousCalibrationDialog = false
                )
            }
        }
    }

    fun performNewCalibration() {
        _uiState.value = _uiState.value.copy(showPreviousCalibrationDialog = false)
        startCalibration()
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showPreviousCalibrationDialog = false)
    }

    fun startCalibration() {
        if (_uiState.value.isCalibrating) return

        if (_isDeviceDisconnected.value) {
            _uiState.value = _uiState.value.copy(errorMessage = "Устройство не подключено")
            return
        }

        isCompleting = false
        _uiState.value = CalibrationUiState(
            isCalibrating = true,
            progress = 0f,
            timeRemaining = totalCalibrationTime
        )

        viewModelScope.launch {
            try {
                importCalibrationUseCase(usePrevious = false)
                startTimer()
            } catch (e: Exception) {
                Log.e("Calibration", "Error starting calibration", e)
                _uiState.value = _uiState.value.copy(
                    isCalibrating = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private suspend fun startTimer() {
        metronomePlayer.start()

        val interval = 100L
        val steps = (totalCalibrationTime / interval).toInt()

        calibrationJob = viewModelScope.launch {
            repeat(steps) { step ->
                if (_uiState.value.isCalibrating) {
                    val elapsed = step * interval
                    val remaining = totalCalibrationTime - elapsed
                    val progress = elapsed.toFloat() / totalCalibrationTime.toFloat()

                    _uiState.value = _uiState.value.copy(
                        progress = progress,
                        timeRemaining = remaining
                    )

                    delay(interval)
                }
            }

            if (_uiState.value.isCalibrating) {
                completeCalibration()
            }
        }
    }

    private fun completeCalibration() {
        if (isCompleting) return
        isCompleting = true

        metronomePlayer.stop()

        viewModelScope.launch {
            try {
                val userId = authRepository.getUserId()
                calibrationRepository.saveCalibration(userId, latestCalibration)
                Log.d("Calibration", "Calibration saved: freq=${latestCalibration.individualFrequency}")
            } catch (e: Exception) {
                Log.e("Calibration", "Error saving calibration", e)
            }
        }

        _uiState.value = CalibrationUiState(
            isCalibrating = false,
            isComplete = true,
            progress = 1f,
            timeRemaining = 0L
        )
    }

    fun cancelCalibration() {
        calibrationJob?.cancel()
        calibrationJob = null
        metronomePlayer.stop()

        viewModelScope.launch {
            try {
                cancelCalibrationUseCase()
            } catch (e: Exception) {
                Log.e("Calibration", "Error cancelling calibration", e)
            }
        }

        _uiState.value = CalibrationUiState(
            isCalibrating = false,
            isComplete = false,
            progress = 0f,
            timeRemaining = totalCalibrationTime
        )
    }

    fun resetState() {
        isCompleting = false
        _uiState.value = CalibrationUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        calibrationJob?.cancel()
        metronomePlayer.stop()
        viewModelScope.launch {
            try {
                stopResistanceCheckUseCase()
            } catch (e: Exception) {
                Log.e("Calibration", "Error stopping resistance check", e)
            }
        }
        super.onCleared()
    }
}