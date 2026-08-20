package com.neuroproject.neuro.data

import android.util.Log
import com.neuroproject.neuro.ApplicationScope
import com.neuroproject.neuro.IoDispatcher
import com.neuroproject.neuro.data.entity.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Singleton

/**
 * Репозиторий для сохранения всех типов метрик в Room Database.
 *
 * Предоставляет методы для сохранения данных от нейро-гарнитуры.
 * Автоматически создаёт сжатые версии данных каждую минуту.
 *
 * Основные возможности:
 * - Сохранение всех типов метрик (NFB, ЭЭГ, MEMS, кардио и др.)
 * - Создание сжатых метрик (медиана за минуту)
 * - Очистка данных по сессиям
 * - Фильтрация некорректных данных (NaN, Infinity)
 *
 * Архитектура буферизации:
 * - Каждый тип метрик имеет свой буфер
 * - При накоплении минуты данных буфер сбрасывается в compressed таблицу
 * - Для булевых полей используется функция majority()
 *
 * @see MetricsDao
 * @see SensorArtifactProcessor
 */
@Singleton
class MetricsRepository(
    private val metricsDao: MetricsDao,
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val COMPRESSED_TIME = 60000L // 1 минута в миллисекундах

    private fun normalizeTimestamp(timestamp: Long): Long =
        if (timestamp > 0L) timestamp else System.currentTimeMillis()

    /**
     * Буфер для NFB метрик с указанием времени первого элемента.
     */
    private data class NfbBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<NFBMetricEntity> = mutableListOf()
    )

    /**
     * Буфер для сырых данных ЭЭГ.
     */
    private data class EEGRAWBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<EEGRawMetricEntity> = mutableListOf()
    )

    /**
     * Буфер для обработанных данных ЭЭГ.
     */
    private data class EEGPROCEEDBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<EEGProceedMetricEntity> = mutableListOf()
    )

    /**
     * Буфер для данных артефактов ЭЭГ.
     */
    private data class EEGArtifactBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<EEGArtifactsMetricEntity> = mutableListOf()
    )

    /**
     * Буфер для физиологических метрик.
     */
    private data class PhysiologicalBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<PhysiologicalMetricEntity> = mutableListOf()
    )

    /**
     * Буфер для эмоциональных метрик.
     */
    private data class EmotionalBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<EmotionalMetricEntity> = mutableListOf()
    )

    /**
     * Буфер для метрик продуктивности.
     */
    private data class ProductivityBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<ProductivityMetricEntity> = mutableListOf()
    )

    /**
     * Буфер для кардио метрик.
     */
    private data class CardioBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<CardioMetricEntity> = mutableListOf()
    )

    /**
     * Буфер для MEMS данных.
     */
    private data class MEMSBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<MEMSMetricEntity> = mutableListOf()
    )

    private val nfbBuffer = NfbBuffer()
    private val eegRawBuffer = EEGRAWBuffer()
    private val eegProceedBuffer = EEGPROCEEDBuffer()
    private val eegArtifactBuffer = EEGArtifactBuffer()
    private val physiologicalBuffer = PhysiologicalBuffer()
    private val productivityBuffer = ProductivityBuffer()
    private val emotionalBuffer = EmotionalBuffer()
    private val cardioBuffer = CardioBuffer()
    private val memsBuffer = MEMSBuffer()

    /** Мьютекс для синхронизации доступа к буферам */
    private val mutex = Mutex()

    /** Процессор артефактов для фильтрации некорректных данных */
    private val artifactProcessor = SensorArtifactProcessor()


    /**
     * Сохранить метрику нейрофидбека (NFB).
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param exp_id ID экспедиции
     * @param sessionId ID сессии
     * @param alpha Уровень альфа-ритма
     * @param beta Уровень бета-ритма
     * @param theta Уровень тета-ритма
     * @param delta Уровень дельта-ритма
     * @param smr Уровень SMR
     */
    fun saveNFBMetric(
        time: Long,
        id: String,
        exp_id: String,
        sessionId: Long,
        alpha: Float,
        beta: Float,
        theta: Float,
        delta: Float,
        smr: Float
    ) {
        if (!artifactProcessor.shouldStoreNfb(alpha, beta, theta, delta, smr)) {
            Log.w("MetricsRepository", "NFB sample skipped: non-finite value")
            return
        }
        appScope.launch(ioDispatcher) {
            try {
                val metric = NFBMetricEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = exp_id,
                    sessionId = sessionId,
                    alpha = alpha,
                    beta = beta,
                    theta = theta,
                    delta = delta,
                    smr = smr,
                    isMarked = false
                )
                metricsDao.insertNFBMetric(metric)
                mutex.withLock {
                    if (nfbBuffer.firstTimestamp == null) {
                        nfbBuffer.firstTimestamp = metric.timestamp
                    }
                    nfbBuffer.values.add(metric)

                    if (metric.timestamp - nfbBuffer.firstTimestamp!! >= COMPRESSED_TIME) {
                        flushNfbBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving NFB metric", e)
            }
        }
    }

    /**
     * Сохранить сырые данные ЭЭГ.
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param exp_id ID экспедиции
     * @param sessionId ID сессии
     * @param channel1 Значение первого канала
     * @param channel2 Значение второго канала
     */
    fun saveEEGRAWMetric(
        time: Long,
        id: String,
        exp_id: String,
        sessionId: Long,
        channel1: Float,
        channel2: Float
    ) {
        if (!artifactProcessor.shouldStoreEeg(channel1, channel2)) {
            Log.w("MetricsRepository", "EEG RAW sample skipped: non-finite value")
            return
        }
        appScope.launch(ioDispatcher) {
            try {
                val metric = EEGRawMetricEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = exp_id,
                    sessionId = sessionId,
                    channel1 = channel1,
                    channel2 = channel2,
                    isMarked = false
                )
                metricsDao.insertEEGRAWMetric(metric)
                mutex.withLock {
                    if (eegRawBuffer.firstTimestamp == null) {
                        eegRawBuffer.firstTimestamp = metric.timestamp
                    }
                    eegRawBuffer.values.add(metric)
                    if (metric.timestamp - eegRawBuffer.firstTimestamp!! >= COMPRESSED_TIME) {
                        flushEEGRAWBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving EEG RAW metric", e)
            }
        }
    }

    /**
     * Сохранить обработанные данные ЭЭГ.
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param exp_id ID экспедиции
     * @param sessionId ID сессии
     * @param channel1 Значение первого канала
     * @param channel2 Значение второго канала
     */
    fun saveEEGPROCEEDMetric(
        time: Long,
        id: String,
        exp_id: String,
        sessionId: Long,
        channel1: Float,
        channel2: Float
    ) {
        if (!artifactProcessor.shouldStoreEeg(channel1, channel2)) {
            Log.w("MetricsRepository", "EEG processed sample skipped: non-finite value")
            return
        }
        appScope.launch(ioDispatcher) {
            try {
                val metric = EEGProceedMetricEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = exp_id,
                    sessionId = sessionId,
                    channel1 = channel1,
                    channel2 = channel2,
                    isMarked = false
                )
                metricsDao.insertEEGPROCEEDMetric(metric)
                mutex.withLock {
                    if (eegProceedBuffer.firstTimestamp == null) {
                        eegProceedBuffer.firstTimestamp = metric.timestamp
                    }
                    eegProceedBuffer.values.add(metric)

                    if (metric.timestamp - eegProceedBuffer.firstTimestamp!! >= COMPRESSED_TIME) {
                        flushEEGPROCEEDBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving EEG PROCEED metric", e)
            }
        }
    }

    /**
     * Сохранить данные об артефактах ЭЭГ.
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param exp_id ID экспедиции
     * @param sessionId ID сессии
     * @param artifactsChannel1 Наличие артефактов на первом канале
     * @param artifactsChannel2 Наличие артефактов на втором канале
     * @param qualityChannel1 Качество сигнала первого канала
     * @param qualityChannel2 Качество сигнала второго канала
     */
    fun saveEEGArtifactMetric(
        time: Long,
        id: String,
        exp_id: String,
        sessionId: Long,
        artifactsChannel1: Boolean,
        artifactsChannel2: Boolean,
        qualityChannel1: Float,
        qualityChannel2: Float,
    ) {
        if (!artifactProcessor.shouldStoreArtifacts(artifactsChannel1, artifactsChannel2, qualityChannel1, qualityChannel2)) {
            Log.w("MetricsRepository", "EEG artifact sample skipped: non-finite quality")
            return
        }
        appScope.launch(ioDispatcher) {
            try {
                val metric = EEGArtifactsMetricEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = exp_id,
                    sessionId = sessionId,
                    artifactsChannel1 = artifactsChannel1,
                    artifactsChannel2 = artifactsChannel2,
                    qualityChannel1 = qualityChannel1,
                    qualityChannel2 = qualityChannel2,
                    isMarked = false
                )
                metricsDao.insertEEGArtifactsMetric(metric)
                mutex.withLock {
                    if (eegArtifactBuffer.firstTimestamp == null) {
                        eegArtifactBuffer.firstTimestamp = metric.timestamp
                    }
                    eegArtifactBuffer.values.add(metric)
                    if (metric.timestamp - eegArtifactBuffer.firstTimestamp!! >= COMPRESSED_TIME) {
                        flushEEGArtifactBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving EEG Artifact metric", e)
            }
        }
    }

    /**
     * Сохранить физиологические метрики.
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param exp_id ID экспедиции
     * @param sessionId ID сессии
     * @param relax Уровень расслабления
     * @param fatigue Уровень утомления
     * @param none Нейтральное состояние
     * @param concentration Уровень концентрации
     * @param involvement Уровень вовлечённости
     * @param stress Уровень стресса
     * @param nfbArtifacts Наличие артефактов NFB
     * @param cardioArtifacts Наличие кардио артефактов
     */
    fun savePhysiologicalMetric(
        time: Long,
        id: String,
        exp_id: String,
        sessionId: Long,
        relax: Float,
        fatigue: Float,
        none: Float,
        concentration: Float,
        involvement: Float,
        stress: Float,
        nfbArtifacts: Boolean,
        cardioArtifacts: Boolean
    ) {
        if (!artifactProcessor.shouldStorePhysiological(listOf(relax, fatigue, none, concentration, involvement, stress))) {
            Log.w("MetricsRepository", "Physiological sample skipped: non-finite value")
            return
        }
        appScope.launch(ioDispatcher) {
            try {
                val metric = PhysiologicalMetricEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = exp_id,
                    sessionId = sessionId,
                    relax = relax,
                    fatigue = fatigue,
                    none = none,
                    concentration = concentration,
                    involvement = involvement,
                    stress = stress,
                    nfbArtifacts = nfbArtifacts,
                    cardioArtifacts = cardioArtifacts,
                    isMarked = false
                )
                metricsDao.insertPhysiologicalMetric(metric)
                mutex.withLock {
                    if (physiologicalBuffer.firstTimestamp == null) {
                        physiologicalBuffer.firstTimestamp = metric.timestamp
                    }
                    physiologicalBuffer.values.add(metric)
                    flushPhysiologicalBuffer()
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving physiological metric", e)
            }
        }
    }

    /**
     * Сохранить MEMS данные (акселерометр и гироскоп).
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param exp_id ID экспедиции
     * @param sessionId ID сессии
     * @param accX Ускорение по оси X
     * @param accY Ускорение по оси Y
     * @param accZ Ускорение по оси Z
     * @param gyroX Угловая скорость по оси X
     * @param gyroY Угловая скорость по оси Y
     * @param gyroZ Угловая скорость по оси Z
     */
    fun saveMEMSMetric(
        time: Long,
        id: String,
        exp_id: String,
        sessionId: Long,
        accX: Float,
        accY: Float,
        accZ: Float,
        gyroX: Float,
        gyroY: Float,
        gyroZ: Float
    ) {
        if (!artifactProcessor.shouldStoreMems(listOf(accX, accY, accZ, gyroX, gyroY, gyroZ))) {
            Log.w("MetricsRepository", "MEMS sample skipped: non-finite value")
            return
        }
        appScope.launch(ioDispatcher) {
            try {
                val metric = MEMSMetricEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = exp_id,
                    sessionId = sessionId,
                    accelerometerX = accX,
                    accelerometerY = accY,
                    accelerometerZ = accZ,
                    gyroscopeX = gyroX,
                    gyroscopeY = gyroY,
                    gyroscopeZ = gyroZ,
                    isMarked = false
                )
                metricsDao.insertMEMSMetric(metric)
                mutex.withLock {
                    if (memsBuffer.firstTimestamp == null) {
                        memsBuffer.firstTimestamp = metric.timestamp
                    }
                    memsBuffer.values.add(metric)
                    if (metric.timestamp - memsBuffer.firstTimestamp!! >= COMPRESSED_TIME) {
                        flushMEMSBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving MEMS metric", e)
            }
        }
    }

    /**
     * Сохранить метрики продуктивности.
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param exp_id ID экспедиции
     * @param sessionId ID сессии
     * @param gravity Гравитационная составляющая
     * @param productivity Уровень продуктивности
     * @param fatigue Уровень утомления
     * @param reverseFatigue Обратный уровень утомления
     * @param relaxation Уровень расслабления
     * @param concentration Уровень концентрации
     */
    fun saveProductivityMetric(
        time: Long,
        id: String,
        exp_id: String,
        sessionId: Long,
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    ) {
        if (!artifactProcessor.shouldStoreProductivity(listOf(gravity, productivity, fatigue, reverseFatigue, relaxation, concentration))) {
            Log.w("MetricsRepository", "Productivity sample skipped: non-finite value")
            return
        }
        appScope.launch(ioDispatcher) {
            try {
                val metric = ProductivityMetricEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = exp_id,
                    sessionId = sessionId,
                    gravity = gravity,
                    productivity = productivity,
                    fatigue = fatigue,
                    reverseFatigue = reverseFatigue,
                    relaxation = relaxation,
                    concentration = concentration,
                    isMarked = false
                )
                metricsDao.insertProductivityMetric(metric)
                Log.d("MetricsRepository", "Productivity metric saved: time=$time, productivity=$productivity")
                mutex.withLock {
                    if (productivityBuffer.firstTimestamp == null) {
                        productivityBuffer.firstTimestamp = metric.timestamp
                    }

                    productivityBuffer.values.add(metric)

                    if (metric.timestamp - productivityBuffer.firstTimestamp!! >= COMPRESSED_TIME) {
                        flushProductivityBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving productivity metric", e)
            }
        }
    }

    /**
     * Сохранить индексы продуктивности.
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param expedition_id ID экспедиции
     * @param sessionId ID сессии
     * @param relaxation Текстовая рекомендация по расслаблению
     * @param stress Текстовый уровень стресса
     * @param gravityBaseline Базовый уровень гравитации
     * @param productivityBaseline Базовый уровень продуктивности
     * @param fatiqueBaseline Базовый уровень утомления
     * @param reverseFatiqueBaseline Обратный базовый уровень утомления
     * @param relaxationBaselines Базовый уровень расслабления
     * @param concentrationBaselines Базовый уровень концентрации
     * @param hasArtifacts Наличие артефактов
     */
    fun saveProductivityIndexes(
        time: Long,
        id: String,
        expedition_id: String,
        sessionId: Long,
        relaxation: String,
        stress: String,
        gravityBaseline: Float,
        productivityBaseline: Float,
        fatiqueBaseline: Float,
        reverseFatiqueBaseline: Float,
        relaxationBaselines: Float,
        concentrationBaselines: Float,
        hasArtifacts: Boolean
    ) {
        appScope.launch(ioDispatcher) {
            try {
                val index = ProductivityIndexesEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = expedition_id,
                    sessionId = sessionId,
                    relaxation = relaxation,
                    stress = stress,
                    gravityBaseline = gravityBaseline,
                    productivityBaseline = productivityBaseline,
                    fatigueBaseline = fatiqueBaseline,
                    reverseFatigueBaseline = reverseFatiqueBaseline,
                    relaxationBaseline = relaxationBaselines,
                    concentrationBaseline = concentrationBaselines,
                    hasArtifacts = hasArtifacts,
                    isMarked = false
                )
                metricsDao.insertProductivityIndex(index);
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving productivity index", e)
            }
        }


    }

    /**
     * Сохранить калибровочные данные продуктивности.
     *
     * @param userId ID пользователя
     * @param gravityBaseline Базовый уровень гравитации
     * @param productivityBaseline Базовый уровень продуктивности
     * @param fatiqueBaseline Базовый уровень утомления
     * @param reverseFatiqueBaseline Обратный базовый уровень утомления
     * @param relaxationBaselines Базовый уровень расслабления
     * @param concentrationBaselines Базовый уровень концентрации
     */
    fun saveProductivityCalibration(
        userId: String,
        gravityBaseline: Float,
        productivityBaseline: Float,
        fatiqueBaseline: Float,
        reverseFatiqueBaseline: Float,
        relaxationBaselines: Float,
        concentrationBaselines: Float,
    ){
        appScope.launch{
            try {
                metricsDao.insertProductivityCalibration(userId, gravityBaseline, productivityBaseline, fatiqueBaseline, reverseFatiqueBaseline, relaxationBaselines, concentrationBaselines)
            }catch (e: Exception){
                Log.e("MetricsRepository", "Error saving productivity calibration")
            }
        }

    }

    /**
     * Сохранить базовые значения продуктивности (калибровка).
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param expedition_id ID экспедиции
     * @param sessionId ID сессии
     * @param gravity Гравитационная составляющая
     * @param productivity Уровень продуктивности
     * @param fatigue Уровень утомления
     * @param reverseFatigue Обратный уровень утомления
     * @param relaxation Уровень расслабления
     * @param concentration Уровень концентрации
     */
    fun saveProductivityBaselines(
        time: Long,
        id: String,
        expedition_id: String,
        sessionId: Long,
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    ) {
        appScope.launch(ioDispatcher) {
            try {
                val index = ProductivityBaselinesEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = expedition_id,
                    sessionId = sessionId,
                    gravity = gravity,
                    productivity = productivity,
                    fatigue = fatigue,
                    reverseFatigue = reverseFatigue,
                    relaxation = relaxation,
                    concentration = concentration,
                    isMarked = false
                )
                metricsDao.insertProductivityBaselines(index);
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving productivity baseline", e)
            }
        }


    }

    /**
     * Сохранить физиологические базовые значения (калибровка).
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param expedition_id ID экспедиции
     * @param sessionId ID сессии
     * @param alpha Уровень альфа-ритма
     * @param beta Уровень бета-ритма
     * @param alphaGravity Гравитационная составляющая альфа-ритма
     * @param betaGravity Гравитационная составляющая бета-ритма
     * @param concentration Уровень концентрации
     */
    fun savePhysiologicalBaselines(
        time: Long,
        id: String,
        expedition_id: String,
        sessionId: Long,
        alpha: Float,
        beta: Float,
        alphaGravity: Float,
        betaGravity: Float,
        concentration: Float
    ) {
        appScope.launch(ioDispatcher) {
            try {
                val index = PhysiologicalBaselinesEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = expedition_id,
                    sessionId = sessionId,
                    alpha = alpha,
                    beta = beta,
                    alphaGravity = alphaGravity,
                    betaGravity = betaGravity,
                    concentration = concentration,
                    isMarked = false
                )
                metricsDao.insertPhysiologicalBaselines(index);
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving physiological baseline", e)
            }
        }


    }
    /**
     * Сохранить физиологические калибровочные данные.
     *
     * @param userId ID пользователя
     * @param alpha Уровень альфа-ритма
     * @param beta Уровень бета-ритма
     * @param alphaGravity Гравитационная составляющая альфа-ритма
     * @param betaGravity Гравитационная составляющая бета-ритма
     * @param concentration Уровень концентрации
     */
    fun savePhysiologicalCalibration(
        userId: String,
        alpha: Float,
        beta: Float,
        alphaGravity: Float,
        betaGravity: Float,
        concentration: Float
    ){
        appScope.launch{
            try {
                metricsDao.insertPhysiologicalCalibration(userId, alpha, beta, alphaGravity, betaGravity, concentration)
            }catch (e: Exception){
                Log.e("MetricsRepository", "Error saving physiological calibration")
            }
        }

    }

    /**
     * Сохранить эмоциональные метрики.
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param exp_id ID экспедиции
     * @param sessionId ID сессии
     * @param attention Уровень внимания
     * @param relaxation Уровень расслабления
     * @param cognitiveLoad Когнитивная нагрузка
     * @param cognitiveControl Когнитивный контроль
     * @param selfControl Самоконтроль
     */
    fun saveEmotionalMetric(
        time: Long,
        id: String,
        exp_id: String,
        sessionId: Long,
        attention: Float,
        relaxation: Float,
        cognitiveLoad: Float,
        cognitiveControl: Float,
        selfControl: Float
    ) {
        if (!artifactProcessor.shouldStoreEmotional(listOf(attention, relaxation, cognitiveLoad, cognitiveControl, selfControl))) {
            Log.w("MetricsRepository", "Emotional sample skipped: non-finite value")
            return
        }
        appScope.launch(ioDispatcher) {
            try {
                val metric = EmotionalMetricEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = exp_id,
                    sessionId = sessionId,
                    attention = attention,
                    relaxation = relaxation,
                    cognitiveLoad = cognitiveLoad,
                    cognitiveControl = cognitiveControl,
                    selfControl = selfControl,
                    isMarked = false
                )
                metricsDao.insertEmotionalMetric(metric)
                mutex.withLock {
                    if (emotionalBuffer.firstTimestamp == null) {
                        emotionalBuffer.firstTimestamp = metric.timestamp
                    }

                    emotionalBuffer.values.add(metric)

                    if (metric.timestamp - emotionalBuffer.firstTimestamp!! >= COMPRESSED_TIME) {
                        flushEmotionalBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving emotional metric", e)
            }
        }
    }

    /**
     * Сохранить кардио метрики.
     *
     * @param time Временная метка (мс)
     * @param id ID пользователя
     * @param exp_id ID экспедиции
     * @param sessionId ID сессии
     * @param heartRate Частота сердечных сокращений
     * @param hasArtifacts Наличие артефактов
     * @param kaplanIndex Индекс Каплана
     * @param metricsAvailable Доступность метрик
     * @param motionAtrifacts Артефакты движения
     * @param skinContact Качество контакта с кожей
     * @param stressIndex Уровень стресса
     */
    fun saveCardioMetric(
        time: Long,
        id: String,
        exp_id: String,
        sessionId: Long,
        heartRate: Float,
        hasArtifacts: Boolean,
        kaplanIndex: Float,
        metricsAvailable: Boolean,
        motionAtrifacts: Boolean,
        skinContact: Boolean,
        stressIndex: Float
    ) {
        if (!artifactProcessor.shouldStoreCardio(listOf(heartRate, kaplanIndex, stressIndex))) {
            Log.w("MetricsRepository", "Cardio sample skipped: non-finite value")
            return
        }
        appScope.launch(ioDispatcher) {
            try {
                val metric = CardioMetricEntity(
                    timestamp = normalizeTimestamp(time),
                    id = id,
                    expedition_id = exp_id,
                    sessionId = sessionId,
                    heartRate = heartRate,
                    hasArtifacts = hasArtifacts,
                    kaplanIndex = kaplanIndex,
                    metricsAvailable = metricsAvailable,
                    motionArtifacts = motionAtrifacts,
                    skinContact = skinContact,
                    stressIndex = stressIndex,
                    isMarked = false
                )
                metricsDao.insertCardioMetric(metric)
                mutex.withLock {
                    if (cardioBuffer.firstTimestamp == null) {
                        cardioBuffer.firstTimestamp = metric.timestamp
                    }

                    cardioBuffer.values.add(metric)

                    if (metric.timestamp - cardioBuffer.firstTimestamp!! >= COMPRESSED_TIME) {
                        flushCardioBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving cardio metric", e)
            }
        }
    }

    /**
     * Удалить все метрики для указанной сессии.
     *
     * @param sessionId ID сессии
     */
    fun clearAllMetricsBySessionId(sessionId: Long){
        appScope.launch(ioDispatcher) {
            try{
                metricsDao.clearNFBMetricsBySessionId(sessionId)
                metricsDao.clearNFBMetricsCompressedBySessionId(sessionId)
                metricsDao.clearCardioMetricsBySessionId(sessionId)
                metricsDao.clearCardioMetricsCompressedBySessionId(sessionId)
                metricsDao.clearMEMSMetricsBySessionId(sessionId)
                metricsDao.clearMEMSMetricsCompressedBySessionId(sessionId)
                metricsDao.clearProductivityMetricsBySessionId(sessionId)
                metricsDao.clearProductivityMetricsCompressedBySessionId(sessionId)
                metricsDao.clearProductivityIndexesBySessionId(sessionId)
                metricsDao.clearProductivityBaselinesBySessionId(sessionId)
                metricsDao.clearPhysiologicalMetricsBySessionId(sessionId)
                metricsDao.clearPhysiologicalMetricsCompressedBySessionId(sessionId)
                metricsDao.clearPhysiologicalBaselinesBySessionId(sessionId)
                metricsDao.clearEmotionalMetricsBySessionId(sessionId)
                metricsDao.clearEmotionalMetricsCompressedBySessionId(sessionId)
                metricsDao.clearEEGRAWBySessionId(sessionId)
                metricsDao.clearEEGRAWCompressedBySessionId(sessionId)
                metricsDao.clearEEGPROCEEDBySessionId(sessionId)
                metricsDao.clearEEGPROCEEDCompressedBySessionId(sessionId)
                metricsDao.clearEEGArtifactsBySessionId(sessionId)
                metricsDao.clearEEGArtifactsCompressedBySessionId(sessionId)
                Log.d("MetricsRepository", "All metrics cleared by sessionId ${sessionId}")
            } catch (e: Exception){
                Log.e("MetricsRepository", "Error clearing metrics", e)
            }
        }
    }

    /** Удалить все метрики из базы данных */
    fun clearAllMetrics() {
        appScope.launch(ioDispatcher) {
            try {
                metricsDao.clearNFBMetrics()
                metricsDao.clearPhysiologicalMetrics()
                metricsDao.clearMEMSMetrics()
                metricsDao.clearProductivityMetrics()
                metricsDao.clearEmotionalMetrics()
                metricsDao.clearCardioMetrics()
                Log.d("MetricsRepository", "All metrics cleared")
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error clearing metrics", e)
            }
        }
    }

    /** Сбросить все буферы и создать сжатые метрики */
    suspend fun flushAllBuffers() {
        mutex.withLock {
            flushNfbBuffer()
            flushEEGRAWBuffer()
            flushEEGPROCEEDBuffer()
            flushEEGArtifactBuffer()
            flushPhysiologicalBuffer()
            flushMEMSBuffer()
            flushProductivityBuffer()
            flushEmotionalBuffer()
            flushCardioBuffer()
        }
    }

    /**
     * Сбросить буфер NFB и создать сжатую метрику (медиана за минуту).
     */
    private suspend fun flushNfbBuffer() {
        if (nfbBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = NFBMetricCompressedEntity(
            timestamp = nfbBuffer.firstTimestamp!!, // начало минутного интервала
            id = nfbBuffer.values.first().id,
            expedition_id = nfbBuffer.values.first().expedition_id,
            sessionId = nfbBuffer.values.first().sessionId,
            alpha = nfbBuffer.values.map { it.alpha }.median(),
            beta = nfbBuffer.values.map { it.beta }.median(),
            theta = nfbBuffer.values.map { it.theta }.median(),
            delta = nfbBuffer.values.map { it.delta }.median(),
            smr = nfbBuffer.values.map { it.smr }.median(),
            isMarked = false
        )
        metricsDao.insertNFBCompressedMetric(compressed)

        // Очищаем буфер
        nfbBuffer.values.clear()
        nfbBuffer.firstTimestamp = null
    }

    /**
     * Сбросить буфер сырых данных ЭЭГ и создать сжатую метрику.
     */
    private suspend fun flushEEGRAWBuffer() {
        if (eegRawBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = EEGRawMetricCompressedEntity(
            timestamp = eegRawBuffer.firstTimestamp!!, // начало минутного интервала
            id = eegRawBuffer.values.first().id,
            expedition_id = eegRawBuffer.values.first().expedition_id,
            sessionId = eegRawBuffer.values.first().sessionId,
            channel1 = eegRawBuffer.values.map { it.channel1 }.median(),
            channel2 = eegRawBuffer.values.map { it.channel2 }.median(),
            isMarked = false
        )
        metricsDao.insertEEGRAWCompressedMetric(compressed)

        // Очищаем буфер
        eegRawBuffer.values.clear()
        eegRawBuffer.firstTimestamp = null
    }

    /**
     * Сбросить буфер обработанных данных ЭЭГ и создать сжатую метрику.
     */
    private suspend fun flushEEGPROCEEDBuffer() {
        if (eegProceedBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = EEGProceedMetricCompressedEntity(
            timestamp = eegProceedBuffer.firstTimestamp!!, // начало минутного интервала
            id = eegProceedBuffer.values.first().id,
            expedition_id = eegProceedBuffer.values.first().expedition_id,
            sessionId = eegProceedBuffer.values.first().sessionId,
            channel1 = eegProceedBuffer.values.map { it.channel1 }.median(),
            channel2 = eegProceedBuffer.values.map { it.channel2 }.median(),
            isMarked = false
        )
        metricsDao.insertEEGPROCEEDCompressedMetric(compressed)

        // Очищаем буфер
        eegProceedBuffer.values.clear()
        eegProceedBuffer.firstTimestamp = null
    }

    /**
     * Сбросить буфер артефактов ЭЭГ и создать сжатую метрику.
     * Для булевых полей используется функция majority().
     */
    private suspend fun flushEEGArtifactBuffer() {
        if (eegArtifactBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = EEGArtifactsMetricCompressedEntity(
            timestamp = eegArtifactBuffer.firstTimestamp!!, // начало минутного интервала
            id = eegArtifactBuffer.values.first().id,
            expedition_id = eegArtifactBuffer.values.first().expedition_id,
            sessionId = eegArtifactBuffer.values.first().sessionId,
            artifactsChannel1 = eegArtifactBuffer.values.map { it.artifactsChannel1 }.majority(),
            artifactsChannel2 = eegArtifactBuffer.values.map { it.artifactsChannel2 }.majority(),
            qualityChannel1 = eegArtifactBuffer.values.map { it.qualityChannel1 }.median(),
            qualityChannel2 = eegArtifactBuffer.values.map { it.qualityChannel2 }.median(),
            isMarked = false
        )
        metricsDao.insertEEGArtifactsCompressedMetric(compressed)

        // Очищаем буфер
        eegArtifactBuffer.values.clear()
        eegArtifactBuffer.firstTimestamp = null
    }

    /**
     * Сбросить буфер физиологических метрик и создать сжатую метрику.
     */
    private suspend fun flushPhysiologicalBuffer() {
        if (physiologicalBuffer.values.isEmpty()) return

        val compressed = PhysiologicalMetricCompressedEntity(
            timestamp = physiologicalBuffer.firstTimestamp!!,
            id = physiologicalBuffer.values.first().id,
            expedition_id = physiologicalBuffer.values.first().expedition_id,
            sessionId = physiologicalBuffer.values.first().sessionId,
            relax = physiologicalBuffer.values.map { it.relax }.median(),
            fatigue = physiologicalBuffer.values.map { it.fatigue }.median(),
            none = physiologicalBuffer.values.map { it.none }.median(),
            concentration = physiologicalBuffer.values.map { it.concentration }.median(),
            involvement = physiologicalBuffer.values.map { it.involvement }.median(),
            stress = physiologicalBuffer.values.map { it.stress }.median(),
            nfbArtifacts = physiologicalBuffer.values.map { it.nfbArtifacts }.majority(),
            cardioArtifacts = physiologicalBuffer.values.map { it.cardioArtifacts }.majority(),
            isMarked = false
        )

        metricsDao.insertPhysiologicalCompressedMetric(compressed)

        // Очищаем буфер
        physiologicalBuffer.values.clear()
        physiologicalBuffer.firstTimestamp = null
    }

    /**
     * Сбросить буфер эмоциональных метрик и создать сжатую метрику.
     */
    private suspend fun flushEmotionalBuffer() {
        if (emotionalBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = EmotionalMetricCompressedEntity(
            timestamp = emotionalBuffer.firstTimestamp!!, // начало минутного интервала
            id = emotionalBuffer.values.first().id,
            expedition_id = emotionalBuffer.values.first().expedition_id,
            sessionId = emotionalBuffer.values.first().sessionId,
            attention = emotionalBuffer.values.map { it.attention }.median(),
            relaxation = emotionalBuffer.values.map { it.relaxation }.median(),
            cognitiveLoad = emotionalBuffer.values.map { it.cognitiveLoad }.median(),
            cognitiveControl = emotionalBuffer.values.map { it.cognitiveControl }.median(),
            selfControl = emotionalBuffer.values.map { it.selfControl }.median(),
            isMarked = false
        )
        metricsDao.insertEmotionalCompressedMetric(compressed)

        // Очищаем буфер
        emotionalBuffer.values.clear()
        emotionalBuffer.firstTimestamp = null
    }

    /**
     * Сбросить буфер метрик продуктивности и создать сжатую метрику.
     */
    private suspend fun flushProductivityBuffer() {
        if (productivityBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = ProductivityMetricCompressedEntity(
            timestamp = productivityBuffer.firstTimestamp!!, // начало минутного интервала
            id = productivityBuffer.values.first().id,
            expedition_id = productivityBuffer.values.first().expedition_id,
            sessionId = productivityBuffer.values.first().sessionId,
            gravity = productivityBuffer.values.map { it.gravity }.median(),
            productivity = productivityBuffer.values.map { it.productivity }.median(),
            fatigue = productivityBuffer.values.map { it.fatigue }.median(),
            reverseFatigue = productivityBuffer.values.map { it.reverseFatigue }.median(),
            relaxation = productivityBuffer.values.map { it.relaxation }.median(),
            concentration = productivityBuffer.values.map { it.concentration }.median(),
            isMarked = false
        )
        metricsDao.insertProductivityCompressedMetric(compressed)

        // Очищаем буфер
        productivityBuffer.values.clear()
        productivityBuffer.firstTimestamp = null
    }

    /**
     * Сбросить буфер MEMS данных и создать сжатую метрику.
     */
    suspend fun flushMEMSBuffer() {
        if (memsBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = MEMSMetricCompressedEntity(
            timestamp = memsBuffer.firstTimestamp!!, // начало минутного интервала
            id = memsBuffer.values.first().id,
            expedition_id = memsBuffer.values.first().expedition_id,
            sessionId = memsBuffer.values.first().sessionId,
            accelerometerX = memsBuffer.values.map { it.accelerometerX }.median(),
            accelerometerY = memsBuffer.values.map { it.accelerometerY }.median(),
            accelerometerZ = memsBuffer.values.map { it.accelerometerZ }.median(),
            gyroscopeX = memsBuffer.values.map { it.gyroscopeX }.median(),
            gyroscopeY = memsBuffer.values.map { it.gyroscopeY }.median(),
            gyroscopeZ = memsBuffer.values.map { it.gyroscopeZ }.median(),
            isMarked = false
        )
        metricsDao.insertMEMSCompressedMetric(compressed)

        // Очищаем буфер
        memsBuffer.values.clear()
        memsBuffer.firstTimestamp = null
    }

    /**
     * Сбросить буфер кардио метрик и создать сжатую метрику.
     */
    private suspend fun flushCardioBuffer() {
        if (cardioBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = CardioMetricCompressedEntity(
            timestamp = cardioBuffer.firstTimestamp!!, // начало минутного интервала
            id = cardioBuffer.values.first().id,
            expedition_id = cardioBuffer.values.first().expedition_id,
            sessionId = cardioBuffer.values.first().sessionId,
            heartRate = cardioBuffer.values.map { it.heartRate }.median(),
            hasArtifacts = cardioBuffer.values.map { it.hasArtifacts }.majority(),
            kaplanIndex = cardioBuffer.values.map { it.kaplanIndex }.median(),
            metricsAvailable = cardioBuffer.values.map { it.metricsAvailable }.majority(),
            motionArtifacts = cardioBuffer.values.map { it.motionArtifacts }.majority(),
            skinContact = cardioBuffer.values.map { it.skinContact }.majority(),
            stressIndex = cardioBuffer.values.map { it.stressIndex }.median(),
            isMarked = false
        )
        metricsDao.insertCardioCompressedMetric(compressed)

        // Очищаем буфер
        cardioBuffer.values.clear()
        cardioBuffer.firstTimestamp = null
    }


    /**
     * Вычислить медиану списка значений.
     */
    private fun List<Float>.median(): Float {
        if (isEmpty()) return 0f
        val sorted = sorted()
        val size = size
        return if (size % 2 == 0) (sorted[size / 2 - 1] + sorted[size / 2]) / 2 else sorted[size / 2]
    }

    /**
     * Определить большинство значений в списке булевых.
     * Возвращает true, если больше половины true.
     */
    private fun List<Boolean>.majority(): Boolean {
        if (isEmpty()) return false
        return count { it } > size / 2
    }
}