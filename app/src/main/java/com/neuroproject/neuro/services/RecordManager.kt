package com.neuroproject.neuro.services

import android.content.Context
import android.util.Log
import com.neuroproject.neuro.data.MetricsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.sql.Timestamp
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

//Для полноценной реализации нужна табличка с сессиями


@Singleton
class RecordManager @Inject constructor(
    deviceManager: CapsuleDeviceManager,
    @ApplicationContext private val context: Context,
    private val metricsRepository: MetricsRepository
) {
    private var _instance = this

    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    init {
        _instance = this
        Log.d("RecordManager", "init")
        loadSavedIds()
    }

    private val capsuleDM = deviceManager
    private val _scope = CoroutineScope(EmptyCoroutineContext)
    private var isRecording = false

    private var isSetup = false
    private val _nfbState = MutableStateFlow(NFBData())
    private var session = java.sql.Timestamp(System.currentTimeMillis())



    // ID пользователя и экспедиции из SharedPreferences
    private var userId: String = ""
    private var expeditionId: String = ""

    private fun loadSavedIds() {
        userId = sharedPreferences.getString("saved_user_id", "") ?: ""

        // Загружаем expedition_id
        expeditionId = sharedPreferences.getString("saved_expedition_id", "") ?: ""

        Log.d("RecordManager", "Loaded IDs - userId: $userId, expeditionId: $expeditionId")
    }

    fun refreshIds() {
        loadSavedIds()
        Log.d("RecordManager", "IDs refreshed - userId: $userId, expeditionId: $expeditionId")
    }

    fun setSession(Tsession: Timestamp) {
        session = Tsession
    }

    fun startRecording() {
        if (!isSetup) setupCapsuleListeners()
        // Обновляем ID перед началом записи, чтобы использовать актуальные значения
        refreshIds()
        isRecording = true
        Log.d("Record Manager", "Recording started with userId: $userId, expeditionId: $expeditionId")
    }

    suspend fun stopRecording() {
        isRecording = false
        Log.d("Record Manager", "Recording stopped")
        metricsRepository.flushAllBuffers()
    }

    //Вспомогательная функция для сбора данных(нашёл на одном из форумов, весьма элегантное решение)
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

    private fun setupCapsuleListeners() {
        isSetup = true
        // Слушатель для NFB данных
        capsuleDM.nfbReceived = { time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float ->
            _scope.launch {
                // Обновляем NFB данные
                _nfbState.emit(NFBData(time, alpha, beta, theta, delta, smr))

                // Автоматически сохраняем данные при записи
                if (isRecording) {
                    saveNFBData(time, alpha, beta, theta, delta, smr)
                }
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
                saveCardioData(
                    hr.timeStampMilli,
                    hr.heartRate,
                    hr.hasArtifacts,
                    hr.kaplanIndex,
                    hr.metricsAvailable,
                    hr.motionArtifacts,
                    hr.skinContact,
                    hr.stress
                )
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

        capsuleDM.eegRawData.collectInScope(_scope) { eegRaw ->
            if (isRecording) {
                saveEEGRAWData(eegRaw.timeStampMilli, eegRaw.channel1, eegRaw.channel2)
            }
        }

        capsuleDM.eegProcessedData.collectInScope(_scope) { eegProceed ->
            if (isRecording) {
                saveEEGPROCEEDData(eegProceed.timeStampMilli, eegProceed.channel1, eegProceed.channel2)
            }
        }

        capsuleDM.eegArtifacts.collectInScope(_scope) { eegArt ->
            if (isRecording) {
                saveEEGArtifactData(
                    eegArt.timeStampMilli,
                    eegArt.artifactsChannel1,
                    eegArt.artifactsChannel2,
                    eegArt.qualityChannel1,
                    eegArt.qualityChannel2
                )
            }
        }
    }

    private fun saveNFBData(time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveNFBMetric(time, userId, expeditionId, session, alpha, beta, theta, delta, smr)
            Log.d("RecordManager", "NFB data saved: alpha=$alpha, beta=$beta")
        } else {
            Log.e("RecordManager", "Cannot save NFB data: userId or expeditionId is empty")
        }
    }

    private fun saveEEGRAWData(time: Long, channel1: Float, channel2: Float) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveEEGRAWMetric(time, userId, expeditionId, session, channel1, channel2)
        } else {
            Log.e("RecordManager", "Cannot save EEG RAW data: userId or expeditionId is empty")
        }
    }

    private fun saveEEGPROCEEDData(time: Long, channel1: Float, channel2: Float) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveEEGPROCEEDMetric(time, userId, expeditionId, session, channel1, channel2)
        } else {
            Log.e("RecordManager", "Cannot save EEG PROCEED data: userId or expeditionId is empty")
        }
    }

    private fun saveEEGArtifactData(
        time: Long,
        ArtifactChannel1: Boolean,
        ArtifactChannel2: Boolean,
        QualityChannel1: Float,
        QualityChannel2: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveEEGArtifactMetric(
                time,
                userId,
                expeditionId,
                session,
                ArtifactChannel1,
                ArtifactChannel2,
                QualityChannel1,
                QualityChannel2
            )
        } else {
            Log.e("RecordManager", "Cannot save EEG Artifact data: userId or expeditionId is empty")
        }
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
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.savePhysiologicalMetric(
                time,
                userId,
                expeditionId,
                session,
                relax,
                fatigue,
                none,
                concentration,
                involvement,
                stress,
                nfbArtifacts,
                cardioArtifacts
            )
        } else {
            Log.e("RecordManager", "Cannot save Physiological data: userId or expeditionId is empty")
        }
    }

    private fun saveCardioData(
        time: Long,
        heartRate: Float,
        hasArtifacts: Boolean,
        kaplanIndex: Float,
        metricsAvailable: Boolean,
        motionArtifact: Boolean,
        skinContact: Boolean,
        stressIndex: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveCardioMetric(
                time,
                userId,
                expeditionId,
                session,
                heartRate,
                hasArtifacts,
                kaplanIndex,
                metricsAvailable,
                motionArtifact,
                skinContact,
                stressIndex
            )
        } else {
            Log.e("RecordManager", "Cannot save Cardio data: userId or expeditionId is empty")
        }
    }

    private fun saveMEMSData(
        time: Long, accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveMEMSMetric(
                time,
                userId,
                expeditionId,
                session,
                accX, accY, accZ,
                gyroX, gyroY, gyroZ
            )
        } else {
            Log.e("RecordManager", "Cannot save MEMS data: userId or expeditionId is empty")
        }
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
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveProductivityMetric(
                time,
                userId,
                expeditionId,
                session,
                gravity,
                productivity,
                fatigue,
                reverseFatigue,
                relaxation,
                concentration
            )
        } else {
            Log.e("RecordManager", "Cannot save Productivity data: userId or expeditionId is empty")
        }
    }

    private fun saveEmotionalData(
        time: Long,
        attention: Float,
        relaxation: Float,
        cognitiveLoad: Float,
        cognitiveControl: Float,
        selfControl: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveEmotionalMetric(
                time,
                userId,
                expeditionId,
                session,
                attention,
                relaxation,
                cognitiveLoad,
                cognitiveControl,
                selfControl
            )
        } else {
            Log.e("RecordManager", "Cannot save Emotional data: userId or expeditionId is empty")
        }
    }
}