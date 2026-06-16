package com.neuroproject.neuro.services

import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.repository.SensorStreamGateway
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataCollector @Inject constructor(
    private val sensorStreamGateway: SensorStreamGateway
) {
    fun observeNFB(): Flow<NFBSample> = sensorStreamGateway.observeNFB()

    fun observeHR(): Flow<CardioSample> = sensorStreamGateway.observeHR()

    fun observePhysiological(): Flow<PhysiologicalSample> = sensorStreamGateway.observePhysiological()

    fun observeMEMS(): Flow<MEMSSample> = sensorStreamGateway.observeMEMS()

    fun observeProductivity(): Flow<ProductivitySample> = sensorStreamGateway.observeProductivity()

    fun observeEmotional(): Flow<EmotionalSample> = sensorStreamGateway.observeEmotional()

    fun observeEEGRaw(): Flow<EEGRawSample> = sensorStreamGateway.observeEEGRaw()

    fun observeEEGProcessed(): Flow<EEGProcessedSample> = sensorStreamGateway.observeEEGProcessed()

    fun observeEEGArtifacts(): Flow<EEGArtifactSample> = sensorStreamGateway.observeEEGArtifacts()

    fun observeProductivityBaseline(): Flow<ProductivityBaselineSample> = sensorStreamGateway.observeProductivityBaseline()

    fun observeProductivityIndexes(): Flow<ProductivityIndexSample> = sensorStreamGateway.observeProductivityIndexes()

    fun observePhysiologicalBaseline(): Flow<PhysiologicalBaselineSample> = sensorStreamGateway.observePhysiologicalBaseline()

    fun observeProductivityScore(): Flow<ProductivityScoreSample> = sensorStreamGateway.observeProductivityScore()
}
