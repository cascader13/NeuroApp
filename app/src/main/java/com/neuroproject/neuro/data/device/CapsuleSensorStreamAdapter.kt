// data/device/CapsuleSensorStreamAdapter.kt
package com.neuroproject.neuro.data.device

import com.neuroproject.neuro.data.mapper.CapsuleCallbackMapper
import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.repository.SensorEvent
import com.neuroproject.neuro.domain.repository.SensorStreamGateway
import com.neuroproject.neuro.jni.JniCallbackHandler
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Адаптер потоков данных сенсоров нейро-гарнитуры.
 *
 * Реализует интерфейс [SensorStreamGateway] из Domain слоя.
 * Преобразует JNI callback'и в потоки (Flow) доменных моделей.
 *
 * Поддерживает наблюдение за:
 * - Основными данными: NFB, ЧСС, физиологические, MEMS, продуктивность, эмоции, ЭЭГ
 * - Калибровочными данными: базовые значения, индексы продуктивности
 *
 * Каждый тип данных имеет свой SharedFlow с буфером.
 * При переполнении буфера старые данные удаляются (DROP_OLDEST).
 *
 * @see CapsuleCallbackMapper
 * @see JniCallbackHandler
 * @see SessionIdProvider
 */
@Singleton
class CapsuleSensorStreamAdapter @Inject constructor(
    private val mapper: CapsuleCallbackMapper,
    private val sessionIdProvider: SessionIdProvider
) : SensorStreamGateway {

    // ============================================================
    // ОСНОВНЫЕ ПОТОКИ ДАННЫХ
    // ============================================================

    /** Поток NFB данных (спектральные характеристики ЭЭГ) */
    private val _nfbFlow = MutableSharedFlow<NFBSample>(
        replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток кардио данных (ЧСС, вариабельность ритма) */
    private val _cardioFlow = MutableSharedFlow<CardioSample>(
        replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток физиологических данных (расслабление, утомление, концентрация) */
    private val _physiologicalFlow = MutableSharedFlow<PhysiologicalSample>(
        replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _memsFlow = MutableSharedFlow<MEMSSample>(
        replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток данных продуктивности */
    private val _productivityFlow = MutableSharedFlow<ProductivitySample>(
        replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток эмоциональных данных (внимание, самоконтроль) */
    private val _emotionalFlow = MutableSharedFlow<EmotionalSample>(
        replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток сырых данных ЭЭГ */
    private val _eegRawFlow = MutableSharedFlow<EEGRawSample>(
        replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток обработанных данных ЭЭГ */
    private val _eegProcessedFlow = MutableSharedFlow<EEGProcessedSample>(
        replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток данных об артефактах ЭЭГ */
    private val _eegArtifactFlow = MutableSharedFlow<EEGArtifactSample>(
        replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    // ============================================================
    // КАЛИБРОВОЧНЫЕ ПОТОКИ ДАННЫХ
    // ============================================================

    /** Поток базовых значений продуктивности (калибровка) */
    private val _productivityBaselineFlow = MutableSharedFlow<ProductivityBaselineSample>(
        replay = 0, extraBufferCapacity = 50, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток индексов продуктивности */
    private val _productivityIndexesFlow = MutableSharedFlow<ProductivityIndexSample>(
        replay = 0, extraBufferCapacity = 50, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток базовых физиологических значений (калибровка) */
    private val _physiologicalBaselineFlow = MutableSharedFlow<PhysiologicalBaselineSample>(
        replay = 0, extraBufferCapacity = 50, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток оценки продуктивности */
    private val _productivityScoreFlow = MutableSharedFlow<ProductivityScoreSample>(
        replay = 0, extraBufferCapacity = 50, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Поток результатов калибровки ЭЭГ */
    private val _calibrationFlow = MutableSharedFlow<CalibrationSample>(
        replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        setupCallbacks()
    }

    /**
     * Настроить JNI callback'и для получения данных с датчиков.
     * Каждый callback преобразует данные через mapper и отправляет в соответствующий Flow.
     */
    private fun setupCallbacks() {
        // ============================================================
        // ОСНОВНЫЕ КОЛБЭКИ
        // ============================================================

        JniCallbackHandler.onNFBReceived = { time, alpha, beta, theta, delta, smr ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = mapper.toNFBSample(time, alpha, beta, theta, delta, smr, sessionId)
            _nfbFlow.tryEmit(sample)
        }

        JniCallbackHandler.onCardioReceived = { time, hr, hasArtifacts, kaplanIndex,
                                                metricsAvailable, motionArtifacts, skinContact, stress ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = mapper.toCardioSample(time, hr, hasArtifacts, kaplanIndex,
                metricsAvailable, motionArtifacts, skinContact, stress, sessionId)
            _cardioFlow.tryEmit(sample)
        }

        JniCallbackHandler.onPhysiologicalReceived = { time, relax, fatigue, none,
                                                       concentration, involvement, stress,
                                                       nfbArtifacts, cardioArtifacts ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = mapper.toPhysiologicalSample(time, relax, fatigue, none,
                concentration, involvement, stress, nfbArtifacts, cardioArtifacts, sessionId)
            _physiologicalFlow.tryEmit(sample)
        }

        JniCallbackHandler.onMEMSReceived = { time, accX, accY, accZ, gyrX, gyrY, gyrZ ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = mapper.toMEMSSample(time, accX, accY, accZ, gyrX, gyrY, gyrZ, sessionId)
            _memsFlow.tryEmit(sample)
        }

        JniCallbackHandler.onProductivityReceived = { time, timestampProd, gravity,
                                                      productivity, fatigue, reverseFatigue,
                                                      relaxation, concentration ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = mapper.toProductivitySample(time, timestampProd, gravity,
                productivity, fatigue, reverseFatigue, relaxation, concentration, sessionId)
            _productivityFlow.tryEmit(sample)
        }

        JniCallbackHandler.onEmotionReceived = { time, attention, relaxation,
                                                 cognitiveLoad, cognitiveControl, selfControl ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = mapper.toEmotionalSample(time, attention, relaxation,
                cognitiveLoad, cognitiveControl, selfControl, sessionId)
            _emotionalFlow.tryEmit(sample)
        }

        JniCallbackHandler.onEEGRawDataReceived = { time, ch1, ch2 ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = mapper.toEegRawSample(time, ch1, ch2, sessionId)
            _eegRawFlow.tryEmit(sample)
        }

        JniCallbackHandler.onEEGProcessedDataReceived = { time, ch1, ch2 ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = mapper.toEegProcessedSample(time, ch1, ch2, sessionId)
            _eegProcessedFlow.tryEmit(sample)
        }

        JniCallbackHandler.onEEGArtifactsReceived = { time, art1, art2, quality1, quality2 ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = mapper.toEegArtifactSample(time, art1, art2, quality1, quality2, sessionId)
            _eegArtifactFlow.tryEmit(sample)
        }

        // ============================================================
        // КАЛИБРОВОЧНЫЕ КОЛБЭКИ
        // ============================================================

        JniCallbackHandler.onCalibrationReceived = { indFrequency, indPeakFrequency,
                                                       indPeakFrequencyPower, indPeakFrequencySuppression,
                                                       indBandwidth, indNormalizedPower,
                                                       lowerFrequency, upperFrequency ->
            val sample = CalibrationSample(
                individualFrequency = indFrequency,
                individualPeakFrequency = indPeakFrequency,
                individualPeakFrequencyPower = indPeakFrequencyPower,
                individualPeakFrequencySuppression = indPeakFrequencySuppression,
                individualBandwidth = indBandwidth,
                individualNormalizedPower = indNormalizedPower,
                lowerFrequency = lowerFrequency,
                upperFrequency = upperFrequency
            )
            _calibrationFlow.tryEmit(sample)
        }

        JniCallbackHandler.onProductivityBaselineReceived = { time, gravity, productivity, fatigue,
                                                              reverseFatigue, relaxation, concentration ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = ProductivityBaselineSample(
                timestamp = time,
                sessionId = sessionId,
                userId = "",  // будет заполнено позже
                expeditionId = "",  // будет заполнено позже
                gravity = gravity,
                productivity = productivity,
                fatigue = fatigue,
                reverse_fatique = reverseFatigue,
                relaxation = relaxation,
                concentration = concentration
            )
            _productivityBaselineFlow.tryEmit(sample)
        }

        JniCallbackHandler.onProductivityIndexesReceived = { time, relaxation, stress, gravityBaseline,
                                                             productivityBaseline, fatigueBaseline,
                                                             reverseFatigueBaseline, relaxationBaseline,
                                                             concentrationBaseline, hasArtifacts ->
            val sessionId = sessionIdProvider.getCurrentSessionId()

            // Преобразуем числовые значения в строковые для модели
            val relaxationString = when (relaxation.toInt()) {
                0 -> "Involvement"
                1 -> "Relaxation"
                2 -> "SlightFatigue"
                3 -> "SevereFatigue"
                4 -> "ChronicFatigue"
                else -> "NoRecommendation"
            }
            val stressString = when (stress.toInt()) {
                0 -> "NoStress"
                1 -> "Anxiety"
                2 -> "Stress"
                else -> "NoStress"
            }

            val sample = ProductivityIndexSample(
                timestamp = time,
                sessionId = sessionId,
                userId = "",
                expeditionId = "",
                relaxation = relaxationString,
                stress = stressString,
                gravityBaseline = gravityBaseline,
                productivityBaseline = productivityBaseline,
                fatigueBaseline = fatigueBaseline,
                reverseFatiqueBaseline = reverseFatigueBaseline,
                relaxationBaseline = relaxationBaseline,
                concentrationBaseline = concentrationBaseline,
                hasArtifacts = hasArtifacts
            )
            _productivityIndexesFlow.tryEmit(sample)
        }

        JniCallbackHandler.onPhysiologicalBaselineReceived = { time, alpha, beta, alphaGravity,
                                                               betaGravity, concentration ->
            val sessionId = sessionIdProvider.getCurrentSessionId()
            val sample = PhysiologicalBaselineSample(
                timestamp = time,
                sessionId = sessionId,
                userId = "",
                expeditionId = "",
                alpha = alpha,
                beta = beta,
                alphaGravity = alphaGravity,
                betaGravity = betaGravity,
                concentration = concentration
            )
            _physiologicalBaselineFlow.tryEmit(sample)
        }

        JniCallbackHandler.onProductivityScore = { score ->
            val sample = ProductivityScoreSample(score = score)
            _productivityScoreFlow.tryEmit(sample)
        }
    }

    // ============================================================
    // РЕАЛИЗАЦИЯ ИНТЕРФЕЙСА
    // ============================================================

    /** Поток NFB данных */
    override fun observeNFB(): Flow<NFBSample> = _nfbFlow.asSharedFlow()

    /** Поток кардио данных */
    override fun observeHR(): Flow<CardioSample> = _cardioFlow.asSharedFlow()

    /** Поток физиологических данных */
    override fun observePhysiological(): Flow<PhysiologicalSample> = _physiologicalFlow.asSharedFlow()

    /** Поток MEMS данных */
    override fun observeMEMS(): Flow<MEMSSample> = _memsFlow.asSharedFlow()

    /** Поток данных продуктивности */
    override fun observeProductivity(): Flow<ProductivitySample> = _productivityFlow.asSharedFlow()

    /** Поток эмоциональных данных */
    override fun observeEmotional(): Flow<EmotionalSample> = _emotionalFlow.asSharedFlow()

    /** Поток сырых данных ЭЭГ */
    override fun observeEEGRaw(): Flow<EEGRawSample> = _eegRawFlow.asSharedFlow()

    /** Поток обработанных данных ЭЭГ */
    override fun observeEEGProcessed(): Flow<EEGProcessedSample> = _eegProcessedFlow.asSharedFlow()

    /** Поток данных об артефактах ЭЭГ */
    override fun observeEEGArtifacts(): Flow<EEGArtifactSample> = _eegArtifactFlow.asSharedFlow()

    /** Поток базовых значений продуктивности */
    override fun observeProductivityBaseline(): Flow<ProductivityBaselineSample> = _productivityBaselineFlow.asSharedFlow()

    /** Поток индексов продуктивности */
    override fun observeProductivityIndexes(): Flow<ProductivityIndexSample> = _productivityIndexesFlow.asSharedFlow()

    /** Поток базовых физиологических значений */
    override fun observePhysiologicalBaseline(): Flow<PhysiologicalBaselineSample> = _physiologicalBaselineFlow.asSharedFlow()

    /** Поток оценки продуктивности */
    override fun observeProductivityScore(): Flow<ProductivityScoreSample> = _productivityScoreFlow.asSharedFlow()

    /** Наблюдает за результатом калибровки (параметры ЭЭГ). */
    fun observeCalibrationResult(): Flow<CalibrationSample> = _calibrationFlow.asSharedFlow()

    /**
     * Объединённый поток всех событий сенсоров.
     * Используется для записи всех данных в сессию.
     */
    override fun observeAll(): Flow<SensorEvent> = merge(
        observeNFB().map { SensorEvent.NFB(it) },
        observeHR().map { SensorEvent.HR(it) },
        observePhysiological().map { SensorEvent.Physiological(it) },
        observeMEMS().map { SensorEvent.MEMS(it) },
        observeProductivity().map { SensorEvent.Productivity(it) },
        observeEmotional().map { SensorEvent.Emotional(it) },
        observeEEGRaw().map { SensorEvent.EEGRaw(it) },
        observeEEGProcessed().map { SensorEvent.EEGProcessed(it) },
        observeEEGArtifacts().map { SensorEvent.EEGArtifact(it) },
        observeProductivityBaseline().map { SensorEvent.ProductivityBaseline(it) },
        observeProductivityIndexes().map { SensorEvent.ProductivityIndexes(it) },
        observePhysiologicalBaseline().map { SensorEvent.PhysiologicalBaseline(it) },
        observeProductivityScore().map { SensorEvent.ProductivityScore(it) }
    )

    // ============================================================
    // ПОИСК УСТРОЙСТВ
    // ============================================================

    /**
     * Начать поиск доступных Bluetooth устройств.
     *
     * @return Flow со списком найденных устройств
     */
    fun searchDevices(): Flow<List<DeviceInfo>> = callbackFlow {
        val devices = mutableMapOf<String, DeviceInfo>()

        JniCallbackHandler.onDeviceFound = { name, address ->
            android.util.Log.d("SensorAdapter", "Device found: name=$name, address=$address")
            val device = DeviceInfo(name = name, address = address)
            devices[address] = device
            trySend(devices.values.toList())
                .onFailure { cause ->
                    android.util.Log.e("SensorAdapter", "Failed to send devices", cause)
                }
        }

        awaitClose {
            android.util.Log.d("SensorAdapter", "Search stopped, clearing callback")
            JniCallbackHandler.onDeviceFound = null
        }
    }
}

/**
 * Провайдер текущего ID сессии.
 *
 * Используется для привязки данных с датчиков к конкретной сессии записи.
 * Обновляется при начале/завершении сессии.
 */
@Singleton
class SessionIdProvider @Inject constructor() {
    private val _currentSessionId = MutableStateFlow("")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    /**
     * Установить текущий ID сессии.
     *
     * @param sessionId ID сессии (timestamp начала)
     */
    fun setSessionId(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    /**
     * Получить текущий ID сессии.
     *
     * @return ID текущей сессии или пустая строка
     */
    fun getCurrentSessionId(): String = _currentSessionId.value
}