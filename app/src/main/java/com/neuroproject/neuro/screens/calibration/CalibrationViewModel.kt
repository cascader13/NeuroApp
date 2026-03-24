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
import com.neuroproject.neuro.data.MetricsDao

/**
 * Модель представления для экрана калибровки
 *
 * Управляет процессом калибровки нейро-гарнитуры, включая:
 * - Проверку наличия предыдущих данных калибровки
 * - Воспроизведение метронома во время калибровки
 * - Отслеживание прогресса и оставшегося времени
 * - Сохранение результатов калибровки
 *
 * ## Процесс калибровки:
 * 1. Проверка наличия предыдущих данных калибровки пользователя
 * 2. Выбор: использовать существующие данные или выполнить новую калибровку
 * 3. При новой калибровке: 60 секунд с метрономом
 * 4. Сохранение результатов для последующего использования
 *
 * @property context Контекст приложения
 * @property MetricsDao DAO для работы с калибровочными данными
 * @property dm Менеджер устройства
 * @see CapsuleDeviceManager
 * @see CalibrationState
 */
@HiltViewModel
class CalibrationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val MetricsDao: MetricsDao,
    val dm: CapsuleDeviceManager
) : ViewModel() {

    /** UI состояние экрана калибровки */
    private val _uiState = MutableStateFlow(CalibrationState())
    val uiState: StateFlow<CalibrationState> = _uiState.asStateFlow()

    /** Поток состояния калибровки от устройства */
    val state = dm.calibrationState

    private var calibrationJob: Job? = null
    private var metronomePlayer: MediaPlayer? = null

    /** Общее время калибровки (60 секунд) */
    private val totalCalibrationTime = 60000L

    /** Флаг отслеживания показа диалога */
    private var dialogShown = false
    private var checkInitialized = false

    init {
        viewModelScope.launch {
            checkPreviousCalibrationData()
        }
    }

    /**
     * Проверка наличия предыдущих данных калибровки
     *
     * При наличии данных показывает диалог выбора.
     */
    private suspend fun checkPreviousCalibrationData() {
        try {
            val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val currentUserId = sharedPreferences.getString("saved_user_id", "")

            if (!currentUserId.isNullOrEmpty()) {
                val previousCalibrations = MetricsDao.getCalibration(currentUserId)
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

    /**
     * Показать диалог выбора использования предыдущих данных
     */
    fun showPreviousCalibrationDialog() {
        dialogShown = true
        _uiState.value = _uiState.value.copy(
            showPreviousCalibrationDialog = true
        )
    }

    /**
     * Закрыть диалог
     */
    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showPreviousCalibrationDialog = false
        )
    }

    /**
     * Использовать предыдущие данные калибровки
     *
     * Загружает последние сохраненные данные для текущего пользователя.
     */
    fun usePreviousCalibrationData() {
        Log.d("Calibration", "Using previous calibration data")

        val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("saved_user_id", "")

        if (!currentUserId.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val previousCalibrations = MetricsDao.getCalibration(currentUserId)
                    if (previousCalibrations.isNotEmpty()) {
                        val lastCalibration = previousCalibrations[0]
                        Log.d("Calibration", "Loaded calibration data: $lastCalibration")

                        val calibrationPrefs = context.getSharedPreferences("calibration_prefs", Context.MODE_PRIVATE)
                        calibrationPrefs.edit()
                            .putBoolean("use_previous_calibration", true)
                            .putLong("last_calibration_id", lastCalibration.id)
                            .apply()

                        completeCalibrationWithPreviousData()
                    }
                } catch (e: Exception) {
                    Log.e("Calibration", "Error loading previous calibration", e)
                }
            }
        }

        dismissDialog()
    }

    /**
     * Завершить калибровку с предыдущими данными
     */
    private fun completeCalibrationWithPreviousData() {
        _uiState.value = CalibrationState(
            isCalibrating = false,
            isComplete = true,
            progress = 1f,
            timeRemaining = 0L
        )
    }

    /**
     * Выполнить новую калибровку
     */
    fun performNewCalibration() {
        Log.d("Calibration", "Performing new calibration")

        val sharedPreferences = context.getSharedPreferences("calibration_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean("use_previous_calibration", false)
            .putLong("last_calibration_id", -1)
            .apply()

        dismissDialog()
    }

    /**
     * Начать процесс калибровки
     *
     * Запускает таймер, метроном и сбор данных с устройства.
     */
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

            if (_uiState.value.isCalibrating) {
                completeCalibration()
            }
        }
        dm.startSignalAndHR()
    }

    /**
     * Принудительная остановка метронома
     */
    fun forceStopMetronome() {
        stopMetronome()
    }

    /**
     * Отмена калибровки
     */
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

    /**
     * Завершение калибровки
     */
    private fun completeCalibration() {
        stopMetronome()
        _uiState.value = CalibrationState(
            isCalibrating = false,
            isComplete = true,
            progress = 1f,
            timeRemaining = 0L
        )
    }

    /**
     * Запуск метронома
     *
     * Воспроизводит звуковой файл metronom.mp3 из ресурсов.
     */
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

    /**
     * Остановка метронома
     */
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

/**
 * Состояние экрана калибровки
 *
 * @property isCalibrating Активен ли процесс калибровки
 * @property isTrueCalibrating Флаг реальной калибровки (не используется)
 * @property isComplete Завершена ли калибровка
 * @property progress Прогресс калибровки (0-1)
 * @property timeRemaining Оставшееся время в миллисекундах
 * @property showPreviousCalibrationDialog Показывать ли диалог выбора
 */
data class CalibrationState(
    val isCalibrating: Boolean = false,
    val isTrueCalibrating: Boolean = false,
    val isComplete: Boolean = false,
    val progress: Float = 0f,
    val timeRemaining: Long = 60000L,
    val showPreviousCalibrationDialog: Boolean = false
)