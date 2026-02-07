
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

data class CalibrationState(

    var ClosedEyes: Boolean = true,
    val isCalibrating: Boolean = false,
    val isComplete: Boolean = false,
    val progress: Float = 0f,
    val timeRemaining: Long = 60000L
)

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    dm: CapsuleDeviceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalibrationState())
    val uiState: StateFlow<CalibrationState> = _uiState.asStateFlow()
    val state = dm.calibrationState
    private var calibrationJob: Job? = null
    private var metronomePlayer: MediaPlayer? = null
    private val totalCalibrationTime = 60000L // 60 секунд

    fun startCalibration() {
        if (_uiState.value.isCalibrating) return

        _uiState.value = CalibrationState(
            isCalibrating = true,
            progress = 0f,
            timeRemaining = totalCalibrationTime
        )

        calibrationJob = viewModelScope.launch {
            // Запускаем метроном
            startMetronome()

            val interval = 100L // Обновление каждые 100 мс
            val steps = (totalCalibrationTime / interval).toInt()

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
            progress = 0f,
            timeRemaining = totalCalibrationTime
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
