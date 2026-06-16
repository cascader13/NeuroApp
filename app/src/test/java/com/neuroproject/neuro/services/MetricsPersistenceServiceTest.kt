package com.neuroproject.neuro.services

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.data.MetricsRepository
import com.neuroproject.neuro.domain.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

class MetricsPersistenceServiceTest {

    private lateinit var service: MetricsPersistenceService
    private val metricsRepository: MetricsRepository = mock()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        service = MetricsPersistenceService(metricsRepository)
    }

    @Test
    fun `saveNFBMetric delegates to repository with correct parameters`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = NFBSample(
            timestamp = 1000L,
            alpha = 0.5f,
            beta = 0.3f,
            theta = 0.2f,
            delta = 0.1f,
            smr = 0.4f
        )

        service.saveNFBMetric(sessionId, userId, expeditionId, data)

        verify(metricsRepository).saveNFBMetric(
            time = 1000L,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            alpha = 0.5f,
            beta = 0.3f,
            theta = 0.2f,
            delta = 0.1f,
            smr = 0.4f
        )
    }

    @Test
    fun `savePhysiologicalMetric delegates to repository with correct parameters`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = PhysiologicalSample(
            timestamp = 1000L,
            relaxation = 0.7f,
            fatigue = 0.3f,
            none = 0.1f,
            concentration = 0.8f,
            involvement = 0.6f,
            stress = 0.2f,
            nfbArtifacts = false,
            cardioArtifacts = false
        )

        service.savePhysiologicalMetric(sessionId, userId, expeditionId, data)

        verify(metricsRepository).savePhysiologicalMetric(
            time = 1000L,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            relax = 0.7f,
            fatigue = 0.3f,
            none = 0.1f,
            concentration = 0.8f,
            involvement = 0.6f,
            stress = 0.2f,
            nfbArtifacts = false,
            cardioArtifacts = false
        )
    }

    @Test
    fun `saveCardioMetric delegates to repository with correct parameters`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = CardioSample(
            timestamp = 1000L,
            heartRate = 72f,
            hasArtifacts = false,
            kaplanIndex = 0.5f,
            metricsAvailable = true,
            motionArtifacts = false,
            skinContact = true,
            stress = 0.3f
        )

        service.saveCardioMetric(sessionId, userId, expeditionId, data)

        verify(metricsRepository).saveCardioMetric(
            time = 1000L,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            heartRate = 72f,
            hasArtifacts = false,
            kaplanIndex = 0.5f,
            metricsAvailable = true,
            motionAtrifacts = false,
            skinContact = true,
            stressIndex = 0.3f
        )
    }

    @Test
    fun `saveMEMSMetric delegates to repository with correct parameters`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = MEMSSample(
            timestamp = 1000L,
            accelerometerX = 0.1f,
            accelerometerY = 0.2f,
            accelerometerZ = 9.8f,
            gyroscopeX = 0.01f,
            gyroscopeY = 0.02f,
            gyroscopeZ = 0.03f
        )

        service.saveMEMSMetric(sessionId, userId, expeditionId, data)

        verify(metricsRepository).saveMEMSMetric(
            time = 1000L,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            accX = 0.1f,
            accY = 0.2f,
            accZ = 9.8f,
            gyroX = 0.01f,
            gyroY = 0.02f,
            gyroZ = 0.03f
        )
    }

    @Test
    fun `saveProductivityMetric delegates to repository with correct parameters`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = ProductivitySample(
            timestamp = 1000L,
            gravity = 0.5f,
            productivity = 0.8f,
            fatigue = 0.2f,
            reverseFatigue = 0.7f,
            relaxation = 0.6f,
            concentration = 0.9f
        )

        service.saveProductivityMetric(sessionId, userId, expeditionId, data)

        verify(metricsRepository).saveProductivityMetric(
            time = 1000L,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            gravity = 0.5f,
            productivity = 0.8f,
            fatigue = 0.2f,
            reverseFatigue = 0.7f,
            relaxation = 0.6f,
            concentration = 0.9f
        )
    }

    @Test
    fun `saveEmotionalMetric delegates to repository with correct parameters`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = EmotionalSample(
            timestamp = 1000L,
            attention = 0.7f,
            relaxation = 0.6f,
            cognitiveLoad = 0.3f,
            cognitiveControl = 0.8f,
            selfControl = 0.5f
        )

        service.saveEmotionalMetric(sessionId, userId, expeditionId, data)

        verify(metricsRepository).saveEmotionalMetric(
            time = 1000L,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            attention = 0.7f,
            relaxation = 0.6f,
            cognitiveLoad = 0.3f,
            cognitiveControl = 0.8f,
            selfControl = 0.5f
        )
    }

    @Test
    fun `saveEEGRawMetric delegates to repository with correct parameters`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = EEGRawSample(
            timestamp = 1000L,
            channel1 = 0.5f,
            channel2 = 0.3f
        )

        service.saveEEGRawMetric(sessionId, userId, expeditionId, data)

        verify(metricsRepository).saveEEGRAWMetric(
            time = 1000L,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            channel1 = 0.5f,
            channel2 = 0.3f
        )
    }

    @Test
    fun `saveEEGProcessedMetric delegates to repository with correct parameters`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = EEGProcessedSample(
            timestamp = 1000L,
            channel1 = 0.5f,
            channel2 = 0.3f
        )

        service.saveEEGProcessedMetric(sessionId, userId, expeditionId, data)

        verify(metricsRepository).saveEEGPROCEEDMetric(
            time = 1000L,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            channel1 = 0.5f,
            channel2 = 0.3f
        )
    }

    @Test
    fun `saveEEGArtifactsMetric delegates to repository with correct parameters`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = EEGArtifactSample(
            timestamp = 1000L,
            artifactChannel1 = true,
            artifactChannel2 = false,
            qualityChannel1 = 0.9f,
            qualityChannel2 = 0.8f
        )

        service.saveEEGArtifactsMetric(sessionId, userId, expeditionId, data)

        verify(metricsRepository).saveEEGArtifactMetric(
            time = 1000L,
            id = userId,
            exp_id = expeditionId,
            sessionId = sessionId,
            artifactsChannel1 = true,
            artifactsChannel2 = false,
            qualityChannel1 = 0.9f,
            qualityChannel2 = 0.8f
        )
    }

    @Test
    fun `saveProductivityIndexData with empty userId saves calibration`() {
        val sessionId = 1L
        val userId = ""
        val expeditionId = "exp_456"
        val data = ProductivityIndexSample(
            timestamp = 1000L,
            relaxation = "relax",
            stress = "low",
            gravityBaseline = 0.5f,
            productivityBaseline = 0.8f,
            fatigueBaseline = 0.2f,
            reverseFatiqueBaseline = 0.7f,
            relaxationBaseline = 0.6f,
            concentrationBaseline = 0.9f,
            hasArtifacts = false
        )

        service.saveProductivityIndexData(sessionId, userId, expeditionId, data)

        verify(metricsRepository).saveProductivityCalibration(
            userId = userId,
            gravityBaseline = 0.5f,
            productivityBaseline = 0.8f,
            fatiqueBaseline = 0.2f,
            reverseFatiqueBaseline = 0.7f,
            relaxationBaselines = 0.6f,
            concentrationBaselines = 0.9f
        )
        verify(metricsRepository, never()).saveProductivityIndexes(
            time = 1000L,
            id = userId,
            expedition_id = expeditionId,
            sessionId = sessionId,
            relaxation = "relax",
            stress = "low",
            gravityBaseline = 0.5f,
            productivityBaseline = 0.8f,
            fatiqueBaseline = 0.2f,
            reverseFatiqueBaseline = 0.7f,
            relaxationBaselines = 0.6f,
            concentrationBaselines = 0.9f,
            hasArtifacts = false
        )
    }

    @Test
    fun `saveProductivityIndexData with valid userId saves index`() {
        val sessionId = 1L
        val userId = "user_123"
        val expeditionId = "exp_456"
        val data = ProductivityIndexSample(
            timestamp = 1000L,
            relaxation = "relax",
            stress = "low",
            gravityBaseline = 0.5f,
            productivityBaseline = 0.8f,
            fatigueBaseline = 0.2f,
            reverseFatiqueBaseline = 0.7f,
            relaxationBaseline = 0.6f,
            concentrationBaseline = 0.9f,
            hasArtifacts = false
        )

        service.saveProductivityIndexData(sessionId, userId, expeditionId, data)

        verify(metricsRepository, never()).saveProductivityCalibration(
            userId = userId,
            gravityBaseline = 0.5f,
            productivityBaseline = 0.8f,
            fatiqueBaseline = 0.2f,
            reverseFatiqueBaseline = 0.7f,
            relaxationBaselines = 0.6f,
            concentrationBaselines = 0.9f
        )
        verify(metricsRepository).saveProductivityIndexes(
            time = 1000L,
            id = userId,
            expedition_id = expeditionId,
            sessionId = sessionId,
            relaxation = "relax",
            stress = "low",
            gravityBaseline = 0.5f,
            productivityBaseline = 0.8f,
            fatiqueBaseline = 0.2f,
            reverseFatiqueBaseline = 0.7f,
            relaxationBaselines = 0.6f,
            concentrationBaselines = 0.9f,
            hasArtifacts = false
        )
    }

    @Test
    fun `flushAllBuffers delegates to repository`() = runTest {
        service.flushAllBuffers()

        verify(metricsRepository).flushAllBuffers()
    }
}
