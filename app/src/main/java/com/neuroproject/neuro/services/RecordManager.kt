package com.neuroproject.neuro.services

import android.content.Context
import android.util.Log
import com.neuroproject.neuro.data.MetricsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Менеджер записи и сохранения данных с нейро-гарнитуры
 *
 * Этот класс отвечает за управление процессом записи данных, поступающих
 * от [CapsuleDeviceManager], и их сохранение в базу данных через [MetricsRepository].
 *
 * ## Основные функции:
 * - Управление состоянием записи (старт/стоп)
 * - Подписка на потоки данных от [CapsuleDeviceManager]
 * - Сохранение различных типов данных: NFB, ЭЭГ, физиологические, кардио, MEMS, продуктивность
 * - Автоматическая привязка данных к пользователю, экспедиции и сессии
 * - Кэширование ID пользователя и экспедиции из SharedPreferences
 *
 * ## Потоки данных:
 * - **NFB данные** - нейрофидбек (альфа, бета, тета, дельта, SMR ритмы)
 * - **ЭЭГ данные** - сырые и обработанные сигналы, артефакты
 * - **Физиологические данные** - расслабление, утомление, концентрация, стресс
 * - **Кардио данные** - ЧСС, индекс Каплана, качество сигнала
 * - **MEMS данные** - акселерометр и гироскоп
 * - **Продуктивность** - показатели эффективности когнитивной деятельности
 * - **Эмоциональные данные** - внимание, когнитивная нагрузка, самоконтроль
 *
 * ## Пример использования:
 * ```kotlin
 * class RecordingViewModel @Inject constructor(
 *     private val recordManager: RecordManager
 * ) {
 *     fun startSession(sessionId: Long) {
 *         recordManager.setSessionId(sessionId)
 *         recordManager.startRecording()
 *     }
 *
 *     suspend fun stopSession() {
 *         recordManager.stopRecording()
 *     }
 * }
 * ```
 *
 * @param deviceManager Экземпляр [CapsuleDeviceManager] для получения данных
 * @param context Контекст приложения для доступа к SharedPreferences
 * @param metricsRepository Репозиторий для сохранения метрик в БД
 * @see CapsuleDeviceManager
 * @see MetricsRepository
 */
@Singleton
class RecordManager @Inject constructor(
    deviceManager: CapsuleDeviceManager,
    @ApplicationContext private val context: Context,
    private val metricsRepository: MetricsRepository
) {
    private var _instance = this

    /** SharedPreferences для хранения ID пользователя и экспедиции */
    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    init {
        _instance = this
        Log.d("RecordManager", "init")
        loadSavedIds()
    }

    /** Менеджер устройства для подписки на данные */
    private val capsuleDM = deviceManager

    /** CoroutineScope для фоновых операций */
    private val _scope = CoroutineScope(EmptyCoroutineContext)

    /** Флаг активной записи данных */
    private var isRecording = false

    /** Флаг настройки слушателей данных */
    private var isSetup = false

    /** Поток состояния NFB данных (для внутреннего использования) */
    private val _nfbState = MutableStateFlow(NFBData())

    public val productivityScore = deviceManager.productivityScore // stateFlow для получения данных о состоянии калибровки productivity

    /** ID текущей сессии записи */
    private var currentSessionId: Long? = null

    /**
     * Установка ID текущей сессии
     *
     * Должен быть вызван перед началом записи для правильной привязки данных
     *
     * @param sessionId ID сессии из базы данных
     */
    fun setSessionId(sessionId: Long) {
        currentSessionId = sessionId
    }

    /**
     * Получение ID текущей сессии
     *
     * @return ID текущей сессии или null если сессия не установлена
     */
    fun getSession(): Long? {
        return currentSessionId
    }

    // ID пользователя и экспедиции из SharedPreferences
    private var userId: String = ""
    private var expeditionId: String = ""

    /**
     * Загрузка ID пользователя и экспедиции из SharedPreferences
     *
     * Читает сохраненные значения:
     * - "saved_user_id" - идентификатор текущего пользователя
     * - "saved_expedition_id" - идентификатор текущей экспедиции
     */
    private fun loadSavedIds() {
        userId = sharedPreferences.getString("saved_user_id", "") ?: ""
        expeditionId = sharedPreferences.getString("saved_expedition_id", "") ?: ""

        Log.d("RecordManager", "Loaded IDs - userId: $userId, expeditionId: $expeditionId")
    }

    /**
     * Обновление ID пользователя и экспедиции
     *
     * Используется для перезагрузки актуальных значений после изменения
     * настроек пользователя или экспедиции.
     */
    fun refreshIds() {
        loadSavedIds()
        Log.d("RecordManager", "IDs refreshed - userId: $userId, expeditionId: $expeditionId")
    }

    /**
     * Начало записи данных
     *
     * Активирует флаг записи и настраивает слушатели данных, если они еще не настроены.
     * Перед началом записи обновляет ID пользователя и экспедиции.
     *
     * @see stopRecording
     */
    fun startRecording() {
        if (!isSetup) setupCapsuleListeners()
        refreshIds()
        isRecording = true
        Log.d(
            "Record Manager",
            "Recording started with userId: $userId, expeditionId: $expeditionId"
        )
    }

    /**
     * Остановка записи данных
     *
     * Деактивирует флаг записи и принудительно сбрасывает все буферы данных
     * в базу данных, чтобы сохранить последние полученные значения.
     *
     * @throws Exception если происходит ошибка при сбросе буферов
     */
    suspend fun stopRecording() {
        isRecording = false
        Log.d("Record Manager", "Recording stopped")
        metricsRepository.flushAllBuffers()
    }

    /**
     * Вспомогательная функция для сбора данных из StateFlow
     *
     * Обеспечивает удобный способ подписки на Flow в заданном CoroutineScope.
     *
     * @param scope CoroutineScope для выполнения коллекции
     * @param action Действие, выполняемое при каждом новом значении
     */
    private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectInScope(
        scope: CoroutineScope,
        action: (T) -> Unit
    ) {
        scope.launch {
            this@collectInScope.collect { value ->
                action(value)
            }
        }
    }

    /**
     * Настройка слушателей данных от CapsuleDeviceManager
     *
     * Подписывается на все доступные потоки данных и настраивает
     * автоматическое сохранение при активной записи.
     * Вызывается автоматически при первом старте записи.
     */
    private fun setupCapsuleListeners() {
        isSetup = true

        /**
         * Слушатель для NFB данных (нейрофидбек)
         *
         * Сохраняет спектральные характеристики ЭЭГ:
         * - alpha (8-13 Гц) - состояние покоя
         * - beta (13-30 Гц) - активное мышление
         * - theta (4-8 Гц) - дремотное состояние
         * - delta (0.5-4 Гц) - глубокий сон
         * - smr (12-15 Гц) - сенсомоторный ритм
         */
        capsuleDM.nfbReceived =
            { time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float ->
                _scope.launch {
                    _nfbState.emit(NFBData(time, alpha, beta, theta, delta, smr))

                    if (isRecording) {
                        saveNFBData(time, alpha, beta, theta, delta, smr)
                    }
                }
            }

        /** Слушатель для физиологических данных */
        capsuleDM.physiologicalData.collectInScope(_scope) { data ->
            if (isRecording) {
                savePhysiologicalData(
                    data.timeStampMilli,
                    data.relax,
                    data.fatigue,
                    data.none,
                    data.concentration,
                    data.involvement,
                    data.stress,
                    data.nfbArtifacts,
                    data.cardioArtifacts
                )
            }
        }

        /** Слушатель для кардио данных (ЧСС) */
        capsuleDM.hrData.collectInScope(_scope) { hr ->
            if (isRecording) {
                saveCardioData(
                    hr.timeStampMilli,
                    hr.heartRate,
                    hr.hasArtifacts,
                    hr.kaplanIndex,
                    hr.metricsAvailable,
                    hr.motionArtifacts,
                    hr.skinContact,
                    hr.stress
                )
            }
        }

        /** Слушатель для MEMS данных (акселерометр/гироскоп) */
        capsuleDM.memsData.collectInScope(_scope) { mems ->
            if (isRecording) {
                saveMEMSData(
                    mems.timeStampMilli,
                    mems.accelerometer_x,
                    mems.accelerometer_y,
                    mems.accelerometer_z,
                    mems.gyroscope_x,
                    mems.gyroscope_y,
                    mems.gyroscope_z
                )
            }
        }
        Log.d("RecordManger", "start connecting Productivity")
        /** Слушатель для данных продуктивности */
        capsuleDM.productivityData.collectInScope(_scope) { productivity ->
            Log.d("RecordManager", "Productivity data received in collector! isRecording=$isRecording")
            Log.d("RecordManager", "Productivity: time=${productivity.timeStampMilli}, value=${productivity.productivity}")
            if (isRecording) {
                saveProductivityData(
                    productivity.timeStampMilli,
                    productivity.gravity,
                    productivity.productivity,
                    productivity.fatigue,
                    productivity.reverse_fatique,
                    productivity.relaxation,
                    productivity.concentration
                )
            }
        }

        /** Слушатель для индексов продуктивности */
        capsuleDM.productivityIndexData.collectInScope(_scope) { productivityIndexes ->
            if (isRecording) {
                saveProductivityIndexData(
                    productivityIndexes.time,
                    productivityIndexes.relaxation,
                    productivityIndexes.stress,
                    productivityIndexes.gravityBaseline,
                    productivityIndexes.productivityBaseline,
                    productivityIndexes.fatigueBaseline,
                    productivityIndexes.reverseFatiqueBaseline,
                    productivityIndexes.relaxationBaseline,
                    productivityIndexes.concentrationBaseline,
                    productivityIndexes.hasArtifacts
                )
            }
        }

        /** Слушатель для базовых значений продуктивности */
        capsuleDM.productivityBaselineData.collectInScope(_scope) { productivityBaseline ->

            if (isRecording) {
                saveProductivityBaselineData(
                    productivityBaseline.time,
                    productivityBaseline.gravity,
                    productivityBaseline.productivity,
                    productivityBaseline.fatigue,
                    productivityBaseline.reverse_fatique,
                    productivityBaseline.relaxation,
                    productivityBaseline.concentration
                )

            }
        }

        /** Слушатель для физиологических базовых значений */
        capsuleDM.physiologicalBaselineData.collectInScope(_scope) { physiologicalBaseline ->
            if (isRecording) {
                savePhysiologicalBaselineData(
                    physiologicalBaseline.time,
                    physiologicalBaseline.alpha,
                    physiologicalBaseline.beta,
                    physiologicalBaseline.alphaGravity,
                    physiologicalBaseline.betaGravity,
                    physiologicalBaseline.concentration
                )
            }
        }

        /** Слушатель для эмоциональных данных */
        capsuleDM.emotionalData.collectInScope(_scope) { emotion ->
            if (isRecording) {
                saveEmotionalData(
                    emotion.timeStampMilli,
                    emotion.attention,
                    emotion.relaxation,
                    emotion.cognitive_load,
                    emotion.cognitive_control,
                    emotion.self_control
                )
            }
        }

        /** Слушатель для сырых данных ЭЭГ */
        capsuleDM.eegRawData.collectInScope(_scope) { eegRaw ->
            if (isRecording) {
                saveEEGRAWData(eegRaw.timeStampMilli, eegRaw.channel1, eegRaw.channel2)
            }
        }

        /** Слушатель для обработанных данных ЭЭГ */
        capsuleDM.eegProcessedData.collectInScope(_scope) { eegProceed ->
            if (isRecording) {
                saveEEGPROCEEDData(
                    eegProceed.timeStampMilli,
                    eegProceed.channel1,
                    eegProceed.channel2
                )
            }
        }

        /** Слушатель для артефактов ЭЭГ */
        capsuleDM.eegArtifacts.collectInScope(_scope) { eegArt ->
            if (isRecording) {
                saveEEGArtifactData(
                    eegArt.timeStampMilli,
                    eegArt.artifactsChannel1,
                    eegArt.artifactsChannel2,
                    eegArt.qualityChannel1,
                    eegArt.qualityChannel2
                )
            }
        }
    }


    /**
    * Сохранение NFB данных
     *
     * @param time Временная метка
     * @param alpha Альфа-ритм (8-13 Гц)
     * @param beta Бета-ритм (13-30 Гц)
     * @param theta Тета-ритм (4-8 Гц)
     * @param delta Дельта-ритм (0.5-4 Гц)
     * @param smr Сенсомоторный ритм (12-15 Гц)
     */
    private fun saveNFBData(
        time: Long,
        alpha: Float,
        beta: Float,
        theta: Float,
        delta: Float,
        smr: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty() && alpha <= 1.0) {
            metricsRepository.saveNFBMetric(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                alpha,
                beta,
                theta,
                delta,
                smr
            )
            Log.d("RecordManager", "NFB data saved: alpha=$alpha, beta=$beta")
        } else {
            if (alpha > 1) {
                Log.e("RecordManager", "Invalid NFB data")
            } else {
                Log.e("RecordManager", "Cannot save NFB data: userId or expeditionId is empty")
            }
        }
    }

    /**
     * Сохранение сырых данных ЭЭГ
     *
     * @param time Временная метка
     * @param channel1 Значение с первого канала
     * @param channel2 Значение со второго канала
     */
    private fun saveEEGRAWData(time: Long, channel1: Float, channel2: Float) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveEEGRAWMetric(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                channel1,
                channel2
            )
        } else {
            Log.e("RecordManager", "Cannot save EEG RAW data: userId or expeditionId is empty")
        }
    }

    /**
     * Сохранение обработанных данных ЭЭГ
     *
     * @param time Временная метка
     * @param channel1 Обработанное значение первого канала
     * @param channel2 Обработанное значение второго канала
     */
    private fun saveEEGPROCEEDData(time: Long, channel1: Float, channel2: Float) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveEEGPROCEEDMetric(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                channel1,
                channel2
            )
        } else {
            Log.e("RecordManager", "Cannot save EEG PROCEED data: userId or expeditionId is empty")
        }
    }

    /**
     * Сохранение данных об артефактах ЭЭГ
     *
     * @param time Временная метка
     * @param ArtifactChannel1 Наличие артефактов на первом канале
     * @param ArtifactChannel2 Наличие артефактов на втором канале
     * @param QualityChannel1 Качество сигнала первого канала (0-1)
     * @param QualityChannel2 Качество сигнала второго канала (0-1)
     */
    private fun saveEEGArtifactData(
        time: Long,
        ArtifactChannel1: Boolean,
        ArtifactChannel2: Boolean,
        QualityChannel1: Float,
        QualityChannel2: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveEEGArtifactMetric(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                ArtifactChannel1,
                ArtifactChannel2,
                QualityChannel1,
                QualityChannel2
            )
        } else {
            Log.e("RecordManager", "Cannot save EEG Artifact data: userId or expeditionId is empty")
        }
    }

    /**
     * Сохранение физиологических данных
     *
     * @param time Временная метка
     * @param relax Уровень расслабления (0-1)
     * @param fatigue Уровень утомления (0-1)
     * @param none Нейтральное состояние
     * @param concentration Уровень концентрации (0-1)
     * @param involvement Уровень вовлеченности (0-1)
     * @param stress Уровень стресса (0-1)
     * @param nfbArtifacts Наличие артефактов в NFB сигнале
     * @param cardioArtifacts Наличие артефактов в кардиосигнале
     */
    private fun savePhysiologicalData(
        time: Long,
        relax: Float,
        fatigue: Float,
        none: Float,
        concentration: Float,
        involvement: Float,
        stress: Float,
        nfbArtifacts: Boolean,
        cardioArtifacts: Boolean
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.savePhysiologicalMetric(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                relax,
                fatigue,
                none,
                concentration,
                involvement,
                stress,
                nfbArtifacts,
                cardioArtifacts
            )
        } else {
            Log.e(
                "RecordManager",
                "Cannot save Physiological data: userId or expeditionId is empty"
            )
        }
    }

    /**
     * Сохранение кардио данных (ЧСС)
     *
     * @param time Временная метка
     * @param heartRate Частота сердечных сокращений (уд/мин)
     * @param hasArtifacts Наличие артефактов
     * @param kaplanIndex Индекс Каплана (вариабельность сердечного ритма)
     * @param metricsAvailable Доступность метрик
     * @param motionArtifact Артефакты движения
     * @param skinContact Качество контакта с кожей
     * @param stressIndex Уровень стресса на основе кардиоданных
     */
    private fun saveCardioData(
        time: Long,
        heartRate: Float,
        hasArtifacts: Boolean,
        kaplanIndex: Float,
        metricsAvailable: Boolean,
        motionArtifact: Boolean,
        skinContact: Boolean,
        stressIndex: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveCardioMetric(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                heartRate,
                hasArtifacts,
                kaplanIndex,
                metricsAvailable,
                motionArtifact,
                skinContact,
                stressIndex
            )
        } else {
            Log.e("RecordManager", "Cannot save Cardio data: userId or expeditionId is empty")
        }
    }

    /**
     * Сохранение данных MEMS-датчиков
     *
     * @param time Временная метка
     * @param accX Ускорение по оси X
     * @param accY Ускорение по оси Y
     * @param accZ Ускорение по оси Z
     * @param gyroX Угловая скорость по оси X
     * @param gyroY Угловая скорость по оси Y
     * @param gyroZ Угловая скорость по оси Z
     */
    private fun saveMEMSData(
        time: Long, accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveMEMSMetric(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                accX, accY, accZ,
                gyroX, gyroY, gyroZ
            )
        } else {
            Log.e("RecordManager", "Cannot save MEMS data: userId or expeditionId is empty")
        }
    }

    /**
     * Сохранение данных продуктивности
     *
     * @param time Временная метка
     * @param gravity Гравитационная составляющая
     * @param productivity Уровень продуктивности
     * @param fatigue Уровень утомления
     * @param reverseFatigue Обратный уровень утомления
     * @param relaxation Уровень расслабления
     * @param concentration Уровень концентрации
     */
    private fun saveProductivityData(
        time: Long,
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveProductivityMetric(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                gravity,
                productivity,
                fatigue,
                reverseFatigue,
                relaxation,
                concentration
            )
        } else {
            Log.e("RecordManager", "Cannot save Productivity data: userId or expeditionId is empty")
        }
    }

    /**
     * Сохранение индексов продуктивности
     *
     * @param time Временная метка
     * @param relaxation Рекомендация по расслаблению
     * @param stress Уровень стресса текстовым описанием
     * @param gravityBaseline Базовый уровень гравитации
     * @param productivityBaseline Базовый уровень продуктивности
     * @param fatigueBaseline Базовый уровень утомления
     * @param reverseFatiqueBaseline Обратный базовый уровень утомления
     * @param relaxationBaseline Базовый уровень расслабления
     * @param concentrationBaseline Базовый уровень концентрации
     * @param hasArtifacts Наличие артефактов
     */
    private fun saveProductivityIndexData(
        time: Long,
        relaxation: String,
        stress: String,
        gravityBaseline: Float,
        productivityBaseline: Float,
        fatigueBaseline: Float,
        reverseFatiqueBaseline: Float,
        relaxationBaseline: Float,
        concentrationBaseline: Float,
        hasArtifacts: Boolean = false
    ) {
        if(userId.isEmpty()){
            metricsRepository.saveProductivityCalibration(
                userId,
                gravityBaseline,
                productivityBaseline,
                fatigueBaseline,
                reverseFatiqueBaseline,
                relaxationBaseline,
                concentrationBaseline
            )
        }
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveProductivityIndexes(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                relaxation,
                stress,
                gravityBaseline,
                productivityBaseline,
                fatigueBaseline,
                reverseFatiqueBaseline,
                relaxationBaseline,
                concentrationBaseline,
                hasArtifacts
            )
        }
    }

    /**
     * Сохранение базовых значений продуктивности
     *
     * @param time Временная метка
     * @param gravity Гравитационная составляющая
     * @param productivity Уровень продуктивности
     * @param fatigue Уровень утомления
     * @param reverseFatigue Обратный уровень утомления
     * @param relaxation Уровень расслабления
     * @param concentration Уровень концентрации
     */
    private fun saveProductivityBaselineData(
        time: Long,
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveProductivityBaselines(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                gravity,
                productivity,
                fatigue,
                reverseFatigue,
                relaxation,
                concentration
            )
        }
    }

    /**
     * Сохранение физиологических базовых значений
     *
     * @param time Временная метка
     * @param alpha Альфа-активность
     * @param beta Бета-активность
     * @param alphaGravity Гравитационная составляющая альфа-ритма
     * @param betaGravity Гравитационная составляющая бета-ритма
     * @param concentration Уровень концентрации
     */
    private fun savePhysiologicalBaselineData(
        time: Long,
        alpha: Float,
        beta: Float,
        alphaGravity: Float,
        betaGravity: Float,
        concentration: Float
    ) {
        if (userId.isNotEmpty()){
            metricsRepository.savePhysiologicalCalibration(userId, alpha, beta, alphaGravity, betaGravity, concentration)
        }
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.savePhysiologicalBaselines(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                alpha,
                beta,
                alphaGravity,
                betaGravity,
                concentration
            )
        }
    }

    /**
     * Сохранение эмоциональных данных
     *
     * @param time Временная метка
     * @param attention Уровень внимания
     * @param relaxation Уровень расслабления
     * @param cognitiveLoad Когнитивная нагрузка
     * @param cognitiveControl Когнитивный контроль
     * @param selfControl Самоконтроль
     */
    private fun saveEmotionalData(
        time: Long,
        attention: Float,
        relaxation: Float,
        cognitiveLoad: Float,
        cognitiveControl: Float,
        selfControl: Float
    ) {
        if (userId.isNotEmpty() && expeditionId.isNotEmpty()) {
            metricsRepository.saveEmotionalMetric(
                time,
                userId,
                expeditionId,
                currentSessionId ?: return,
                attention,
                relaxation,
                cognitiveLoad,
                cognitiveControl,
                selfControl
            )
        } else {
            Log.e("RecordManager", "Cannot save Emotional data: userId or expeditionId is empty")
        }
    }


}