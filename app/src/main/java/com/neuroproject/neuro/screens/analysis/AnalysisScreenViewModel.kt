package com.neuroproject.neuro.screens.analysis

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.neuroproject.neuro.data.MetricsRepository
import com.neuroproject.neuro.services.CapsuleDeviceManager
import com.neuroproject.neuro.services.NFBData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Модель представления для экрана анализа данных(в релизе не попадёт)
 *
 * Отвечает за визуализацию данных нейрофидбека в реальном времени,
 * управление записью данных и сохранение метрик в базу данных.
 *
 * ## Основные функции:
 * - Отображение графиков альфа, бета и дельта ритмов
 * - Управление записью данных (старт/стоп)
 * - Сохранение метрик в базу данных с интервалом 1 секунда
 * - Автоматическое обновление UI при получении новых данных
 *
 * ## Потоки данных:
 * - **NFB данные** - альфа, бета, тета, дельта, SMR ритмы
 * - **Физиологические данные** - расслабление, утомление, концентрация, стресс
 * - **Кардио данные** - ЧСС, индекс Каплана
 * - **MEMS данные** - акселерометр и гироскоп
 * - **ЭЭГ данные** - сырые, обработанные и артефакты
 *
 * @property capsuleDM Менеджер устройства для получения данных
 * @property metricsRepository Репозиторий для сохранения метрик
 * @see CapsuleDeviceManager
 * @see MetricsRepository
 */
@HiltViewModel
class AnalysisScreenViewModel @Inject constructor(
    dm: CapsuleDeviceManager,
    @ApplicationContext private val context: Context,
    private val metricsRepository: MetricsRepository
) : ViewModel() {

    /** Менеджер устройства */
    val capsuleDM = dm

    private val _scope = CoroutineScope(EmptyCoroutineContext)

    /** Поток данных NFB */
    private val _nfbState = MutableStateFlow(NFBData())
    val nfb = _nfbState.asStateFlow()

    /** Поток данных для графиков */
    private val _plotData = MutableStateFlow(
        PlotData(
            alphaPoints = emptyList(),
            betaPoints = emptyList(),
            deltaPoints = emptyList()
        )
    )
    val plotData = _plotData.asStateFlow()

    private var timeCounter = 0f
    private val timeStep = 0.1f
    private val maxPoints = 200 // Максимальное количество точек на графике

    /** Флаг активной записи */
    private var isRecording = false
    private var lastSaveTime = 0L

    // ID сессии (временный костыль)
    private var date: Long = 5

    // Временный костыль для получения ID пользователя
    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    private var id = sharedPreferences.getString("saved_password", "").toString()
    private var exp_id = "01"

    /** Интервал сохранения данных (1 секунда) */
    private val saveInterval = 1000L

    init {
        setupCapsuleListeners()
    }

    /**
     * Настройка слушателей данных от устройства
     *
     * Подписывается на все потоки данных и настраивает:
     * - Обновление UI при получении новых данных
     * - Автоматическое сохранение в БД при активной записи
     * - Обновление графиков в реальном времени
     */
    private fun setupCapsuleListeners() {
        // Слушатель для NFB данных
        capsuleDM.nfbReceived = { time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float ->
            _scope.launch {
                _nfbState.emit(NFBData(time, alpha, beta, theta, delta, smr))

                // Обновление графиков
                val newAlphaPoint = DataPoint(timeCounter, alpha)
                val newBetaPoint = DataPoint(timeCounter, beta)
                val newDeltaPoint = DataPoint(timeCounter, delta)

                updatePlotData { current ->
                    current.copy(
                        alphaPoints = (current.alphaPoints + newAlphaPoint).takeLast(maxPoints),
                        betaPoints = (current.betaPoints + newBetaPoint).takeLast(maxPoints),
                        deltaPoints = (current.deltaPoints + newDeltaPoint).takeLast(maxPoints)
                    )
                }

                // Сохранение данных при активной записи
                if (isRecording) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastSaveTime >= saveInterval) {
                        saveNFBData(time, alpha, beta, theta, delta, smr)
                        lastSaveTime = currentTime
                    }
                }

                timeCounter += timeStep
            }
        }

        // Аналогичные слушатели для других типов данных...
        // (остальные методы опущены для краткости, их структура аналогична)
    }

    /**
     * Сохранение NFB данных
     */
    private fun saveNFBData(time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        metricsRepository.saveNFBMetric(time, id, exp_id, date, alpha, beta, theta, delta, smr)
        Log.d("MainScreenViewModel", "NFB data saved: alpha=$alpha, beta=$beta")
    }

    // ... (остальные методы сохранения аналогичны)

    /**
     * Начало записи данных
     */
    fun startRecording() {
        isRecording = true
        lastSaveTime = System.currentTimeMillis()
        Log.d("MainScreenViewModel", "Recording started")
    }

    /**
     * Остановка записи данных
     */
    fun stopRecording() {
        isRecording = false
        Log.d("MainScreenViewModel", "Recording stopped")
    }

    /**
     * Очистка всех метрик из базы данных
     */
    fun clearDatabase() {
        metricsRepository.clearAllMetrics()
        Log.d("MainScreenViewModel", "Database cleared")
    }

    /**
     * Проверка статуса записи
     */
    fun isRecording(): Boolean = isRecording

    /**
     * Очистка данных графиков
     */
    fun clearPlotData() {
        _scope.launch {
            updatePlotData {
                PlotData(emptyList(), emptyList(), emptyList())
            }
            timeCounter = 0f
        }
    }

    private fun updatePlotData(transform: (PlotData) -> PlotData) {
        _plotData.value = transform(_plotData.value)
    }
}

/**
 * Точка данных для графика
 *
 * @property x Временная координата
 * @property y Значение метрики
 */
data class DataPoint(
    val x: Float,
    val y: Float
)

/**
 * Данные для всех графиков
 *
 * @property alphaPoints Точки альфа-ритма
 * @property betaPoints Точки бета-ритма
 * @property deltaPoints Точки дельта-ритма
 */
data class PlotData(
    val alphaPoints: List<DataPoint>,
    val betaPoints: List<DataPoint>,
    val deltaPoints: List<DataPoint>
)

/**
 * Вспомогательная функция для сбора Flow данных
 */
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectInScope(
    scope: CoroutineScope,
    action: (T) -> Unit
) {
    scope.launch {
        this@collectInScope.collect { value ->
            action(value)
        }
    }
}