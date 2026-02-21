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
    private val metricsRepository: MetricsRepository){
    private var _instance = this
    init {
        _instance = this
        Log.d("RecordManager", "init")
    }


    private val capsuleDM = deviceManager
    private val _scope = CoroutineScope(EmptyCoroutineContext)
    private var isRecording = false

    private var isSetup = false
    private val _nfbState = MutableStateFlow(NFBData())
    private var session = java.sql.Timestamp(System.currentTimeMillis())

    //КОСТЫЛЬ КОСТЫЛЬ КОСТЫЛЬ КОСТЫЛЬ КОСТЫЛЬ
    private var id = "01010101" // здесь также нужна табличка.

    private var exp_id = "01" // здесь также надо подвязать shared perference


    fun setSession(Tsession: Timestamp){
        session = Tsession
    }


    fun startRecording() {
        if(!isSetup) setupCapsuleListeners()
        isRecording = true
        Log.d("Record Manager", "Recording started")
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
        capsuleDM.nfbReceived = {time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float ->
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

    private fun saveNFBData(time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        metricsRepository.saveNFBMetric(time, id, exp_id, session, alpha, beta, theta, delta, smr)
        Log.d("MainScreenViewModel", "NFB data saved: alpha=$alpha, beta=$beta")
    }

    private fun saveEEGRAWData(time: Long, channel1: Float, channel2: Float) {
        metricsRepository.saveEEGRAWMetric(time, id, exp_id, session,channel1, channel2)
    }

    private fun saveEEGPROCEEDData(time: Long, channel1: Float, channel2: Float) {
        metricsRepository.saveEEGPROCEEDMetric(time, id, exp_id, session, channel1, channel2)
    }

    private fun saveEEGArtifactData(time: Long, ArtifactChannel1: Boolean, ArtifactChannel2: Boolean, QualityChannel1: Float, QualityChannel2: Float){
        metricsRepository.saveEEGArtifactMetric(time, id, exp_id, session, ArtifactChannel1, ArtifactChannel2, QualityChannel1, QualityChannel2)
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
    }

    private fun saveCardioData(time: Long, heartRate: Float, hasArtifacts: Boolean, kaplanIndex: Float, metricsAvailable: Boolean, motionArtifact: Boolean, skinContact: Boolean, stressIndex: Float) {
        metricsRepository.saveCardioMetric(time, id, exp_id, session, heartRate, hasArtifacts, kaplanIndex, metricsAvailable, motionArtifact, skinContact, stressIndex)
    }

    private fun saveMEMSData(
        time: Long, accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float
    ) {
        metricsRepository.saveMEMSMetric(time, id, exp_id, session,accX, accY, accZ, gyroX, gyroY, gyroZ)
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
            session,
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
            session,
            attention,
            relaxation,
            cognitiveLoad,
            cognitiveControl,
            selfControl
        )
    }






}