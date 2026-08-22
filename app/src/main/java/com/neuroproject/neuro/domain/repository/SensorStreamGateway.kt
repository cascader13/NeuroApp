// domain/repository/SensorStreamGateway.kt
package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Шлюз потоков данных сенсоров устройства.
 *
 * Предоставляет доступ к потокам данных различных типов сенсоров
 * в реальном времени. Используется во время записи сессии.
 *
 * Все потоки эмитят данные непрерывно до вызова [DeviceGateway.stopSession].
 */
interface SensorStreamGateway {
    /** Поток данных нейро-фидбека (ЭЭГ банды). */
    fun observeNFB(): Flow<NFBSample>
    /** Поток данных сердечного ритма (ЧСС, артефакты). */
    fun observeHR(): Flow<CardioSample>
    /** Поток физиологических метрик (релакс, усталость, стресс). */
    fun observePhysiological(): Flow<PhysiologicalSample>
    /** Поток данных MEMS (акселерометр, гироскоп). */
    fun observeMEMS(): Flow<MEMSSample>
    /** Поток метрик продуктивности. */
    fun observeProductivity(): Flow<ProductivitySample>
    /** Поток эмоциональных метрик (внимание, когнитивная нагрузка). */
    fun observeEmotional(): Flow<EmotionalSample>
    /** Поток сырых данных ЭЭГ. */
    fun observeEEGRaw(): Flow<EEGRawSample>
    /** Поток обработанных данных ЭЭГ. */
    fun observeEEGProcessed(): Flow<EEGProcessedSample>
    /** Поток данных об артефактах ЭЭГ. */
    fun observeEEGArtifacts(): Flow<EEGArtifactSample>

    /** Поток базового уровня продуктивности (калибровка). */
    fun observeProductivityBaseline(): Flow<ProductivityBaselineSample>
    /** Поток индексов продуктивности (результат калибровки). */
    fun observeProductivityIndexes(): Flow<ProductivityIndexSample>
    /** Поток базового уровня физиологии (калибровка). */
    fun observePhysiologicalBaseline(): Flow<PhysiologicalBaselineSample>
    /** Поток оценки продуктивности (калибровочный балл). */
    fun observeProductivityScore(): Flow<ProductivityScoreSample>

    /**
     * Объединённый поток всех событий сенсоров.
     *
     * Удобен для маршрутизации данных в соответствующие репозитории
     * без подписки на каждый поток отдельно.
     */
    fun observeAll(): Flow<SensorEvent>
}

/**
 * Sealed interface для всех событий сенсоров.
 *
 * Каждый тип обёрнут в data class с полем [data],
 * что позволяет единообразно маршрутизировать данные.
 */
sealed interface SensorEvent {
    data class NFB(val data: NFBSample) : SensorEvent
    data class HR(val data: CardioSample) : SensorEvent
    data class Physiological(val data: PhysiologicalSample) : SensorEvent
    data class MEMS(val data: MEMSSample) : SensorEvent
    data class Productivity(val data: ProductivitySample) : SensorEvent
    data class Emotional(val data: EmotionalSample) : SensorEvent
    data class EEGRaw(val data: EEGRawSample) : SensorEvent
    data class EEGProcessed(val data: EEGProcessedSample) : SensorEvent
    data class EEGArtifact(val data: EEGArtifactSample) : SensorEvent
    data class ProductivityBaseline(val data: ProductivityBaselineSample) : SensorEvent
    data class ProductivityIndexes(val data: ProductivityIndexSample) : SensorEvent
    data class PhysiologicalBaseline(val data: PhysiologicalBaselineSample) : SensorEvent
    data class ProductivityScore(val data: ProductivityScoreSample) : SensorEvent
}