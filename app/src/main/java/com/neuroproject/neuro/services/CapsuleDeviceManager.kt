package com.neuroproject.neuro.services

import com.neuroproject.neuro.jni.JniCallbackHandler
import javax.inject.Inject
import javax.inject.Singleton

// services/CapsuleDeviceManager.kt
// ВАЖНО: этот класс находится в legacy package, который ожидает native-библиотека.
@Singleton
class CapsuleDeviceManager @Inject constructor() {

    // Приватные external функции
    private companion object {
        init {
            System.loadLibrary("native-lib")
        }

        private external fun nativeInitCapsule()
        private external fun nativeStartSearch()
        private external fun nativeConnect(id: String)
        private external fun nativeDisconnect()
        private external fun nativeStartResistance()
        private external fun nativeStopResistance()
        private external fun nativeStartSignalAndHR()
        private external fun nativeStopSignalAndHR()
        private external fun nativeStartSession()
        private external fun nativeStopSession()
        private external fun nativeStartProductivity()
        private external fun nativeImportCalibration(
            indFrequency: Float, indPeakFrequency: Float, indPeakFrequencyPower: Float,
            indPeakFrequencySuppression: Float, indBandwidth: Float, indNormalizedPower: Float,
            lowerFrequency: Float, upperFrequency: Float
        )
        private external fun nativeImportProductivityCalibration(
            gravity: Float, b_productivity: Float, fatigue: Float,
            reverseFatigue: Float, relaxation: Float, concentration: Float
        )
        private external fun nativeImportPhysiologicalCalibration(
            alpha: Float, beta: Float, alphaGravity: Float,
            betaGravity: Float, concentration: Float
        )
        private external fun removeAll()
    }

    // Публичные обёртки
    fun initCapsule() = nativeInitCapsule()
    fun startSearch() = nativeStartSearch()
    fun connect(id: String) = nativeConnect(id)
    fun disconnect() = nativeDisconnect()
    fun startResistance() = nativeStartResistance()
    fun stopResistance() = nativeStopResistance()
    fun startSignalAndHR() = nativeStartSignalAndHR()
    fun stopSignalAndHR() = nativeStopSignalAndHR()
    fun startSession() = nativeStartSession()
    fun stopSession() = nativeStopSession()
    fun startProductivity() = nativeStartProductivity()

    fun importCalibration(
        indFrequency: Float, indPeakFrequency: Float, indPeakFrequencyPower: Float,
        indPeakFrequencySuppression: Float, indBandwidth: Float, indNormalizedPower: Float,
        lowerFrequency: Float, upperFrequency: Float
    ) = nativeImportCalibration(indFrequency, indPeakFrequency, indPeakFrequencyPower,
        indPeakFrequencySuppression, indBandwidth, indNormalizedPower,
        lowerFrequency, upperFrequency)

    fun importProductivityCalibration(
        gravity: Float, b_productivity: Float, fatigue: Float,
        reverseFatigue: Float, relaxation: Float, concentration: Float
    ) = nativeImportProductivityCalibration(gravity, b_productivity, fatigue,
        reverseFatigue, relaxation, concentration)

    fun importPhysiologicalCalibration(
        alpha: Float, beta: Float, alphaGravity: Float,
        betaGravity: Float, concentration: Float
    ) = nativeImportPhysiologicalCalibration(alpha, beta, alphaGravity, betaGravity, concentration)

    fun removeAllResources() = removeAll()
}