package com.neuroproject.neuro.jni

import android.content.Context
import android.util.Log
import com.neuroproject.neuro.data.CalibrationHistoryEntity
import kotlinx.coroutines.launch

/**
 * Чистый JNI мост — вызывается из C++ кода.
 * Не содержит бизнес-логики, только передаёт данные дальше.
 */
object JniCallbackHandler {


    //Костыль для JNI(для singletone не генерит getInstanse)
    @JvmStatic
    fun getInstance(): JniCallbackHandler = this
    
    var onDeviceFound: ((name: String, address: String) -> Unit)? = null
    var onConnectionStateChanged: ((state: Int) -> Unit)? = null
    var onResistanceReceived: ((o1: Double, o2: Double, t3: Double, t4: Double) -> Unit)? = null
    var onCardioReceived: ((
        time: Long, heartRate: Float, hasArtifacts: Boolean, kaplanIndex: Float,
        metricsAvailable: Boolean, motionArtifacts: Boolean, skinContact: Boolean, stress: Float
    ) -> Unit)? = null
    var onNFBReceived: ((time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) -> Unit)? = null
    var onMEMSReceived: ((time: Long, accx: Float, accy: Float, accz: Float,
                          hyrx: Float, hyry: Float, hyrz: Float) -> Unit)? = null
    var onPhysiologicalReceived: ((
        time: Long, relax: Float, fatigue: Float, none: Float,
        concentration: Float, involvement: Float, stress: Float,
        nfbArtifacts: Boolean, cardioArtifacts: Boolean
    ) -> Unit)? = null
    var onProductivityReceived: ((
        time: Long, timestampProd: Double, gravity: Float,
        productivity: Float, fatigue: Float, reverseFatigue: Float,
        relaxation: Float, concentration: Float
    ) -> Unit)? = null
    var onEmotionReceived: ((
        time: Long, attention: Float, relaxation: Float,
        cognitiveLoad: Float, cognitiveControl: Float, selfControl: Float
    ) -> Unit)? = null
    var onEEGRawDataReceived: ((timeStampMilli: Long, channel1: Float, channel2: Float) -> Unit)? = null
    var onEEGProcessedDataReceived: ((timeStampMilli: Long, channel1: Float, channel2: Float) -> Unit)? = null
    var onEEGArtifactsReceived: ((
        timeStampMilli: Long, artifacts1: Boolean, artifacts2: Boolean,
        quality1: Float, quality2: Float
    ) -> Unit)? = null
    var onBatteryChargeReceived: ((value: Float) -> Unit)? = null
    var onCalibrationReceived: ((
        indFrequency: Float, indPeakFrequency: Float, indPeakFrequencyPower: Float,
        indPeakFrequencySuppression: Float, indBandwidth: Float, indNormalizedPower: Float,
        lowerFrequency: Float, upperFrequency: Float
    ) -> Unit)? = null
    var onCalibrationStateChanged: ((stageNum: Int) -> Unit)? = null
    var onProductivityBaselineReceived: ((
        time: Long, gravity: Float, productivity: Float, fatigue: Float,
        reverseFatigue: Float, relaxation: Float, concentration: Float
    ) -> Unit)? = null
    var onProductivityIndexesReceived: ((
        time: Long, relaxation: Float, stress: Float, gravityBaseline: Float,
        productivityBaseline: Float, fatigueBaseline: Float, reverseFatigueBaseline: Float,
        relaxationBaseline: Float, concentrationBaseline: Float, hasArtifacts: Boolean
    ) -> Unit)? = null
    var onProductivityScore: ((score: Float) -> Unit)? = null
    var onPhysiologicalBaselineReceived: ((
        time: Long, alpha: Float, beta: Float,
        alphaGravity: Float, betaGravity: Float, concentration: Float
    ) -> Unit)? = null

    // Методы для вызова из C++
    @Suppress("unused")
    fun onDeviceFound(name: String, address: String) {
        onDeviceFound?.invoke(name, address)
    }

    @Suppress("unused")
    fun deviceConnectionState(state: Int) {
        onConnectionStateChanged?.invoke(state)
    }

    @Suppress("unused")
    fun onNFBReceived(time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        onNFBReceived?.invoke(time, alpha, beta, theta, delta, smr)
    }

    @Suppress("unused")
    fun onResistanceReceived(o1: Double, o2: Double, t3: Double, t4: Double) {
        onResistanceReceived?.invoke(o1 / 10e3, o2 / 10e3, t3 / 10e3, t4 / 10e3)
    }

    @Suppress("unused")
    fun onCardioReceived(
        time: Long, heartRate: Float, hasArtifacts: Boolean, kaplanIndex: Float,
        metricsAvailable: Boolean, motionArtifacts: Boolean, skinContact: Boolean, stress: Float
    ) {
        onCardioReceived?.invoke(time, heartRate, hasArtifacts, kaplanIndex, metricsAvailable, motionArtifacts, skinContact, stress)
    }

    @Suppress("unused")
    fun onMEMSReceived(
        time: Long, accx: Float, accy: Float, accz: Float,
        hyrx: Float, hyry: Float, hyrz: Float
    ) {
        onMEMSReceived?.invoke(time, accx, accy, accz, hyrx, hyry, hyrz)
    }

    @Suppress("unused")
    fun onPhysiologicalReceived(
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
        onPhysiologicalReceived?.invoke(time, relax, fatigue, none, concentration, involvement, stress, nfbArtifacts, cardioArtifacts)
    }

    @Suppress("unused")
    fun onProductivityReceived(
        time: Long, timestamp_prod: Double, gravity: Float,
        productivity: Float, fatigue: Float, reverse_fatique: Float,
        relaxation: Float, concentration: Float
    ) {
        onProductivityReceived?.invoke(time, timestamp_prod, gravity, productivity, fatigue, reverse_fatique, relaxation, concentration)
    }

    @Suppress("unused")
    fun onEmotionReceived(
        time: Long, attention: Float, relaxation: Float,
        cognitive_load: Float, cognitive_control: Float, self_control: Float
    ) {
        onEmotionReceived?.invoke(time, attention, relaxation, cognitive_load, cognitive_control, self_control)
    }

    @Suppress("unused")
    fun onEEGRawDataReceived(
        timeStampMilli: Long,
        channel1: Float,
        channel2: Float
    ) {
        onEEGRawDataReceived?.invoke(timeStampMilli, channel1, channel2)
    }

    @Suppress("unused")
    fun onEEGProcessedDataReceived(
        timeStampMilli: Long,
        channel1: Float,
        channel2: Float
    ) {
        onEEGProcessedDataReceived?.invoke(timeStampMilli, channel1, channel2)
    }

    @Suppress("unused")
    fun onEEGArtifactsReceived(
        timeStampMilli: Long,
        artifacts1: Boolean,
        artifacts2: Boolean,
        quality1: Float,
        quality2: Float
    ) {
        onEEGArtifactsReceived?.invoke(timeStampMilli, artifacts1, artifacts2, quality1, quality2)
    }

    @Suppress("unused")
    fun onBatteryChargeReceived(value:Float) {
        onBatteryChargeReceived?.invoke(value)
    }

    @Suppress("unused")
    fun onCalibrationReceived(
        indFrequency: Float, indPeakFrequency: Float, indPeakFrequencyPower: Float,
        indPeakFrequencySuppression: Float, indBandwidth: Float, indNormalizedPower: Float,
        lowerFrequency: Float, upperFrequency: Float
    ) {
        onCalibrationReceived?.invoke(indFrequency,  indPeakFrequency, indPeakFrequencyPower, indPeakFrequencySuppression, indBandwidth, indNormalizedPower, lowerFrequency, upperFrequency)
    }

    @Suppress("unused")
    fun onCalibrationStateReceived(StageNum: Int){
        onCalibrationStateChanged?.invoke(StageNum)
    }

    @Suppress("unused")
    fun onProductivityBaselineReceived(
        time: Long, gravity: Float, productivity: Float, fatigue: Float,
        reverseFatigue: Float, relaxation: Float, concentration: Float
    ){
       onProductivityBaselineReceived?.invoke(time, gravity, productivity, fatigue, reverseFatigue, relaxation, concentration)

    }

    @Suppress("unused")
    fun onProductivityIndexesReceived(
    time: Long, relaxation: Float, stress: Float, gravityBaseline: Float,
    productivityBaseline: Float, fatigueBaseline: Float, reverseFatigueBaseline: Float,
    relaxationBaseline: Float, concentrationBaseline: Float, hasArtifacts: Boolean
    ){
        onProductivityIndexesReceived?.invoke(time, relaxation, stress, gravityBaseline, productivityBaseline, fatigueBaseline, reverseFatigueBaseline, relaxationBaseline, concentrationBaseline, hasArtifacts)
    }

    @Suppress("unused")
    fun onProductivityScore(score: Float){
        onProductivityScore?.invoke(score)
    }

    @Suppress("unused")
    fun onPhysiologicalBaselineReceived(
        time: Long, alpha: Float, beta: Float,
        alphaGravity: Float, betaGravity: Float, concentration: Float){
        onPhysiologicalBaselineReceived?.invoke(time, alpha, beta, alphaGravity, betaGravity, concentration)
    }





}