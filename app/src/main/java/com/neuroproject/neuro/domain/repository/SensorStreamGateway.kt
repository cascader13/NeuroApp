// domain/repository/SensorStreamGateway.kt
package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SensorStreamGateway {
    // Основные потоки
    fun observeNFB(): Flow<NFBSample>
    fun observeHR(): Flow<CardioSample>
    fun observePhysiological(): Flow<PhysiologicalSample>
    fun observeMEMS(): Flow<MEMSSample>
    fun observeProductivity(): Flow<ProductivitySample>
    fun observeEmotional(): Flow<EmotionalSample>
    fun observeEEGRaw(): Flow<EEGRawSample>
    fun observeEEGProcessed(): Flow<EEGProcessedSample>
    fun observeEEGArtifacts(): Flow<EEGArtifactSample>

    // Дополнительные потоки (калибровка)
    fun observeProductivityBaseline(): Flow<ProductivityBaselineSample>
    fun observeProductivityIndexes(): Flow<ProductivityIndexSample>
    fun observePhysiologicalBaseline(): Flow<PhysiologicalBaselineSample>
    fun observeProductivityScore(): Flow<ProductivityScoreSample>

    // Объединённый поток всех событий
    fun observeAll(): Flow<SensorEvent>
}

/**
 * Sealed interface для всех событий сенсоров.
 * Теперь включает все типы данных, включая калибровочные.
 */
sealed interface SensorEvent {
    // Основные типы
    data class NFB(val data: NFBSample) : SensorEvent
    data class HR(val data: CardioSample) : SensorEvent
    data class Physiological(val data: PhysiologicalSample) : SensorEvent
    data class MEMS(val data: MEMSSample) : SensorEvent
    data class Productivity(val data: ProductivitySample) : SensorEvent
    data class Emotional(val data: EmotionalSample) : SensorEvent
    data class EEGRaw(val data: EEGRawSample) : SensorEvent
    data class EEGProcessed(val data: EEGProcessedSample) : SensorEvent
    data class EEGArtifact(val data: EEGArtifactSample) : SensorEvent

    // Калибровочные типы
    data class ProductivityBaseline(val data: ProductivityBaselineSample) : SensorEvent
    data class ProductivityIndexes(val data: ProductivityIndexSample) : SensorEvent
    data class PhysiologicalBaseline(val data: PhysiologicalBaselineSample) : SensorEvent
    data class ProductivityScore(val data: ProductivityScoreSample) : SensorEvent
}