// domain/repository/MetricsRepository.kt
package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий хранения и агрегации метрик сенсоров.
 *
 * Управляет сырыми данными от устройства и их сжатием (компрессией)
 * в минутные интервалы. Каждый тип данных имеет свой буфер,
 * который периодически сбрасывается в БД.
 *
 * Типы данных:
 * - NFB (нейро-фидбек): альфа, бета, тета, дельта, SMR
 * - Cardio: ЧСС, артефакты
 * - Physiological: релакс, усталость, стресс, концентрация
 * - MEMS: акселерометр, гироскоп
 * - Productivity: продуктивность, концентрация, усталость
 * - Emotional: внимание, расслабление, когнитивная нагрузка
 * - EEG Raw/Processed/Artifact: сырые и обработанные данные ЭЭГ
 */
interface MetricsRepository {

    /** Сохраняет сырые данные нейро-фидбека. */
    suspend fun saveNFB(sample: NFBSample)
    /** Наблюдает за данными нейро-фидбека. */
    fun observeNFB(): Flow<NFBSample>

    /** Сохраняет данные сердечного ритма. */
    suspend fun saveCardio(sample: CardioSample)
    /** Наблюдает за данными сердечного ритма. */
    fun observeCardio(): Flow<CardioSample>

    /** Сохраняет физиологические метрики. */
    suspend fun savePhysiological(sample: PhysiologicalSample)
    /** Наблюдает за физиологическими метриками. */
    fun observePhysiological(): Flow<PhysiologicalSample>

    /** Сохраняет данные MEMS (акселерометр/гироскоп). */
    suspend fun saveMEMS(sample: MEMSSample)
    /** Наблюдает за данными MEMS. */
    fun observeMEMS(): Flow<MEMSSample>

    /** Сохраняет метрики продуктивности. */
    suspend fun saveProductivity(sample: ProductivitySample)
    /** Наблюдает за метриками продуктивности. */
    fun observeProductivity(): Flow<ProductivitySample>

    /** Сохраняет базовый уровень продуктивности (калибровка). */
    suspend fun saveProductivityBaseline(sample: ProductivityBaselineSample)
    /** Наблюдает за базовым уровнем продуктивности. */
    fun observeProductivityBaseline(): Flow<ProductivityBaselineSample>

    /** Сохраняет индексы продуктивности (результат калибровки). */
    suspend fun saveProductivityIndexes(sample: ProductivityIndexSample)
    /** Наблюдает за индексами продуктивности. */
    fun observeProductivityIndexes(): Flow<ProductivityIndexSample>

    /** Сохраняет базовый уровень физиологии (калибровка). */
    suspend fun savePhysiologicalBaseline(sample: PhysiologicalBaselineSample)
    /** Наблюдает за базовым уровнем физиологии. */
    fun observePhysiologicalBaseline(): Flow<PhysiologicalBaselineSample>

    /** Сохраняет эмоциональные метрики. */
    suspend fun saveEmotional(sample: EmotionalSample)
    /** Наблюдает за эмоциональными метриками. */
    fun observeEmotional(): Flow<EmotionalSample>

    /** Сохраняет сырые данные ЭЭГ. */
    suspend fun saveEEGRaw(sample: EEGRawSample)
    /** Наблюдает за сырыми данными ЭЭГ. */
    fun observeEEGRaw(): Flow<EEGRawSample>

    /** Сохраняет обработанные данные ЭЭГ. */
    suspend fun saveEEGProcessed(sample: EEGProcessedSample)
    /** Наблюдает за обработанными данными ЭЭГ. */
    fun observeEEGProcessed(): Flow<EEGProcessedSample>

    /** Сохраняет данные об артефактах ЭЭГ. */
    suspend fun saveEEGArtifact(sample: EEGArtifactSample)
    /** Наблюдает за данными об артефактах ЭЭГ. */
    fun observeEEGArtifact(): Flow<EEGArtifactSample>

    /**
     * Возвращает агрегированные данные за сессию для выгрузки на сервер.
     *
     * @param sessionId ID сессии (строка).
     * @return [AggregatedSessionData] со всеми типами сэмплов.
     */
    suspend fun getAggregatedDataForSession(sessionId: String): AggregatedSessionData

    /** Очищает все метрики из БД. */
    suspend fun clearAllMetrics()

    /** Принудительно сбрасывает все буферы в БД. */
    suspend fun flushAllBuffers()

    /** Очищает все метрики по ID сессии. */
    suspend fun clearAllMetricsBySessionId(sessionId: Long)
}

/**
 * Агрегированные данные сессии для выгрузки.
 *
 * Содержит списки сэмплов всех типов, собранных за время сессии.
 */
data class AggregatedSessionData(
    val sessionId: String,
    val nfbSamples: List<NFBSample>,
    val cardioSamples: List<CardioSample>,
    val physiologicalSamples: List<PhysiologicalSample>,
    val memsSamples: List<MEMSSample>,
    val productivitySamples: List<ProductivitySample>,
    val emotionalSamples: List<EmotionalSample>
)