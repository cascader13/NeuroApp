// presentation/screens/calibration/CalibrationViewModel.kt
package com.neuroproject.neuro.presentation.screens.calibration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.domain.model.CalibrationStage
import com.neuroproject.neuro.domain.usecase.calibration.CancelCalibrationUseCase
import com.neuroproject.neuro.domain.usecase.calibration.CheckPreviousCalibrationUseCase
import com.neuroproject.neuro.domain.usecase.calibration.ImportCalibrationUseCase
import com.neuroproject.neuro.domain.usecase.calibration.ObserveCalibrationStageUseCase
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
    private val stopResistanceCheckUseCase: StopResistanceCheckUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    private var calibrationJob: Job? = null
    private val totalCalibrationTime = 60000L

    init {
        checkPreviousCalibration()
        observeCalibrationStage()
        observeResistance()
        startResistanceCheck()
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

                    // stageValue - это Int из JNI (0,1,2,3,4,5,6)
                    when (stageValue) {
                        6 -> { // Ошибка калибровки
                            cancelCalibration()
                            _uiState.value = _uiState.value.copy(errorMessage = "Ошибка калибровки")
                        }
                        4, 5 -> { // PHYSIO_INIT_STAGE или PHYSIO_BASELINE_STAGE
                            metronomePlayer.stop()
                            if (_uiState.value.isCalibrating) {
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
        metronomePlayer.stop()
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
        _uiState.value = CalibrationUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        calibrationJob?.cancel()
        metronomePlayer.stop()
        viewModelScope.launch {
            try {
                stopResistanceCheckUseCase()
            } catch (e: Exception) {
                Log.e("Calibration", "Error stopping resistance check", e)
            }
        }
    }
}