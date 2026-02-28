package com.neuroproject.neuro.screens.analysis

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.neuroproject.neuro.data.MetricsRepository
import com.neuroproject.neuro.services.CapsuleDeviceManager
import com.neuroproject.neuro.services.NFBData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.boguszpawlowski.composecalendar.kotlinxDateTime.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

// Модель для точки данных графика
data class DataPoint(
    val x: Float, // время
    val y: Float  // значение
)

// Модель для хранения данных всех графиков
data class PlotData(
    val alphaPoints: List<DataPoint>,
    val betaPoints: List<DataPoint>,
    val deltaPoints: List<DataPoint>
)

@HiltViewModel
class AnalysisScreenViewModel @Inject constructor(
    dm: CapsuleDeviceManager,
    @ApplicationContext private val context: Context,
    private val metricsRepository: MetricsRepository
) : ViewModel() {
    val capsuleDM = dm
    private val _scope = CoroutineScope(EmptyCoroutineContext)
    private val _nfbState = MutableStateFlow(NFBData())
    val nfb = _nfbState.asStateFlow()

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

    // Флаги для управления записью
    private var isRecording = false
    private var lastSaveTime = 0L


    private var date = java.sql.Timestamp(System.currentTimeMillis())


    // !!!!Костыль. С появлением настроек его нужно убрать!!!!
    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    private var id = sharedPreferences.getString("saved_password", "").toString()

    private var exp_id = "01"
    private val saveInterval = 1000L // Сохранять каждую секунду

    init {
        setupCapsuleListeners()
    }

    private fun setupCapsuleListeners() {
        // Слушатель для NFB данных
        capsuleDM.nfbReceived = {time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float ->
            _scope.launch {
                // Обновляем NFB данные
                _nfbState.emit(NFBData(time, alpha, beta, theta, delta, smr))

                // Добавляем точки на графики
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

                // Автоматически сохраняем данные при записи
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

        // Слушатель для физиологических данных
        capsuleDM.physiologicalData.collectInScope(_scope) { data ->
            if (isRecording) {
                savePhysiologicalData(
                    data.timeStampMilli,
                    data.relax,
                    data.fatigue,
                    data.none,
                    data.concentration,
                    data.involvement,
                    data.stress,
                    data.nfbArtifacts,
                    data.cardioArtifacts
                )
            }
        }

        // Слушатель для кардио данных
        capsuleDM.hrData.collectInScope(_scope) { hr ->
            if (isRecording) {
                saveCardioData(hr.timeStampMilli,
                    hr.heartRate,
                    hr.hasArtifacts,
                    hr.kaplanIndex,
                    hr.metricsAvailable,
                    hr.motionArtifacts,
                    hr.skinContact,
                    hr.stress)
            }
        }

        // Слушатель для MEMS данных
        capsuleDM.memsData.collectInScope(_scope) { mems ->
            if (isRecording) {
                saveMEMSData(
                    mems.timeStampMilli,
                    mems.accelerometer_x,
                    mems.accelerometer_y,
                    mems.accelerometer_z,
                    mems.gyroscope_x,
                    mems.gyroscope_y,
                    mems.gyroscope_z
                )
            }
        }

        // Слушатель для продуктивности
        capsuleDM.productivityData.collectInScope(_scope) { productivity ->
            if (isRecording) {
                saveProductivityData(
                    productivity.timeStampMilli,
                    productivity.gravity,
                    productivity.productivity,
                    productivity.fatigue,
                    productivity.reverse_fatique,
                    productivity.relaxation,
                    productivity.concentration
                )
            }
        }

        // Слушатель для эмоциональных данных
        capsuleDM.emotionalData.collectInScope(_scope) { emotion ->
            if (isRecording) {
                saveEmotionalData(
                    emotion.timeStampMilli,
                    emotion.attention,
                    emotion.relaxation,
                    emotion.cognitive_load,
                    emotion.cognitive_control,
                    emotion.self_control
                )
            }
        }
        capsuleDM.eegRawData.collectInScope(_scope) {eegRaw ->
            if (isRecording) {
                saveEEGRAWData(eegRaw.timeStampMilli, eegRaw.channel1, eegRaw.channel2)
            }
        }

        capsuleDM.eegProcessedData.collectInScope(_scope) {eegProceed ->
            if(isRecording){
                saveEEGPROCEEDData(eegProceed.timeStampMilli, eegProceed.channel1, eegProceed.channel2)
            }
        }

        capsuleDM.eegArtifacts.collectInScope(_scope){eegArt ->
            if(isRecording){
                saveEEGArtifactData(eegArt.timeStampMilli,
                    eegArt.artifactsChannel1,
                    eegArt.artifactsChannel2,
                    eegArt.qualityChannel1,
                    eegArt.qualityChannel2)
            }
        }


    }

    // Методы для сохранения данных в БД
    private fun saveNFBData(time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        metricsRepository.saveNFBMetric(time, id, exp_id, date, alpha, beta, theta, delta, smr)
        Log.d("MainScreenViewModel", "NFB data saved: alpha=$alpha, beta=$beta")
    }

    private fun saveEEGRAWData(time: Long, channel1: Float, channel2: Float) {
        metricsRepository.saveEEGRAWMetric(time, id, exp_id, date,channel1, channel2)
    }

    private fun saveEEGPROCEEDData(time: Long, channel1: Float, channel2: Float) {
        metricsRepository.saveEEGPROCEEDMetric(time, id, exp_id, date, channel1, channel2)
    }

    private fun saveEEGArtifactData(time: Long, ArtifactChannel1: Boolean, ArtifactChannel2: Boolean, QualityChannel1: Float, QualityChannel2: Float){
        metricsRepository.saveEEGArtifactMetric(time, id, exp_id, date, ArtifactChannel1, ArtifactChannel2, QualityChannel1, QualityChannel2)
    }

    private fun savePhysiologicalData(
        time: Long,
        relax: Float,
        fatigue: Float,
        none: Float,
        concentration: Float,
        involvement: Float,
        stress: Float,
        nfbArtifacts: Boolean,
        cardioArtifacts: Boolean
    ) {
        metricsRepository.savePhysiologicalMetric(
            time,
            id,
            exp_id,
            date,
            relax,
            fatigue,
            none,
            concentration,
            involvement,
            stress,
            nfbArtifacts,
            cardioArtifacts
        )
    }

    private fun saveCardioData(time: Long, heartRate: Float, hasArtifacts: Boolean, kaplanIndex: Float, metricsAvailable: Boolean, motionArtifact: Boolean, skinContact: Boolean, stressIndex: Float) {
        metricsRepository.saveCardioMetric(time, id, exp_id, date, heartRate, hasArtifacts, kaplanIndex, metricsAvailable, motionArtifact, skinContact, stressIndex)
    }

    private fun saveMEMSData(
        time: Long, accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float
    ) {
        metricsRepository.saveMEMSMetric(time, id, exp_id, date,accX, accY, accZ, gyroX, gyroY, gyroZ)
    }

    private fun saveProductivityData(
        time: Long,
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    ) {
        metricsRepository.saveProductivityMetric(
            time,
            id,
            exp_id,
            date,
            gravity,
            productivity,
            fatigue,
            reverseFatigue,
            relaxation,
            concentration
        )
    }

    private fun saveEmotionalData(
        time: Long,
        attention: Float,
        relaxation: Float,
        cognitiveLoad: Float,
        cognitiveControl: Float,
        selfControl: Float
    ) {
        metricsRepository.saveEmotionalMetric(
            time,
            id,
            exp_id,
            date,
            attention,
            relaxation,
            cognitiveLoad,
            cognitiveControl,
            selfControl
        )
    }

    // Методы управления записью
    fun startRecording() {
        isRecording = true
        lastSaveTime = System.currentTimeMillis()
        Log.d("MainScreenViewModel", "Recording started")
    }

    fun stopRecording() {
        isRecording = false
        Log.d("MainScreenViewModel", "Recording stopped")
    }

    fun clearDatabase() {
        metricsRepository.clearAllMetrics()
        Log.d("MainScreenViewModel", "Database cleared")
    }

    fun isRecording(): Boolean = isRecording

    // Очистка данных графиков
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

// Extension function для удобного сбора Flow данных
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