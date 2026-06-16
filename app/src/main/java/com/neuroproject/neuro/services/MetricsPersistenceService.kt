package com.neuroproject.neuro.services

import android.util.Log
import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.data.MetricsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetricsPersistenceService @Inject constructor(
    private val metricsRepository: MetricsRepository
) {
    fun saveNFBMetric(sessionId: Long, userId: String, expeditionId: String, data: NFBSample) {
        metricsRepository.saveNFBMetric(
            time = data.timestamp,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            alpha = data.alpha,
            beta = data.beta,
            theta = data.theta,
            delta = data.delta,
            smr = data.smr
        )
    }

    fun savePhysiologicalMetric(sessionId: Long, userId: String, expeditionId: String, data: PhysiologicalSample) {
        metricsRepository.savePhysiologicalMetric(
            time = data.timestamp,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            relax = data.relaxation,
            fatigue = data.fatigue,
            none = data.none,
            concentration = data.concentration,
            involvement = data.involvement,
            stress = data.stress,
            nfbArtifacts = data.nfbArtifacts,
            cardioArtifacts = data.cardioArtifacts
        )
    }

    fun saveCardioMetric(sessionId: Long, userId: String, expeditionId: String, data: CardioSample) {
        metricsRepository.saveCardioMetric(
            time = data.timestamp,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            heartRate = data.heartRate,
            hasArtifacts = data.hasArtifacts,
            kaplanIndex = data.kaplanIndex,
            metricsAvailable = data.metricsAvailable,
            motionAtrifacts = data.motionArtifacts,
            skinContact = data.skinContact,
            stressIndex = data.stress
        )
    }

    fun saveMEMSMetric(sessionId: Long, userId: String, expeditionId: String, data: MEMSSample) {
        metricsRepository.saveMEMSMetric(
            time = data.timestamp,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            accX = data.accelerometerX,
            accY = data.accelerometerY,
            accZ = data.accelerometerZ,
            gyroX = data.gyroscopeX,
            gyroY = data.gyroscopeY,
            gyroZ = data.gyroscopeZ
        )
    }

    fun saveProductivityMetric(sessionId: Long, userId: String, expeditionId: String, data: ProductivitySample) {
        metricsRepository.saveProductivityMetric(
            time = data.timestamp,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            gravity = data.gravity,
            productivity = data.productivity,
            fatigue = data.fatigue,
            reverseFatigue = data.reverseFatigue,
            relaxation = data.relaxation,
            concentration = data.concentration
        )
    }

    fun saveProductivityIndexData(sessionId: Long, userId: String, expeditionId: String, data: ProductivityIndexSample) {
        if (userId.isEmpty()) {
            metricsRepository.saveProductivityCalibration(
                userId = userId,
                gravityBaseline = data.gravityBaseline,
                productivityBaseline = data.productivityBaseline,
                fatiqueBaseline = data.fatigueBaseline,
                reverseFatiqueBaseline = data.reverseFatiqueBaseline,
                relaxationBaselines = data.relaxationBaseline,
                concentrationBaselines = data.concentrationBaseline
            )
        }
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveProductivityIndexes(
                time = data.timestamp,
                id = userId,
                expedition_id = expeditionId,
                sessionId = sessionId,
                relaxation = data.relaxation,
                stress = data.stress,
                gravityBaseline = data.gravityBaseline,
                productivityBaseline = data.productivityBaseline,
                fatiqueBaseline = data.fatigueBaseline,
                reverseFatiqueBaseline = data.reverseFatiqueBaseline,
                relaxationBaselines = data.relaxationBaseline,
                concentrationBaselines = data.concentrationBaseline,
                hasArtifacts = data.hasArtifacts
            )
        }
    }

    fun saveProductivityBaselineData(sessionId: Long, userId: String, expeditionId: String, data: ProductivityBaselineSample) {
        metricsRepository.saveProductivityBaselines(
            time = data.timestamp,
            id = userId,
            expedition_id = expeditionId,
            sessionId = sessionId,
            gravity = data.gravity,
            productivity = data.productivity,
            fatigue = data.fatigue,
            reverseFatigue = data.reverse_fatique,
            relaxation = data.relaxation,
            concentration = data.concentration
        )
    }

    fun savePhysiologicalBaselineData(sessionId: Long, userId: String, expeditionId: String, data: PhysiologicalBaselineSample) {
        if (userId.isNotEmpty()) {
            metricsRepository.savePhysiologicalCalibration(
                userId = userId,
                alpha = data.alpha,
                beta = data.beta,
                alphaGravity = data.alphaGravity,
                betaGravity = data.betaGravity,
                concentration = data.concentration
            )
        }
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.savePhysiologicalBaselines(
                time = data.timestamp,
                id = userId,
                expedition_id = expeditionId,
                sessionId = sessionId,
                alpha = data.alpha,
                beta = data.beta,
                alphaGravity = data.alphaGravity,
                betaGravity = data.betaGravity,
                concentration = data.concentration
            )
        }
    }

    fun saveEmotionalMetric(sessionId: Long, userId: String, expeditionId: String, data: EmotionalSample) {
        metricsRepository.saveEmotionalMetric(
            time = data.timestamp,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            attention = data.attention,
            relaxation = data.relaxation,
            cognitiveLoad = data.cognitiveLoad,
            cognitiveControl = data.cognitiveControl,
            selfControl = data.selfControl
        )
    }

    fun saveEEGRawMetric(sessionId: Long, userId: String, expeditionId: String, data: EEGRawSample) {
        metricsRepository.saveEEGRAWMetric(
            time = data.timestamp,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            channel1 = data.channel1,
            channel2 = data.channel2
        )
    }

    fun saveEEGProcessedMetric(sessionId: Long, userId: String, expeditionId: String, data: EEGProcessedSample) {
        metricsRepository.saveEEGPROCEEDMetric(
            time = data.timestamp,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            channel1 = data.channel1,
            channel2 = data.channel2
        )
    }

    fun saveEEGArtifactsMetric(sessionId: Long, userId: String, expeditionId: String, data: EEGArtifactSample) {
        metricsRepository.saveEEGArtifactMetric(
            time = data.timestamp,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            artifactsChannel1 = data.artifactChannel1,
            artifactsChannel2 = data.artifactChannel2,
            qualityChannel1 = data.qualityChannel1,
            qualityChannel2 = data.qualityChannel2
        )
    }

    suspend fun flushAllBuffers() {
        metricsRepository.flushAllBuffers()
    }
}
