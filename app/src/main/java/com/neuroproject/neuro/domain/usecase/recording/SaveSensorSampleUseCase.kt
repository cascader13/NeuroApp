package com.neuroproject.neuro.domain.usecase.recording

import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.MetricsRepository
import javax.inject.Inject

/**
 * Сохраняет пример сенсорных данных в репозиторий метрик.
 *
 * Автоматически заполняет userId и expeditionId из текущей сессии.
 *
 * @param sample данные сенсора любого поддерживаемого типа
 */
class SaveSensorSampleUseCase @Inject constructor(
    private val metricsRepository: MetricsRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(sample: SensorSample) {
        val userId = authRepository.getUserId()
        val expeditionId = authRepository.getExpeditionId()
        when (sample) {
            is NFBSample -> metricsRepository.saveNFB(sample.copy(userId=userId, expeditionId = expeditionId))
            is CardioSample -> metricsRepository.saveCardio(sample.copy(userId=userId, expeditionId = expeditionId))
            is PhysiologicalSample -> metricsRepository.savePhysiological(sample.copy(userId=userId, expeditionId = expeditionId))
            is MEMSSample -> metricsRepository.saveMEMS(sample.copy(userId=userId, expeditionId = expeditionId))
            is ProductivitySample -> metricsRepository.saveProductivity(sample.copy(userId=userId, expeditionId = expeditionId))
            is ProductivityBaselineSample -> {metricsRepository.saveProductivityBaseline(sample.copy(userId=userId, expeditionId = expeditionId))
            }
            is ProductivityIndexSample -> metricsRepository.saveProductivityIndexes(sample.copy(userId=userId, expeditionId = expeditionId))
            is PhysiologicalBaselineSample -> metricsRepository.savePhysiologicalBaseline(sample.copy(userId=userId, expeditionId = expeditionId))
            is EmotionalSample -> metricsRepository.saveEmotional(sample.copy(userId=userId, expeditionId = expeditionId))
            is EEGRawSample -> metricsRepository.saveEEGRaw(sample.copy(userId=userId, expeditionId = expeditionId))
            is EEGProcessedSample -> metricsRepository.saveEEGProcessed(sample.copy(userId=userId, expeditionId = expeditionId))
            is EEGArtifactSample -> metricsRepository.saveEEGArtifact(sample.copy(userId=userId, expeditionId = expeditionId))
        }
    }
}