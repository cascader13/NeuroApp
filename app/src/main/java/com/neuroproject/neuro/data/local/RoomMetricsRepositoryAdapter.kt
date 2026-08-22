// data/local/RoomMetricsRepositoryAdapter.kt
package com.neuroproject.neuro.data.local

import android.util.Log
import com.neuroproject.neuro.data.MetricsRepository as LegacyMetricsRepository
import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.CalibrationRepository
import com.neuroproject.neuro.domain.repository.MetricsRepository
import com.neuroproject.neuro.domain.repository.AggregatedSessionData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Адаптер, который реализует domain интерфейс MetricsRepository
 * и делегирует вызовы существующему MetricsRepository (который работает с Room)
 */
@Singleton
class RoomMetricsRepositoryAdapter @Inject constructor(
    private val legacyRepository: LegacyMetricsRepository,
    private val calibrationRepository: CalibrationRepository,
    private val authRepository: AuthRepository
) : MetricsRepository {


    override suspend fun saveNFB(sample: NFBSample) {
        legacyRepository.saveNFBMetric(
            time = sample.timestamp,
            id = sample.userId,
            exp_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            alpha = sample.alpha,
            beta = sample.beta,
            theta = sample.theta,
            delta = sample.delta,
            smr = sample.smr
        )
    }

    override fun observeNFB(): Flow<NFBSample> = flow {
        // TODO: Implement observation from database if needed
        // Для простоты пока не реализуем, так как данные приходят через SensorStreamGateway
    }


    override suspend fun saveCardio(sample: CardioSample) {
        legacyRepository.saveCardioMetric(
            time = sample.timestamp,
            id = sample.userId,
            exp_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            heartRate = sample.heartRate,
            hasArtifacts = sample.hasArtifacts,
            kaplanIndex = sample.kaplanIndex,
            metricsAvailable = sample.metricsAvailable,
            motionAtrifacts = sample.motionArtifacts,
            skinContact = sample.skinContact,
            stressIndex = sample.stress
        )
    }

    override fun observeCardio(): Flow<CardioSample> = flow { }


    override suspend fun savePhysiological(sample: PhysiologicalSample) {
        legacyRepository.savePhysiologicalMetric(
            time = sample.timestamp,
            id = sample.userId,
            exp_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            relax = sample.relaxation,
            fatigue = sample.fatigue,
            none = 0f,
            concentration = sample.concentration,
            involvement = sample.involvement,
            stress = sample.stress,
            nfbArtifacts = sample.nfbArtifacts,
            cardioArtifacts = sample.cardioArtifacts
        )
    }

    override fun observePhysiological(): Flow<PhysiologicalSample> = flow { }


    override suspend fun saveMEMS(sample: MEMSSample) {
        legacyRepository.saveMEMSMetric(
            time = sample.timestamp,
            id = sample.userId,
            exp_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            accX = sample.accelerometerX,
            accY = sample.accelerometerY,
            accZ = sample.accelerometerZ,
            gyroX = sample.gyroscopeX,
            gyroY = sample.gyroscopeY,
            gyroZ = sample.gyroscopeZ
        )
    }

    override fun observeMEMS(): Flow<MEMSSample> = flow { }


    override suspend fun saveProductivity(sample: ProductivitySample) {
        legacyRepository.saveProductivityMetric(
            time = sample.timestamp,
            id = sample.userId,
            exp_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            gravity = sample.gravity,
            productivity = sample.productivity,
            fatigue = sample.fatigue,
            reverseFatigue = sample.reverseFatigue,
            relaxation = sample.relaxation,
            concentration = sample.concentration
        )
    }

    override fun observeProductivity(): Flow<ProductivitySample> = flow { }


    override suspend fun saveProductivityBaseline(sample: ProductivityBaselineSample) {
        try {
            legacyRepository.saveProductivityBaselines(
                time = sample.timestamp,
                id = sample.userId,
                expedition_id = sample.expeditionId,
                sessionId = sample.sessionId.toLongOrNull() ?: 0L,
                gravity = sample.gravity,
                productivity = sample.productivity,
                fatigue = sample.fatigue,
                reverseFatigue = sample.reverse_fatique,
                relaxation = sample.relaxation,
                concentration = sample.concentration
            )
            updateCalibrationWithProductivityBaseline(sample)
        } catch (e: Exception) {
            Log.e("RoomMetricsRepo", "Error saving productivity baseline", e)
        }
    }

    private suspend fun updateCalibrationWithProductivityBaseline(sample: ProductivityBaselineSample) {
        try {
            val userId = authRepository.getUserId()
            calibrationRepository.updateProductivityCalibration(
                userId,
                sample.gravity,
                sample.productivity,
                sample.fatigue,
                sample.reverse_fatique,
                sample.relaxation,
                sample.concentration
            )
        } catch (e: Exception) {
            Log.e("RoomMetricsRepo", "Error updating calibration with productivity baseline", e)
        }
    }

    override fun observeProductivityBaseline(): Flow<ProductivityBaselineSample> = flow { }


    override suspend fun saveProductivityIndexes(sample: ProductivityIndexSample) {
        legacyRepository.saveProductivityIndexes(
            time = sample.timestamp,
            id = sample.userId,
            expedition_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            relaxation = sample.relaxation,
            stress = sample.stress,
            gravityBaseline = sample.gravityBaseline,
            productivityBaseline = sample.productivityBaseline,
            fatiqueBaseline = sample.fatigueBaseline,
            reverseFatiqueBaseline = sample.reverseFatiqueBaseline,
            relaxationBaselines = sample.relaxationBaseline,
            concentrationBaselines = sample.concentrationBaseline,
            hasArtifacts = sample.hasArtifacts
        )
    }

    override fun observeProductivityIndexes(): Flow<ProductivityIndexSample> = flow { }


    override suspend fun savePhysiologicalBaseline(sample: PhysiologicalBaselineSample) {
        try {
            legacyRepository.savePhysiologicalBaselines(
                time = sample.timestamp,
                id = sample.userId,
                expedition_id = sample.expeditionId,
                sessionId = sample.sessionId.toLongOrNull() ?: 0L,
                alpha = sample.alpha,
                beta = sample.beta,
                alphaGravity = sample.alphaGravity,
                betaGravity = sample.betaGravity,
                concentration = sample.concentration
            )
            updateCalibrationWithPhysiologicalBaseline(sample)
        } catch (e: Exception) {
            Log.e("RoomMetricsRepo", "Error saving physiological baseline", e)
        }
    }

    private suspend fun updateCalibrationWithPhysiologicalBaseline(sample: PhysiologicalBaselineSample) {
        try {
            val userId = authRepository.getUserId()
            calibrationRepository.updatePhysiologicalCalibration(
                userId,
                sample.alpha,
                sample.beta,
                sample.alphaGravity,
                sample.betaGravity,
                sample.concentration
            )
        } catch (e: Exception) {
            Log.e("RoomMetricsRepo", "Error updating calibration with physiological baseline", e)
        }
    }

    override fun observePhysiologicalBaseline(): Flow<PhysiologicalBaselineSample> = flow { }


    override suspend fun saveEmotional(sample: EmotionalSample) {
        legacyRepository.saveEmotionalMetric(
            time = sample.timestamp,
            id = sample.userId,
            exp_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            attention = sample.attention,
            relaxation = sample.relaxation,
            cognitiveLoad = sample.cognitiveLoad,
            cognitiveControl = sample.cognitiveControl,
            selfControl = sample.selfControl
        )
    }

    override fun observeEmotional(): Flow<EmotionalSample> = flow { }


    override suspend fun saveEEGRaw(sample: EEGRawSample) {
        legacyRepository.saveEEGRAWMetric(
            time = sample.timestamp,
            id = sample.userId,
            exp_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            channel1 = sample.channel1,
            channel2 = sample.channel2
        )
    }

    override fun observeEEGRaw(): Flow<EEGRawSample> = flow { }

    override suspend fun saveEEGProcessed(sample: EEGProcessedSample) {
        legacyRepository.saveEEGPROCEEDMetric(
            time = sample.timestamp,
            id = sample.userId,
            exp_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            channel1 = sample.channel1,
            channel2 = sample.channel2
        )
    }

    override fun observeEEGProcessed(): Flow<EEGProcessedSample> = flow { }

    override suspend fun saveEEGArtifact(sample: EEGArtifactSample) {
        legacyRepository.saveEEGArtifactMetric(
            time = sample.timestamp,
            id = sample.userId,
            exp_id = sample.expeditionId,
            sessionId = sample.sessionId.toLongOrNull() ?: 0L,
            artifactsChannel1 = sample.artifactChannel1,
            artifactsChannel2 = sample.artifactChannel2,
            qualityChannel1 = sample.qualityChannel1,
            qualityChannel2 = sample.qualityChannel2
        )
    }

    override fun observeEEGArtifact(): Flow<EEGArtifactSample> = flow { }


    override suspend fun getAggregatedDataForSession(sessionId: String): AggregatedSessionData {
        return AggregatedSessionData(
            sessionId = sessionId,
            nfbSamples = emptyList(),
            cardioSamples = emptyList(),
            physiologicalSamples = emptyList(),
            memsSamples = emptyList(),
            productivitySamples = emptyList(),
            emotionalSamples = emptyList()
        )
    }


    override suspend fun clearAllMetrics() {
        legacyRepository.clearAllMetrics()
    }

    override suspend fun flushAllBuffers() {
        legacyRepository.flushAllBuffers()
    }

    override suspend fun clearAllMetricsBySessionId(sessionId: Long) {
        legacyRepository.clearAllMetricsBySessionId(sessionId)
    }
}