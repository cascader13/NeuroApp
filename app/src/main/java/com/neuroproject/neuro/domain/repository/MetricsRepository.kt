// domain/repository/MetricsRepository.kt
package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория метрик для Domain слоя
 *
 * Скрывает детали реализации (Room, база данных)
 */
interface MetricsRepository {

    suspend fun saveNFB(sample: NFBSample)
    fun observeNFB(): Flow<NFBSample>

    suspend fun saveCardio(sample: CardioSample)
    fun observeCardio(): Flow<CardioSample>

    suspend fun savePhysiological(sample: PhysiologicalSample)
    fun observePhysiological(): Flow<PhysiologicalSample>


    suspend fun saveMEMS(sample: MEMSSample)
    fun observeMEMS(): Flow<MEMSSample>

    suspend fun saveProductivity(sample: ProductivitySample)
    fun observeProductivity(): Flow<ProductivitySample>

    suspend fun saveProductivityBaseline(sample: ProductivityBaselineSample)
    fun observeProductivityBaseline(): Flow<ProductivityBaselineSample>

    suspend fun saveProductivityIndexes(sample: ProductivityIndexSample)
    fun observeProductivityIndexes(): Flow<ProductivityIndexSample>

    suspend fun savePhysiologicalBaseline(sample: PhysiologicalBaselineSample)
    fun observePhysiologicalBaseline(): Flow<PhysiologicalBaselineSample>

    suspend fun saveEmotional(sample: EmotionalSample)
    fun observeEmotional(): Flow<EmotionalSample>

    suspend fun saveEEGRaw(sample: EEGRawSample)
    fun observeEEGRaw(): Flow<EEGRawSample>

    suspend fun saveEEGProcessed(sample: EEGProcessedSample)
    fun observeEEGProcessed(): Flow<EEGProcessedSample>

    suspend fun saveEEGArtifact(sample: EEGArtifactSample)
    fun observeEEGArtifact(): Flow<EEGArtifactSample>

    suspend fun getAggregatedDataForSession(sessionId: String): AggregatedSessionData

    suspend fun clearAllMetrics()
    suspend fun flushAllBuffers()
    suspend fun clearAllMetricsBySessionId(sessionId: Long)
}

data class AggregatedSessionData(
    val sessionId: String,
    val nfbSamples: List<NFBSample>,
    val cardioSamples: List<CardioSample>,
    val physiologicalSamples: List<PhysiologicalSample>,
    val memsSamples: List<MEMSSample>,
    val productivitySamples: List<ProductivitySample>,
    val emotionalSamples: List<EmotionalSample>
)