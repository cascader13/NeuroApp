
package com.neuroproject.neuro.screens.calibration

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.neuroproject.neuro.services.CapsuleDeviceManager

data class NewCalibrationState(

    var ClosedEyes: Boolean = true,
    val isCalibrating: Boolean = false,
    val isComplete: Boolean = false,
)

@HiltViewModel
class NewCalibrationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    dm: CapsuleDeviceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalibrationState())
    val uiState: StateFlow<CalibrationState> = _uiState.asStateFlow()
    val state = dm.calibrationState
    private var calibrationJob: Job? = null
    private var metronomePlayer: MediaPlayer? = null

    fun startCalibration() {
        if (_uiState.value.isCalibrating) return

        _uiState.value = CalibrationState(
            isCalibrating = true,
        )

        calibrationJob = viewModelScope.launch {
            // Запускаем метроном
            startMetronome()
            // Завершаем калибровку, если она не была отменена
            if (_uiState.value.isCalibrating) {
                completeCalibration()
            }
        }
    }

    fun cancelCalibration() {
        calibrationJob?.cancel()
        stopMetronome()

        _uiState.value = CalibrationState(
            isCalibrating = false,
            isComplete = false,
        )
    }

    private fun completeCalibration() {
        stopMetronome()

        _uiState.value = CalibrationState(
            isCalibrating = false,
            isComplete = true,
            progress = 1f,
            timeRemaining = 0L
        )
    }

    private fun startMetronome() {
        try {
            // Получаем ID ресурса по имени файла
            val resourceId = context.resources.getIdentifier(
                "metronom",
                "raw",
                context.packageName
            )

            if (resourceId != 0) {
                metronomePlayer = MediaPlayer.create(context, resourceId)
                metronomePlayer?.isLooping = true
                metronomePlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun stopMetronome() {
        try {
            metronomePlayer?.stop()
            metronomePlayer?.release()
            metronomePlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMetronome()
        calibrationJob?.cancel()
    }
}
