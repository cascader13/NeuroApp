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
import android.content.SharedPreferences
import com.neuroproject.neuro.data.MetricsDao

data class CalibrationState(
    val isCalibrating: Boolean = false,
    val isTrueCalibrating: Boolean = false,
    val isComplete: Boolean = false,
    val progress: Float = 0f,
    val timeRemaining: Long = 60000L,

    // Новое состояние для отображения диалога
    val showPreviousCalibrationDialog: Boolean = false
)

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val MetricsDao: MetricsDao,
    dm: CapsuleDeviceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalibrationState())
    val uiState: StateFlow<CalibrationState> = _uiState.asStateFlow()
    val state = dm.calibrationState
    private var calibrationJob: Job? = null
    private var metronomePlayer: MediaPlayer? = null
    private val totalCalibrationTime = 90000L // 90 секунд

    // Флаг для отслеживания, показывался ли уже диалог в текущей сессии
    private var dialogShown = false
    private var checkInitialized = false

    init {
        viewModelScope.launch {
            // Проверяем наличие предыдущих данных калибровки при инициализации
            checkPreviousCalibrationData()
        }
    }

    private suspend fun checkPreviousCalibrationData() {
        try {
            // Получаем текущего пользователя из SharedPreferences
            val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val currentUserId = sharedPreferences.getString("saved_user_id", "")

            // Если есть текущий пользователь, проверяем его предыдущие калибровки
            if (!currentUserId.isNullOrEmpty()) {
                val previousCalibrations = MetricsDao.getCalibration(currentUserId)
                // Если есть хотя бы одна предыдущая калибровка для этого пользователя,
                // показываем диалог (если он еще не показывался)
                if (previousCalibrations.isNotEmpty() && !dialogShown) {
                    showPreviousCalibrationDialog()
                }
            }

            checkInitialized = true
        } catch (e: Exception) {
            Log.e("CalibrationViewModel", "Error checking previous calibration data", e)
            checkInitialized = true
        }
    }


    fun showPreviousCalibrationDialog() {
        dialogShown = true
        _uiState.value = _uiState.value.copy(
            showPreviousCalibrationDialog = true
        )
    }


    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showPreviousCalibrationDialog = false
        )
    }


    fun usePreviousCalibrationData() {
        // Здесь логика загрузки предыдущих данных калибровки
        Log.d("Calibration", "Using previous calibration data")

        // Получаем текущего пользователя
        val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("saved_user_id", "")

        if (!currentUserId.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    // Получаем последнюю калибровку пользователя
                    val previousCalibrations = MetricsDao.getCalibration(currentUserId)
                    if (previousCalibrations.isNotEmpty()) {
                        val lastCalibration = previousCalibrations[0]
                        // Здесь можно использовать данные из lastCalibration
                        // Например, загрузить параметры калибровки в устройство
                        Log.d("Calibration", "Loaded calibration data: $lastCalibration")

                        // Сохраняем флаг, что использовали старые данные
                        val calibrationPrefs = context.getSharedPreferences("calibration_prefs", Context.MODE_PRIVATE)
                        calibrationPrefs.edit()
                            .putBoolean("use_previous_calibration", true)
                            .putLong("last_calibration_id", lastCalibration.id)
                            .apply()

                        // Можно сразу завершить калибровку или перейти к следующему экрану
                        completeCalibrationWithPreviousData()
                    }
                } catch (e: Exception) {
                    Log.e("Calibration", "Error loading previous calibration", e)
                }
            }
        }

        // Закрываем диалог
        dismissDialog()
    }


    private fun completeCalibrationWithPreviousData() {
        _uiState.value = CalibrationState(
            isCalibrating = false,
            isComplete = true,
            progress = 1f,
            timeRemaining = 0L
        )
    }


    fun performNewCalibration() {
        Log.d("Calibration", "Performing new calibration")

        val sharedPreferences = context.getSharedPreferences("calibration_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean("use_previous_calibration", false)
            .putLong("last_calibration_id", -1)
            .apply()

        dismissDialog()

    }

    fun startCalibration() {
        if (_uiState.value.isCalibrating) return

        _uiState.value = CalibrationState(
            isCalibrating = true,
            progress = 0f,
            timeRemaining = totalCalibrationTime
        )

        calibrationJob = viewModelScope.launch {
            startMetronome()

            val interval = 100L
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

    fun forceStopMetronome() {
        stopMetronome()
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