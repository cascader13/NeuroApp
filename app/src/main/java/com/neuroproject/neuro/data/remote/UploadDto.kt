package com.neuroproject.neuro.data.remote

import com.google.gson.annotations.SerializedName
import com.neuroproject.neuro.data.session.SessionCategory

/**
 * Основной DTO (Data Transfer Object) для отправки метрик на сервер
 *
 * Содержит все типы данных, собранные во время сессии работы с нейро-гарнитурой.
 * Поддерживает как обычные, так и сжатые форматы данных для оптимизации трафика.
 *
 * ## Структура запроса:
 * - **Основные метрики** - данные в реальном времени (NFB, ЭЭГ, физиологические)
 * - **Сжатые метрики** - агрегированные данные для уменьшения объема
 * - **Базовые значения** - индивидуальные нормы пользователя
 *
 * ## Типы данных:
 * - **NFB** - спектральные характеристики ЭЭГ (альфа, бета, тета, дельта, SMR)
 * - **ЭЭГ** - сырые, обработанные сигналы и информация об артефактах
 * - **Физиологические** - расслабление, утомление, концентрация, вовлеченность, стресс
 * - **Кардио** - ЧСС, индекс Каплана, качество контакта
 * - **MEMS** - данные акселерометра и гироскопа
 * - **Продуктивность** - показатели эффективности когнитивной деятельности
 * - **Эмоциональные** - внимание, когнитивная нагрузка, самоконтроль
 *
 * @see NfbMetricDto
 * @see PhysiologicalMetricDto
 * @see EEGRawMetricDto
 */
data class UploadRequest(
    // ==================== ОСНОВНЫЕ МЕТРИКИ (UNCOMPRESSED) ====================

    /**
     * Метрики нейрофидбека (NFB)
     *
     * Содержат спектральные характеристики ЭЭГ сигнала:
     * - Альфа-ритм (8-13 Гц) - состояние покоя
     * - Бета-ритм (13-30 Гц) - активное мышление
     * - Тета-ритм (4-8 Гц) - дремотное состояние
     * - Дельта-ритм (0.5-4 Гц) - глубокий сон
     * - SMR (12-15 Гц) - сенсомоторный ритм
     */
    @SerializedName("nfbMetrics")
    val nfbMetrics: List<NfbMetricDto>? = emptyList(),

    /**
     * Физиологические метрики
     *
     * Комплексные показатели психофизиологического состояния:
     * - relax - уровень расслабления
     * - fatigue - уровень утомления
     * - concentration - уровень концентрации
     * - involvement - уровень вовлеченности
     * - stress - уровень стресса
     */
    @SerializedName("physiologicalMetrics")
    val physiologicalMetrics: List<PhysiologicalMetricDto>? = emptyList(),

    /**
     * Сырые данные ЭЭГ
     *
     * Необработанные сигналы с электродов (2 канала).
     * Используются для глубокого анализа и кастомной обработки.
     */
    @SerializedName("EEGRawMetrics")
    val EEGRawMetrics: List<EEGRawMetricDto>? = emptyList(),

    /**
     * Обработанные данные ЭЭГ
     *
     * Сигналы после фильтрации и удаления артефактов.
     * Оптимальны для большинства аналитических задач.
     */
    @SerializedName("EEGProceedMetrics")
    val EEGProceedMetrics: List<EEGProceedMetricDto>? = emptyList(),

    /**
     * Артефакты ЭЭГ
     *
     * Информация о качестве сигнала:
     * - Наличие артефактов на каждом канале
     * - Качество сигнала (0-1)
     */
    @SerializedName("EEGArtifactsMetrics")
    val EEGArtifactsMetrics: List<EEGArtifactMetricDto>? = emptyList(),

    /**
     * Данные MEMS-датчиков
     *
     * Показания акселерометра и гироскопа для отслеживания движений головы.
     * Используются для компенсации артефактов движения.
     */
    @SerializedName("memsMetrics")
    val memsMetrics: List<MemsMetricDto>? = emptyList(),

    /**
     * Метрики продуктивности
     *
     * Показатели эффективности когнитивной деятельности:
     * - gravity - гравитационная составляющая
     * - productivity - уровень продуктивности
     * - relaxation - уровень расслабления
     * - concentration - уровень концентрации
     */
    @SerializedName("productivityMetrics")
    val productivityMetrics: List<ProductivityMetricDto>? = emptyList(),

    /**
     * Эмоциональные метрики
     *
     * Показатели эмоционального состояния:
     * - attention - уровень внимания
     * - cognitiveLoad - когнитивная нагрузка
     * - cognitiveControl - когнитивный контроль
     * - selfControl - самоконтроль
     */
    @SerializedName("emotionalMetrics")
    val emotionalMetrics: List<EmotionalMetricDto>? = emptyList(),

    /**
     * Кардио метрики
     *
     * Показатели сердечно-сосудистой системы:
     * - heartRate - частота сердечных сокращений
     * - kaplanIndex - индекс Каплана (вариабельность ритма)
     * - stressIndex - уровень стресса на основе ЧСС
     * - skinContact - качество контакта электродов
     */
    @SerializedName("cardioMetrics")
    val cardioMetrics: List<CardioMetricDto>? = emptyList(),

    // ==================== СЖАТЫЕ МЕТРИКИ (COMPRESSED) ====================

    /**
     * Сжатые NFB метрики
     *
     * Агрегированные данные нейрофидбека для уменьшения объема передаваемых данных.
     */
    @SerializedName("nfbMetricsCompressed")
    val nfbMetricsCompressed: List<NfbMetricCompressedDto>? = emptyList(),

    /**
     * Сжатые физиологические метрики
     */
    @SerializedName("physiologicalMetricsCompressed")
    val physiologicalMetricsCompressed: List<PhysiologicalMetricCompressedDto>? = emptyList(),

    /**
     * Сжатые сырые данные ЭЭГ
     */
    @SerializedName("EEGRawMetricsCompressed")
    val EEGRawMetricsCompressed: List<EEGRawMetricCompressedDto>? = emptyList(),

    /**
     * Сжатые обработанные данные ЭЭГ
     */
    @SerializedName("EEGProceedMetricsCompressed")
    val EEGProceedMetricsCompressed: List<EEGProceedMetricCompressedDto>? = emptyList(),

    /**
     * Сжатые артефакты ЭЭГ
     */
    @SerializedName("EEGArtifactsMetricsCompressed")
    val EEGArtifactsMetricsCompressed: List<EEGArtifactMetricCompressedDto>? = emptyList(),

    /**
     * Сжатые MEMS данные
     */
    @SerializedName("memsMetricsCompressed")
    val memsMetricsCompressed: List<MemsMetricCompressedDto>? = emptyList(),

    /**
     * Сжатые метрики продуктивности
     */
    @SerializedName("productivityMetricsCompressed")
    val productivityMetricsCompressed: List<ProductivityMetricCompressedDto>? = emptyList(),

    /**
     * Сжатые эмоциональные метрики
     */
    @SerializedName("emotionalMetricsCompressed")
    val emotionalMetricsCompressed: List<EmotionalMetricCompressedDto>? = emptyList(),

    /**
     * Сжатые кардио метрики
     */
    @SerializedName("cardioMetricsCompressed")
    val cardioMetricsCompressed: List<CardioMetricCompressedDto>? = emptyList(),

    // ==================== БАЗОВЫЕ ЗНАЧЕНИЯ (BASELINE) ====================

    /**
     * Физиологические базовые значения
     *
     * Индивидуальные нормы пользователя для физиологических показателей,
     * установленные в процессе калибровки.
     */
    @SerializedName("physiologicalBaseline")
    val physiologicalBaseline: List<PhysiologicalBaselineDto>? = emptyList(),

    /**
     * Базовые значения продуктивности
     *
     * Индивидуальные нормы для показателей продуктивности.
     */
    @SerializedName("productivityBaseline")
    val productivityBaseline: List<ProductivityBaselineDto>? = emptyList(),

    /**
     * Индексы продуктивности
     *
     * Нормированные показатели продуктивности с текстовыми рекомендациями.
     */
    @SerializedName("productivityIndex")
    val productivityIndex: List<ProductivityIndexDto>? = emptyList(),

    @SerializedName("sessionResults")
    val sessionResult: List<SessionDto>? = emptyList()
)

// ==================== NFB МЕТРИКИ ====================

/**
 * DTO для NFB метрик (нейрофидбек)
 *
 * Содержит спектральные характеристики ЭЭГ сигнала.
 *
 * @property individualNumber Идентификатор пользователя
 * @property expeditionId Идентификатор экспедиции
 * @property timestamp Временная метка в миллисекундах
 * @property session Номер сессии
 * @property alpha Альфа-ритм (8-13 Гц) - состояние покоя
 * @property beta Бета-ритм (13-30 Гц) - активное мышление
 * @property theta Тета-ритм (4-8 Гц) - дремотное состояние
 * @property delta Дельта-ритм (0.5-4 Гц) - глубокий сон
 * @property smr Сенсомоторный ритм (12-15 Гц)
 */
data class NfbMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("alpha")
    val alpha: Double,
    @SerializedName("beta")
    val beta: Double,
    @SerializedName("theta")
    val theta: Double,
    @SerializedName("delta")
    val delta: Double,
    @SerializedName("smr")
    val smr: Double,
    @Transient
    val rowId: Long = 0L
)

/** Сжатый вариант NFB метрик */
data class NfbMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("alpha")
    val alpha: Double,
    @SerializedName("beta")
    val beta: Double,
    @SerializedName("theta")
    val theta: Double,
    @SerializedName("delta")
    val delta: Double,
    @SerializedName("smr")
    val smr: Double,
    @Transient
    val rowId: Long = 0L
)

// ==================== ФИЗИОЛОГИЧЕСКИЕ МЕТРИКИ ====================

/**
 * DTO для физиологических метрик
 *
 * Комплексные показатели психофизиологического состояния.
 *
 * @property relax Уровень расслабления (0-1)
 * @property fatigue Уровень утомления (0-1)
 * @property none Нейтральное состояние
 * @property concentration Уровень концентрации (0-1)
 * @property involvement Уровень вовлеченности (0-1)
 * @property stress Уровень стресса (0-1)
 * @property nfbArtifacts Наличие артефактов в NFB сигнале (0/1)
 * @property cardioArtifacts Наличие артефактов в кардиосигнале (0/1)
 */
data class PhysiologicalMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("relax")
    val relax: Double,
    @SerializedName("fatigue")
    val fatigue: Double,
    @SerializedName("none")
    val none: Double,
    @SerializedName("concentration")
    val concentration: Double,
    @SerializedName("involvement")
    val involvement: Double,
    @SerializedName("stress")
    val stress: Double,
    @SerializedName("nfbArtifacts")
    val nfbArtifacts: Int,
    @SerializedName("cardioArtifacts")
    val cardioArtifacts: Int,
    @Transient
    val rowId: Long = 0L
)

/** Сжатый вариант физиологических метрик */
data class PhysiologicalMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("relax")
    val relax: Double,
    @SerializedName("fatigue")
    val fatigue: Double,
    @SerializedName("none")
    val none: Double,
    @SerializedName("concentration")
    val concentration: Double,
    @SerializedName("involvement")
    val involvement: Double,
    @SerializedName("stress")
    val stress: Double,
    @SerializedName("nfbArtifacts")
    val nfbArtifacts: Int,
    @SerializedName("cardioArtifacts")
    val cardioArtifacts: Int,
    @Transient
    val rowId: Long = 0L
)

// ==================== ЭЭГ МЕТРИКИ ====================

/**
 * DTO для сырых данных ЭЭГ
 *
 * @property channel1 Значение с первого канала
 * @property channel2 Значение со второго канала
 */
data class EEGRawMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("channel1")
    val channel1: Float,
    @SerializedName("channel2")
    val channel2: Float,
    @Transient
    val rowId: Long = 0L
)

/** Сжатый вариант сырых данных ЭЭГ */
data class EEGRawMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("channel1")
    val channel1: Float,
    @SerializedName("channel2")
    val channel2: Float,
    @Transient
    val rowId: Long = 0L
)

/**
 * DTO для обработанных данных ЭЭГ
 *
 * Сигналы после фильтрации и удаления артефактов.
 */
data class EEGProceedMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("channel1")
    val channel1: Float,
    @SerializedName("channel2")
    val channel2: Float,
    @Transient
    val rowId: Long = 0L
)

/** Сжатый вариант обработанных данных ЭЭГ */
data class EEGProceedMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("channel1")
    val channel1: Float,
    @SerializedName("channel2")
    val channel2: Float,
    @Transient
    val rowId: Long = 0L
)

/**
 * DTO для артефактов ЭЭГ
 *
 * Информация о качестве сигнала.
 *
 * @property artifactsChannel1 Наличие артефактов на первом канале
 * @property artifactsChannel2 Наличие артефактов на втором канале
 * @property qualityChannel1 Качество сигнала первого канала (0-1)
 * @property qualityChannel2 Качество сигнала второго канала (0-1)
 */
data class EEGArtifactMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("artifactsChannel1")
    val artifactsChannel1: Boolean,
    @SerializedName("artifactsChannel2")
    val artifactsChannel2: Boolean,
    @SerializedName("qualityChannel1")
    val qualityChannel1: Float,
    @SerializedName("qualityChannel2")
    val qualityChannel2: Float,
    @Transient
    val rowId: Long = 0L
)

/** Сжатый вариант артефактов ЭЭГ */
data class EEGArtifactMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("artifactsChannel1")
    val artifactsChannel1: Boolean,
    @SerializedName("artifactsChannel2")
    val artifactsChannel2: Boolean,
    @SerializedName("qualityChannel1")
    val qualityChannel1: Float,
    @SerializedName("qualityChannel2")
    val qualityChannel2: Float,
    @Transient
    val rowId: Long = 0L
)

// ==================== MEMS МЕТРИКИ ====================

/**
 * DTO для MEMS-датчиков
 *
 * Данные акселерометра и гироскопа.
 *
 * @property accelerometerX Ускорение по оси X (м/с²)
 * @property accelerometerY Ускорение по оси Y (м/с²)
 * @property accelerometerZ Ускорение по оси Z (м/с²)
 * @property gyroscopeX Угловая скорость по оси X (рад/с)
 * @property gyroscopeY Угловая скорость по оси Y (рад/с)
 * @property gyroscopeZ Угловая скорость по оси Z (рад/с)
 */
data class MemsMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("accelerometerX")
    val accelerometerX: Double,
    @SerializedName("accelerometerY")
    val accelerometerY: Double,
    @SerializedName("accelerometerZ")
    val accelerometerZ: Double,
    @SerializedName("gyroscopeX")
    val gyroscopeX: Double,
    @SerializedName("gyroscopeY")
    val gyroscopeY: Double,
    @SerializedName("gyroscopeZ")
    val gyroscopeZ: Double,
    @Transient
    val rowId: Long = 0L
)

/** Сжатый вариант MEMS данных */
data class MemsMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("accelerometerX")
    val accelerometerX: Double,
    @SerializedName("accelerometerY")
    val accelerometerY: Double,
    @SerializedName("accelerometerZ")
    val accelerometerZ: Double,
    @SerializedName("gyroscopeX")
    val gyroscopeX: Double,
    @SerializedName("gyroscopeY")
    val gyroscopeY: Double,
    @SerializedName("gyroscopeZ")
    val gyroscopeZ: Double,
    @Transient
    val rowId: Long = 0L
)

// ==================== ПРОДУКТИВНОСТЬ ====================

/**
 * DTO для метрик продуктивности
 *
 * Показатели эффективности когнитивной деятельности.
 *
 * @property gravity Гравитационная составляющая
 * @property productivity Уровень продуктивности
 * @property fatigue Уровень утомления
 * @property reverseFatigue Обратный уровень утомления
 * @property relaxation Уровень расслабления
 * @property concentration Уровень концентрации
 */
data class ProductivityMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("gravity")
    val gravity: Double,
    @SerializedName("productivity")
    val productivity: Double,
    @SerializedName("fatigue")
    val fatigue: Double,
    @SerializedName("reverseFatigue")
    val reverseFatigue: Double,
    @SerializedName("relaxation")
    val relaxation: Double,
    @SerializedName("concentration")
    val concentration: Double,
    @Transient
    val rowId: Long = 0L
)

/** Сжатый вариант метрик продуктивности */
data class ProductivityMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("gravity")
    val gravity: Double,
    @SerializedName("productivity")
    val productivity: Double,
    @SerializedName("fatigue")
    val fatigue: Double,
    @SerializedName("reverseFatigue")
    val reverseFatigue: Double,
    @SerializedName("relaxation")
    val relaxation: Double,
    @SerializedName("concentration")
    val concentration: Double,
    @Transient
    val rowId: Long = 0L
)

// ==================== ЭМОЦИОНАЛЬНЫЕ МЕТРИКИ ====================

/**
 * DTO для эмоциональных метрик
 *
 * Показатели эмоционального и когнитивного состояния.
 *
 * @property attention Уровень внимания
 * @property relaxation Уровень расслабления
 * @property cognitiveLoad Когнитивная нагрузка
 * @property cognitiveControl Когнитивный контроль
 * @property selfControl Самоконтроль
 */
data class EmotionalMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("attention")
    val attention: Double,
    @SerializedName("relaxation")
    val relaxation: Double,
    @SerializedName("cognitiveLoad")
    val cognitiveLoad: Double,
    @SerializedName("cognitiveControl")
    val cognitiveControl: Double,
    @SerializedName("selfControl")
    val selfControl: Double,
    @Transient
    val rowId: Long = 0L
)

/** Сжатый вариант эмоциональных метрик */
data class EmotionalMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("attention")
    val attention: Double,
    @SerializedName("relaxation")
    val relaxation: Double,
    @SerializedName("cognitiveLoad")
    val cognitiveLoad: Double,
    @SerializedName("cognitiveControl")
    val cognitiveControl: Double,
    @SerializedName("selfControl")
    val selfControl: Double,
    @Transient
    val rowId: Long = 0L
)

// ==================== КАРДИО МЕТРИКИ ====================

/**
 * DTO для кардио метрик
 *
 * Показатели сердечно-сосудистой системы.
 *
 * @property heartRate Частота сердечных сокращений (уд/мин)
 * @property hasArtifacts Наличие артефактов (0/1)
 * @property kaplanIndex Индекс Каплана (вариабельность сердечного ритма)
 * @property metricsAvailable Доступность метрик (0/1)
 * @property motionArtifacts Артефакты движения (0/1)
 * @property skinContact Качество контакта с кожей (0/1)
 * @property stressIndex Уровень стресса на основе кардиоданных
 */
data class CardioMetricDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("heartRate")
    val heartRate: Double,
    @SerializedName("hasArtifacts")
    val hasArtifacts: Int,
    @SerializedName("kaplanIndex")
    val kaplanIndex: Double,
    @SerializedName("metricsAvailable")
    val metricsAvailable: Int,
    @SerializedName("motionArtifacts")
    val motionArtifacts: Int,
    @SerializedName("skinContact")
    val skinContact: Int,
    @SerializedName("stressIndex")
    val stressIndex: Double,
    @Transient
    val rowId: Long = 0L
)

/** Сжатый вариант кардио метрик */
data class CardioMetricCompressedDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("heartRate")
    val heartRate: Double,
    @SerializedName("hasArtifacts")
    val hasArtifacts: Int,
    @SerializedName("kaplanIndex")
    val kaplanIndex: Double,
    @SerializedName("metricsAvailable")
    val metricsAvailable: Int,
    @SerializedName("motionArtifacts")
    val motionArtifacts: Int,
    @SerializedName("skinContact")
    val skinContact: Int,
    @SerializedName("stressIndex")
    val stressIndex: Double,
    @Transient
    val rowId: Long = 0L
)

// ==================== БАЗОВЫЕ ЗНАЧЕНИЯ ====================

/**
 * DTO для физиологических базовых значений
 *
 * Индивидуальные нормы, установленные в процессе калибровки.
 *
 * @property alpha Альфа-активность
 * @property beta Бета-активность
 * @property alphaGravity Гравитационная составляющая альфа-ритма
 * @property betaGravity Гравитационная составляющая бета-ритма
 * @property concentration Уровень концентрации
 */
data class PhysiologicalBaselineDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("alpha")
    val alpha: Double,
    @SerializedName("beta")
    val beta: Double,
    @SerializedName("alphaGravity")
    val alphaGravity: Double,
    @SerializedName("betaGravity")
    val betaGravity: Double,
    @SerializedName("concentration")
    val concentration: Double,
    @Transient
    val rowId: Long = 0L
)

/**
 * DTO для базовых значений продуктивности
 */
data class ProductivityBaselineDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("gravity")
    val gravity: Double,
    @SerializedName("productivity")
    val productivity: Double,
    @SerializedName("fatigue")
    val fatigue: Double,
    @SerializedName("reverseFatigue")
    val reverseFatigue: Double,
    @SerializedName("relaxation")
    val relaxation: Double,
    @SerializedName("concentration")
    val concentration: Double,
    @Transient
    val rowId: Long = 0L
)

/**
 * DTO для индексов продуктивности
 *
 * Нормированные показатели с текстовыми рекомендациями.
 *
 * @property relaxation Текстовая рекомендация по расслаблению
 * @property stress Текстовый уровень стресса
 * @property gravityBaseline Нормированный базовый уровень гравитации
 * @property productivityBaseline Нормированный базовый уровень продуктивности
 * @property fatigueBaseline Нормированный базовый уровень утомления
 * @property reverseFatigueBaseline Нормированный обратный уровень утомления
 * @property relaxationBaseline Нормированный базовый уровень расслабления
 * @property concentrationBaseline Нормированный базовый уровень концентрации
 * @property hasArtifacts Наличие артефактов
 */
data class ProductivityIndexDto(
    @SerializedName("individualNumber")
    val individualNumber: String,
    @SerializedName("expeditionId")
    val expeditionId: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session")
    val session: Int,
    @SerializedName("relaxation")
    val relaxation: String,
    @SerializedName("stress")
    val stress: String,
    @SerializedName("gravityBaseline")
    val gravityBaseline: Double,
    @SerializedName("productivityBaseline")
    val productivityBaseline: Double,
    @SerializedName("fatigueBaseline")
    val fatigueBaseline: Double,
    @SerializedName("reverseFatigueBaseline")
    val reverseFatigueBaseline: Double,
    @SerializedName("relaxationBaseline")
    val relaxationBaseline: Double,
    @SerializedName("concentrationBaseline")
    val concentrationBaseline: Double,
    @SerializedName("hasArtifacts")
    val hasArtifacts: Boolean,
    @Transient
    val rowId: Long = 0L
)

data class SessionDto(
    @SerializedName("session")
    val session : Int,
    @SerializedName("individualNumber")
    val individualNumber: String?,
    @SerializedName("expeditionId")
    val expeditionId: String?,
    // Объективные оценки
    @SerializedName("objectiveCognitive")
    val objectiveCognitive: Int?,
    @SerializedName("objectivePsychological")
    val objectivePsychological: Int?,
    @SerializedName("objectivePhysiological")
    val objectivePhysiological: Int?,

    // Субъективные оценки
    @SerializedName("subjectiveCognitive")
    val subjectiveCognitive: Int?,
    @SerializedName("subjectivePsychological")
    val subjectivePsychological: Int?,
    @SerializedName("subjectivePhysiological")
    val subjectivePhysiological: Int?,

    // Итоговые и средние
    @SerializedName("totalIndex")
    val totalIndex: Int?,
    @SerializedName("averageObjective")
    val averageObjective: Int?,
    @SerializedName("averageSubjective")
    val averageSubjective: Int?,
    @SerializedName("totalCognitive")
    val totalCognitive: Int?,
    @SerializedName("totalPhysiological")
    val totalPhysiological: Int?,
    @SerializedName("totalPsychological")
    val totalPsychological: Int?,

    @SerializedName("durationMinutes")
    val durationMinutes: Int?,
    @SerializedName("endTime")
    val endTime: Int?,
    @SerializedName("sessionCategory")
    val sessionCategory: SessionCategory?,
    @SerializedName("comment")
    val comment: String?,


    @SerializedName("objectiveFatigue")
    val objectiveFatigue: String?,
    @SerializedName("objectiveStress")
    val objectiveStress: String?,
    val passingPrematurely: Boolean?,
    @Transient
    val localSessionId: Long? = null
)