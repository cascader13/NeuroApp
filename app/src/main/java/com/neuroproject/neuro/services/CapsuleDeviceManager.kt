package com.neuroproject.neuro.services

import android.content.Context
import android.util.Log
import com.neuroproject.neuro.data.CalibrationHistoryEntity
import com.neuroproject.neuro.data.MetricsDao
import com.neuroproject.neuro.data.MonitorUploadRepository
import com.neuroproject.neuro.models.BaselineValues
import com.neuroproject.neuro.models.CapsuleInitializedState
import com.neuroproject.neuro.models.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Перечисление этапов калибровки устройства Capsule
 *
 * Определяет все возможные состояния процесса калибровки нейро-гарнитуры,
 * от начальной настройки до финальных этапов физиологической регистрации.
 *
 * @property value числовое представление этапа, используемое в нативном коде
 */
enum class CapsuleStages(val value: Int) {
    /** Неизвестное состояние калибратора */
    CALIBRATOR_UNKNOWN_STAGE(-2),
    /** Калибратор готов к работе */
    CALIBRATOR_READY_STAGE(-1),
    /** Первый этап калибровки */
    CALIBRATOR_STAGE1(0),
    /** Второй этап калибровки */
    CALIBRATOR_STAGE2(1),
    /** Третий этап калибровки */
    CALIBRATOR_STAGE3(2),
    /** Четвертый этап калибровки */
    CALIBRATOR_STAGE4(3),
    /** Этап инициализации физиологических измерений */
    PHYSIO_INIT_STAGE(4),
    /** Этап установления физиологического базового уровня */
    PHYSIO_BASELINE_STAGE(5),
    /** Ошибка калибратора */
    CALIBRATOR_ERROR_STAGE(6);

    companion object {
        /**
         * Преобразует числовое значение в соответствующее перечисление
         *
         * @param value числовое значение этапа
         * @return соответствующий этап калибровки, или [CALIBRATOR_UNKNOWN_STAGE] если значение не найдено
         */
        fun fromInt(value: Int) =
            CapsuleStages.entries.firstOrNull { it.value == value } ?: CALIBRATOR_UNKNOWN_STAGE
    }
}

/**
 * Состояние подключения устройства
 *
 * Определяет текущий статус соединения с нейро-гарнитурой
 */
enum class DeviceConnectionState {
    /** Процесс подключения */
    connection,
    /** Устройство подключено и готово к работе */
    connected,
    /** Процесс отключения */
    disconnection,
    /** Устройство отключено */
    disconnected,
    /** Ошибка подключения */
    error
}

/**
 * Физиологические данные пользователя
 *
 * Содержит комплексные показатели психофизиологического состояния
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property relax уровень расслабления (0-1)
 * @property fatigue уровень утомления (0-1)
 * @property none нейтральное состояние
 * @property concentration уровень концентрации (0-1)
 * @property involvement уровень вовлеченности (0-1)
 * @property stress уровень стресса (0-1)
 * @property nfbArtifacts наличие артефактов в нейрофидбек-сигнале
 * @property cardioArtifacts наличие артефактов в кардиосигнале
 */
data class PhysiologicalData(
    val timeStampMilli: Long = 0,
    val relax: Float = 0f,
    val fatigue: Float = 0f,
    val none: Float = 0f,
    val concentration: Float = 0f,
    val involvement: Float = 0f,
    val stress: Float = 0f,
    val nfbArtifacts: Boolean = true,
    val cardioArtifacts: Boolean = true
)

/**
 * Данные нейрофидбека (НФБ)
 *
 * Содержит спектральные характеристики ЭЭГ сигнала
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property alpha альфа-ритм (8-13 Гц) - состояние покоя
 * @property beta бета-ритм (13-30 Гц) - активное мышление
 * @property theta тета-ритм (4-8 Гц) - дремотное состояние
 * @property delta дельта-ритм (0.5-4 Гц) - глубокий сон
 * @property smr сенсомоторный ритм (12-15 Гц)
 */
data class NFBData(
    val timeStampMilli: Long = 0,
    val alpha: Float = 0f,
    val beta: Float = 0f,
    val theta: Float = 0f,
    val delta: Float = 0f,
    val smr: Float = 0f
)

/**
 * Данные с MEMS-датчиков (акселерометр и гироскоп)
 *
 * Используется для отслеживания движений головы и компенсации артефактов
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property accelerometer_x ускорение по оси X
 * @property accelerometer_y ускорение по оси Y
 * @property accelerometer_z ускорение по оси Z
 * @property gyroscope_x угловая скорость по оси X
 * @property gyroscope_y угловая скорость по оси Y
 * @property gyroscope_z угловая скорость по оси Z
 */
data class MEMSdata(
    val timeStampMilli: Long = 0,
    val accelerometer_x: Float = 0f,
    val accelerometer_y: Float = 0f,
    val accelerometer_z: Float = 0f,
    val gyroscope_x: Float = 0f,
    val gyroscope_y: Float = 0f,
    val gyroscope_z: Float = 0f
)

/**
 * Данные продуктивности
 *
 * Комплексный показатель эффективности когнитивной деятельности
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property timestamp_prod альтернативная временная метка
 * @property gravity гравитационная составляющая
 * @property productivity уровень продуктивности
 * @property fatigue уровень утомления
 * @property reverse_fatique обратный показатель утомления
 * @property relaxation уровень расслабления
 * @property concentration уровень концентрации
 */
data class Productivitydata(
    val timeStampMilli: Long = 0,
    val timestamp_prod: Double = 0.0,
    val gravity: Float = 0f,
    val productivity: Float = 0f,
    val fatigue: Float = 0f,
    val reverse_fatique: Float = 0f,
    val relaxation: Float = 0f,
    val concentration: Float = 0f
)

/**
 * Эмоциональные показатели
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property attention уровень внимания
 * @property relaxation уровень расслабления
 * @property cognitive_load когнитивная нагрузка
 * @property cognitive_control когнитивный контроль
 * @property self_control самоконтроль
 */
data class Emotionaldata(
    val timeStampMilli: Long = 0,
    val attention: Float = 0f,
    val relaxation: Float = 0f,
    val cognitive_load: Float = 0f,
    val cognitive_control: Float = 0f,
    val self_control: Float = 0f
)

/**
 * Кардиологические данные (ЧСС и связанные показатели)
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property heartRate частота сердечных сокращений (уд/мин)
 * @property hasArtifacts наличие артефактов в сигнале
 * @property kaplanIndex индекс Каплана (вариабельность сердечного ритма)
 * @property metricsAvailable доступность метрик
 * @property motionArtifacts артефакты движения
 * @property skinContact качество контакта с кожей
 * @property stress уровень стресса на основе кардиоданных
 */
data class Cardiodata(
    val timeStampMilli: Long = 0,
    val heartRate: Float = 0f,
    val hasArtifacts: Boolean = false,
    val kaplanIndex: Float = 0f,
    val metricsAvailable: Boolean = false,
    val motionArtifacts: Boolean = false,
    val skinContact: Boolean = false,
    val stress: Float = 0f
)

/**
 * Сырые данные ЭЭГ
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property channel1 значение с первого канала
 * @property channel2 значение со второго канала
 */
data class EEGRawSample(
    val timeStampMilli: Long = 0,
    val channel1: Float = 0f,
    val channel2: Float = 0f
)

/**
 * Обработанные данные ЭЭГ
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property channel1 обработанное значение первого канала
 * @property channel2 обработанное значение второго канала
 */
data class EEGProcessedSample(
    val timeStampMilli: Long = 0,
    val channel1: Float = 0f,
    val channel2: Float = 0f
)

/**
 * Информация об артефактах ЭЭГ
 *
 * @property timeStampMilli временная метка в миллисекундах
 * @property artifactsChannel1 наличие артефактов на первом канале
 * @property artifactsChannel2 наличие артефактов на втором канале
 * @property qualityChannel1 качество сигнала первого канала (0-1)
 * @property qualityChannel2 качество сигнала второго канала (0-1)
 */
data class EEGArtifactsSample(
    val timeStampMilli: Long = 0,
    val artifactsChannel1: Boolean = false,
    val artifactsChannel2: Boolean = false,
    val qualityChannel1: Float = 0f,
    val qualityChannel2: Float = 0f
)

/**
 * Индексы продуктивности с рекомендациями
 *
 * @property time временная метка
 * @property relaxation рекомендация по расслаблению
 * @property stress уровень стресса текстовым описанием
 * @property gravityBaseline базовый уровень гравитации
 * @property productivityBaseline базовый уровень продуктивности
 * @property fatigueBaseline базовый уровень утомления
 * @property reverseFatiqueBaseline обратный базовый уровень утомления
 * @property relaxationBaseline базовый уровень расслабления
 * @property concentrationBaseline базовый уровень концентрации
 * @property hasArtifacts наличие артефактов
 */
data class ProductivityIndexes(
    val time: Long = 0,
    val relaxation: String = "NoRecommendation",
    val stress: String = "NoStress",
    val gravityBaseline: Float = 1f,
    val productivityBaseline: Float = 1f,
    val fatigueBaseline: Float = 1f,
    val reverseFatiqueBaseline: Float = 1f,
    val relaxationBaseline: Float = 1f,
    val concentrationBaseline: Float = 1f,
    val hasArtifacts: Boolean = false
)

/**
 * Базовые значения продуктивности
 *
 * @property time временная метка
 * @property gravity гравитационная составляющая
 * @property productivity уровень продуктивности
 * @property fatigue уровень утомления
 * @property reverse_fatique обратный уровень утомления
 * @property relaxation уровень расслабления
 * @property concentration уровень концентрации
 */
data class ProductivityBaseline(
    val time: Long = 0,
    val gravity: Float = 1f,
    val productivity: Float = 1f,
    val fatigue: Float = 1f,
    val reverse_fatique: Float = 1f,
    val relaxation: Float = 1f,
    val concentration: Float = 1f
)

/**
 * Физиологические базовые значения
 *
 * @property time временная метка
 * @property alpha альфа-активность
 * @property beta бета-активность
 * @property alphaGravity гравитационная составляющая альфа-ритма
 * @property betaGravity гравитационная составляющая бета-ритма
 * @property concentration уровень концентрации
 */
data class PhysiologicalBaseline(
    val time: Long = 0,
    val alpha: Float = 1f,
    val beta: Float = 1f,
    val alphaGravity: Float = 1f,
    val betaGravity: Float = 1f,
    val concentration: Float = 1f
)
/**
 * Состояние калиброаки Productivity
 * @property score состояние в процентах
 */
data class ProductivityScore(
    val score: Float = 0f
)


/**
 * Главный менеджер для управления устройством Capsule (нейро-гарнитурой)
 *
 * Этот класс предоставляет полный интерфейс для взаимодействия с нейро-гарнитурой
 * через нативную библиотеку. Он управляет подключением устройства, сбором данных
 * с различных сенсоров (ЭЭГ, ЧСС, MEMS), процессом калибровки и передачей данных
 * в базу данных и на удаленный сервер.
 *
 * ## Основные возможности:
 * - Подключение и поиск устройств по Bluetooth
 * - Получение данных в реальном времени: ЭЭГ, ЧСС, физиологические показатели
 * - Калибровка устройства под индивидуальные особенности пользователя
 * - Сбор и сохранение метрик в локальную базу данных
 * - Отправка данных на сервер мониторинга
 *
 * @property context Контекст приложения (инжектируется через Dagger Hilt)
 * @property metricsDao DAO для работы с базой данных метрик
 * @property uploadMonitor Репозиторий для отправки данных на сервер
 *
 * @author Neuro Project Team
 * @since 1.0
 * @see CapsuleStages
 * @see DeviceConnectionState
 */
@Singleton
class CapsuleDeviceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metricsDao: MetricsDao,
    private val uploadMonitor: MonitorUploadRepository
) {

    private var _instance = this

    init {
        _instance = this
        Log.d("CapsuleDeviceManager", "init")
    }

    /** CoroutineScope для асинхронных операций */
    var scope = CoroutineScope(EmptyCoroutineContext)

    /**
     * Callback для получения списка найденных устройств
     *
     * Вызывается при поиске устройств через [startSearch]
     */
    var devicesFound: (Array<DeviceInfo>) -> Unit = {}

    // Потоки данных (StateFlow)
    private var _connectionState = MutableStateFlow(DeviceConnectionState.disconnected)
    private var _calibrationState = MutableStateFlow(CapsuleStages.CALIBRATOR_UNKNOWN_STAGE)
    private var _hrData = MutableStateFlow(Cardiodata(0, 0f, false, 0f, false, false, false, 0f))
    private var _physiologicalData = MutableStateFlow(PhysiologicalData())
    private var _nfbData = MutableStateFlow(NFBData())
    private var _baseLineData = MutableStateFlow(BaselineValues(0f, 0f, 0f, 0f))
    private var _memsData = MutableStateFlow(MEMSdata(0, 0f, 0f, 0f, 0f, 0f, 0f))
    private var _productivityData = MutableStateFlow(Productivitydata(0, 0.0, 0f, 0f, 0f, 0f, 0f, 0f))
    private var _emotionalData = MutableStateFlow(Emotionaldata(0, 0f, 0f, 0f, 0f, 0f))
    private var _productivityIndexData = MutableStateFlow(ProductivityIndexes())
    private var _productivityBaselineData = MutableStateFlow(ProductivityBaseline())
    private var _physiologicalBaselineData = MutableStateFlow(PhysiologicalBaseline())
    private var _eegRawData = MutableStateFlow(EEGRawSample())
    private var _eegProcessedData = MutableStateFlow(EEGProcessedSample())
    private var _eegArtifacts = MutableStateFlow(EEGArtifactsSample())
    private var _productivityScore = MutableStateFlow(ProductivityScore())

    // Публичные Flow для подписки на данные
    /** Поток данных об этапах калибровки */
    var calibrationStage = _calibrationState.asStateFlow()
    /** Поток кардиологических данных (ЧСС и др.) */
    var hrData = _hrData.asStateFlow()
    /** Поток физиологических данных */
    var physiologicalData = _physiologicalData.asStateFlow()
    /** Поток физиологических базовых значений */
    var physiologicalBaselineData = _physiologicalBaselineData.asStateFlow()
    /** Поток данных MEMS-датчиков */
    var memsData = _memsData.asStateFlow()
    /** Поток данных продуктивности */
    var productivityData = _productivityData.asStateFlow()
    /** Поток эмоциональных показателей */
    var emotionalData = _emotionalData.asStateFlow()
    /** Поток данных нейрофидбека */
    var nfbData = _nfbData.asStateFlow()
    /** Поток базовых значений для нейрофидбека */
    var baseLineData = _baseLineData.asStateFlow()
    /** Поток индексов продуктивности */
    var productivityIndexData = _productivityIndexData.asStateFlow()
    /** Поток базовых значений продуктивности */
    var productivityBaselineData = _productivityBaselineData.asStateFlow()
    /** Поток сырых данных ЭЭГ */
    var eegRawData = _eegRawData.asStateFlow()
    /** Поток обработанных данных ЭЭГ */
    var eegProcessedData = _eegProcessedData.asStateFlow()
    /** Поток информации об артефактах ЭЭГ */
    var eegArtifacts = _eegArtifacts.asStateFlow()
    /** Поток состояния подключения устройства */
    var connectionState = _connectionState.asStateFlow()
    /** Поток состояния калибровки */
    var calibrationState = _calibrationState.asStateFlow()
    /** Поток состояния калибровки Productivity(в %) */
    var productivityScore = _productivityScore.asStateFlow()

    /**
     * Callback для получения уровня заряда батареи
     *
     * @param Int уровень заряда в процентах (0-100)
     */
    var batteryChanged: (Int) -> Unit = {}

    /**
     * Callback для получения данных сопротивления электродов
     *
     * @param o1 сопротивление на канале O1 (Ом)
     * @param o2 сопротивление на канале O2 (Ом)
     * @param t3 сопротивление на канале T3 (Ом)
     * @param t4 сопротивление на канале T4 (Ом)
     */
    var resistanceReceived: (o1: Double, o2: Double, t3: Double, t4: Double) -> Unit =
        { o1: Double, o2: Double, t3: Double, t4: Double -> }

    /**
     * Callback для получения данных нейрофидбека
     *
     * @param time временная метка
     * @param alpha альфа-ритм
     * @param beta бета-ритм
     * @param theta тета-ритм
     * @param delta дельта-ритм
     * @param smr сенсомоторный ритм
     */
    var nfbReceived: (time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) -> Unit =
        { time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float -> }

    /**
     * Callback для отслеживания прогресса калибровки
     *
     * @param stage текущий этап калибровки (0-4)
     */
    var stageCalibrationProgress: (stage: Int) -> Unit = {}

    /**
     * Callback для отслеживания состояния инициализации капсулы
     *
     * @param state состояние инициализации из [CapsuleInitializedState]
     */
    var initializeStateChanged: (state: CapsuleInitializedState) -> Unit = {}

    /**
     * Обработчик изменения состояния капсулы
     *
     * Вызывается из нативного кода при изменении статуса инициализации
     *
     * @param state числовой код состояния
     */
    fun onCapsuleStateChanged(state: Int) {
        initializeStateChanged(CapsuleInitializedState.entries[state])
    }

    /**
     * Инициализация капсулы
     *
     * Загружает нативную библиотеку и запускает процесс инициализации устройства.
     * Вызывается после получения разрешений на Bluetooth и перед поиском устройств.
     */
    fun initCapsule() {
        scope.launch {
            removeAll()
            delay(1000)
            thread {
                nativeInitCapsule(_instance)
                Log.d("CAPSULE", "end thread")
            }
        }
    }

    /**
     * Получение текущего состояния подключения
     *
     * @return текущее состояние подключения
     */
    fun getConnectionState(): DeviceConnectionState {
        return _connectionState.value
    }

    /**
     * Обработчик изменения этапа калибровки
     *
     * Вызывается из нативного кода при переходе между этапами калибровки
     *
     * @param stageNum числовой номер этапа
     */
    fun calibrationStateChanged(stageNum: Int) {
        val stage = CapsuleStages.fromInt(stageNum)
        Log.d("JCAPSULE", "StateChanged $stageNum")
        MainScope().launch {
            _calibrationState.emit(stage)
        }
    }

    /**
     * Начать поиск устройств
     *
     * Запускает сканирование Bluetooth-устройств в поисках нейро-гарнитуры.
     * Найденные устройства будут переданы через [devicesFound] callback.
     */
    fun startSearch() {
        nativeStartSearch()
    }

    /**
     * Подключиться к устройству
     *
     * @param id уникальный идентификатор (MAC-адрес) устройства
     */
    fun connect(id: String) {
        nativeConnect(id)
    }

    /**
     * Начать измерение сопротивления электродов
     *
     * Используется для проверки качества контакта электродов с кожей.
     * Результаты возвращаются через [resistanceReceived] callback.
     */
    fun startResistance() {
        nativeStartResistance()
    }

    fun importMainCalibration(indFrequency: Float, indPeakFrequency: Float, indPeakFrequencyPower: Float, indPeakFrequencySuppression: Float, indBandwidth: Float, indNormalizedPower: Float, lowerFrequency: Float, upperFrequency: Float){
        nativeImportCalibration(indFrequency, indPeakFrequency, indPeakFrequencyPower, indPeakFrequencySuppression, indBandwidth, indNormalizedPower, lowerFrequency, upperFrequency)
    }

    /**
     * Остановить измерение сопротивления электродов
     */
    fun stopResistance() {
        nativeStopResistance()
    }

    /**
     * Начать сбор сигналов и ЧСС
     *
     * Запускает потоковую передачу данных ЭЭГ и кардиодатчиков.
     * Перед вызовом рекомендуется провести калибровку.
     */
    fun startSignalAndHR() {
        MainScope().launch {
            _calibrationState.emit(CapsuleStages.CALIBRATOR_UNKNOWN_STAGE)
        }
        nativeStartSignalAndHR()
    }

    /**
     * Остановить сбор сигналов и ЧСС
     */
    fun stopSignalAndHR() {
        nativeStopSignalAndHR()
    }

    /**
     * Начать сессию сбора данных
     *
     * Запускает полный цикл сбора всех типов данных (ЭЭГ, ЧСС, MEMS, продуктивность)
     */
    fun startSession() {
        nativeStartSession()
    }

    /**
     * Остановить сессию сбора данных
     */
    fun stopSession() {
        nativeStopSession()
    }

    /**
     * Обработчик события обнаружения устройств
     *
     * Вызывается из нативного кода при обнаружении новых устройств
     *
     * @param devices массив найденных устройств
     */
    fun locatorEvent(devices: Array<DeviceInfo>) {
        devicesFound(devices)
        Log.d("JCAPSULE", "locatorEvent")
    }

    /**
     * Обработчик изменения состояния подключения
     *
     * Вызывается из нативного кода при изменении статуса соединения
     *
     * @param state числовой код состояния (соответствует [DeviceConnectionState])
     */
    fun deviceConnectionState(state: Int) {
        scope.launch {
            _connectionState.emit(DeviceConnectionState.entries[state])
        }
        Log.d("JCAPSULE", "deviceConnectionState")
    }

    /**
     * Обработчик получения индексов продуктивности
     *
     * Преобразует числовые значения в текстовые рекомендации и сохраняет данные
     *
     * @param time временная метка
     * @param relaxation уровень расслабления (-1 до 4)
     * @param stress уровень стресса (0-2)
     * @param gravityBaseline базовый уровень гравитации
     * @param productivityBaseline базовый уровень продуктивности
     * @param fatigueBaseline базовый уровень утомления
     * @param reverseFatiqueBaseline обратный базовый уровень утомления
     * @param relaxationBaseline базовый уровень расслабления
     * @param concentrationBaseline базовый уровень концентрации
     * @param hasArtifacts наличие артефактов
     */
    fun onProductivityIndexesReceived(
        time: Long, relaxation: Float, stress: Float,
        gravityBaseline: Float, productivityBaseline: Float,
        fatigueBaseline: Float, reverseFatiqueBaseline: Float,
        relaxationBaseline: Float, concentrationBaseline: Float,
        hasArtifacts: Boolean
    ) {
        Log.d("JCAPSULE", "OnProductivityIndexesReceived: stress $stress")
        var relaxation_string = "NoRecommendation"
        when (relaxation) {
            -1f -> relaxation_string = "NoRecommendation"
            0f -> relaxation_string = "Involvement"
            1f -> relaxation_string = "Relaxation"
            2f -> relaxation_string = "SlightFatigue"
            3f -> relaxation_string = "SevereFatigue"
            4f -> relaxation_string = "ChronicFatigue"
        }
        var stress_string = "NoStress"
        when (stress) {
            0f -> stress_string = "NoStress"
            1f -> stress_string = "Anxiety"
            2f -> stress_string = "Stress"
        }

        scope.launch {
            val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("saved_user_id", "")

            //uploadMonitor.sendStringSimple(userId, "concentrationBaseline $concentrationBaseline")

            _productivityIndexData.emit(
                ProductivityIndexes(
                    time, relaxation_string, stress_string,
                    gravityBaseline, productivityBaseline, fatigueBaseline,
                    reverseFatiqueBaseline, relaxationBaseline, concentrationBaseline,
                    hasArtifacts
                )
            )
        }
    }

    /**
     * Обработчик получения базовых значений продуктивности
     *
     * @param time временная метка
     * @param gravity гравитационная составляющая
     * @param productivity уровень продуктивности
     * @param fatigue уровень утомления
     * @param reverse_fatique обратный уровень утомления
     * @param relaxation уровень расслабления
     * @param concentration уровень концентрации
     */
    fun onProductivityBaselineReceived(
        time: Long, gravity: Float, productivity: Float,
        fatigue: Float, reverse_fatique: Float, relaxation: Float,
        concentration: Float
    ) {
        Log.d("JCAPSULE", "onProductivityBaseline: smth")
        scope.launch {
            val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("saved_user_id", "")

            //uploadMonitor.sendStringSimple(userId, "concentrationBaseline $concentration")

            _productivityBaselineData.emit(
                ProductivityBaseline(time, gravity, productivity, fatigue, reverse_fatique, relaxation, concentration)
            )
        }
    }

    /**
     * Обработчик получения физиологических базовых значений
     *
     * @param time временная метка
     * @param alpha альфа-активность
     * @param beta бета-активность
     * @param alphaGravity гравитационная составляющая альфа-ритма
     * @param betaGravity гравитационная составляющая бета-ритма
     * @param concentration уровень концентрации
     */
    fun onPhysiologicalBaselineReceived(
        time: Long, alpha: Float, beta: Float,
        alphaGravity: Float, betaGravity: Float, concentration: Float
    ) {
        Log.d("JCAPSULE", "onPhysiologicalReceived: smth")
        scope.launch {
            _physiologicalBaselineData.emit(
                PhysiologicalBaseline(time, alpha, beta, alphaGravity, betaGravity, concentration)
            )
        }
    }

    /**
     * Обработчик получения данных с MEMS-датчиков
     *
     * @param time временная метка
     * @param accx ускорение по оси X
     * @param accy ускорение по оси Y
     * @param accz ускорение по оси Z
     * @param hyrx угловая скорость по оси X
     * @param hyry угловая скорость по оси Y
     * @param hyrz угловая скорость по оси Z
     */
    fun onMEMSReceived(
        time: Long, accx: Float, accy: Float, accz: Float,
        hyrx: Float, hyry: Float, hyrz: Float
    ) {
        Log.d("JCAPSULE", "onMEMSReceived: smth")
        scope.launch {
            _memsData.emit(MEMSdata(time, accx, accy, accz, hyrx, hyry, hyrz))
        }
    }

    /**
     * Обработчик получения эмоциональных показателей
     *
     * @param time временная метка
     * @param attention уровень внимания
     * @param relaxation уровень расслабления
     * @param cognitive_load когнитивная нагрузка
     * @param cognitive_control когнитивный контроль
     * @param self_control самоконтроль
     */
    fun onEmotionReceived(
        time: Long, attention: Float, relaxation: Float,
        cognitive_load: Float, cognitive_control: Float, self_control: Float
    ) {
        Log.d("JCAPSULE", "onEmotionReceived: smth")
        scope.launch {
            val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("saved_user_id", "")

            //uploadMonitor.sendStringSimple(userId, "relax $relaxation")

            _emotionalData.emit(
                Emotionaldata(time, attention, relaxation, cognitive_load, cognitive_control, self_control)
            )
        }
    }

    /**
     * Обработчик получения данных продуктивности
     *
     * @param time временная метка
     * @param timestamp_prod альтернативная временная метка
     * @param gravity гравитационная составляющая
     * @param productivity уровень продуктивности
     * @param fatigue уровень утомления
     * @param reverse_fatique обратный уровень утомления
     * @param relaxation уровень расслабления
     * @param concentration уровень концентрации
     */
    fun onProductivityReceived(
        time: Long, timestamp_prod: Double, gravity: Float,
        productivity: Float, fatigue: Float, reverse_fatique: Float,
        relaxation: Float, concentration: Float
    ) {
        Log.d("JCAPSULE", "onProductivityReceived: fatigue $fatigue")
        scope.launch {
            val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("saved_user_id", "")

            //uploadMonitor.sendStringSimple(userId, "concentration $concentration")

            _productivityData.emit(
                Productivitydata(time, timestamp_prod, gravity, productivity, fatigue, reverse_fatique, relaxation, concentration)
            )
        }
    }

    /**
     * Обработчик получения данных сопротивления электродов
     *
     * Значения делятся на 10к для перевода в килоомы
     *
     * @param o1 сопротивление на канале O1
     * @param o2 сопротивление на канале O2
     * @param t3 сопротивление на канале T3
     * @param t4 сопротивление на канале T4
     */
    fun onResistanceReceived(o1: Double, o2: Double, t3: Double, t4: Double) {
        Log.d("JCAPSULE", "onResistanceReceived: smth")
        resistanceReceived(o1 / 10e3, o2 / 10e3, t3 / 10e3, t4 / 10e3)
    }

    /**
     * Обработчик получения данных калибровки
     *
     * Сохраняет результаты калибровки в локальную базу данных
     *
     * @param indFrequency индивидуальная частота
     * @param indPeakFrequency индивидуальная пиковая частота
     * @param indPeakFrequencyPower мощность индивидуальной пиковой частоты
     * @param indPeakFrequencySuppression подавление индивидуальной пиковой частоты
     * @param indBandwidth индивидуальная ширина полосы
     * @param indNormalizedPower нормализованная мощность
     * @param lowerFrequency нижняя граница частоты
     * @param upperFrequency верхняя граница частоты
     */
    fun onCalibrationReceived(
        indFrequency: Float, indPeakFrequency: Float, indPeakFrequencyPower: Float,
        indPeakFrequencySuppression: Float, indBandwidth: Float, indNormalizedPower: Float,
        lowerFrequency: Float, upperFrequency: Float
    ) {
        val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        scope.launch {
            try {
                val prob: CalibrationHistoryEntity = CalibrationHistoryEntity(
                    user_id = sharedPreferences.getString("saved_user_id", "").toString(),
                    individualFrequency = indFrequency,
                    individualPeakFrequency = indPeakFrequency,
                    individualPeakFrequencyPower = indPeakFrequencyPower,
                    individualPeakFrequencySuppression = indPeakFrequencySuppression,
                    individualBandwidth = indBandwidth,
                    individualNormalizedPower = indNormalizedPower,
                    lowerFrequency = lowerFrequency,
                    upperFrequency = upperFrequency
                )
                metricsDao.insertCalibrationData(prob)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving Calibration data", e)
            }
        }
    }

    /**
     * Обработчик получения данных о состоянии калибровки Productivity
     *
     * @param score состояние в процентах
     */
    fun onProductivityScore(score: Float) {
        Log.d("JCAPSULE", "Productivity score $score")
        scope.launch {
            _productivityScore.emit(ProductivityScore(score))
        }
    }



    /**
     * Обработчик прогресса калибровки ЭЭГ
     *
     * @param stage текущий этап калибровки
     */
    fun onEEGCalibrationReceived(stage: Int) {
        stageCalibrationProgress(stage)
    }

    /**
     * Обработчик получения базовых значений для нейрофидбека
     *
     * @param alpha альфа-активность
     * @param alphaGravity гравитационная составляющая альфа-ритма
     * @param beta бета-активность
     * @param betaGravity гравитационная составляющая бета-ритма
     */
    fun onBaselineReceived(alpha: Float, alphaGravity: Float, beta: Float, betaGravity: Float) {
        scope.launch {
            _baseLineData.emit(BaselineValues(alpha, alphaGravity, beta, betaGravity))
        }
        Log.d("JCAPSULE", "onCalibrationReceived")
    }

    /**
     * Обработчик получения данных нейрофидбека
     *
     * @param time временная метка
     * @param alpha альфа-ритм
     * @param beta бета-ритм
     * @param theta тета-ритм
     * @param delta дельта-ритм
     * @param smr сенсомоторный ритм
     */
    fun onNFBReceived(time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        scope.launch {
            _nfbData.emit(NFBData(time, alpha, beta, theta, delta, smr))
        }
        nfbReceived(time, alpha, beta, theta, delta, smr)
        Log.d("JCAPSULE", "alpha = $alpha, beta = $beta, theta = $theta")
    }

    /**
     * Обработчик получения кардиологических данных
     *
     * @param time временная метка
     * @param heartRate частота сердечных сокращений
     * @param hasArtifacts наличие артефактов
     * @param kaplanIndex индекс Каплана
     * @param metricsAvailable доступность метрик
     * @param motionArtifacts артефакты движения
     * @param skinContact качество контакта с кожей
     * @param stress уровень стресса
     */
    fun onCardioReceived(
        time: Long, heartRate: Float, hasArtifacts: Boolean, kaplanIndex: Float,
        metricsAvailable: Boolean, motionArtifacts: Boolean, skinContact: Boolean, stress: Float
    ) {
        scope.launch {
            _hrData.emit(
                Cardiodata(time, heartRate, hasArtifacts, kaplanIndex, metricsAvailable, motionArtifacts, skinContact, stress)
            )
        }
        Log.d("JCAPSULE", "HR = $heartRate")
    }

    /**
     * Обработчик получения физиологических данных
     *
     * @param time временная метка
     * @param relax уровень расслабления
     * @param fatigue уровень утомления
     * @param none нейтральное состояние
     * @param concentration уровень концентрации
     * @param involvement уровень вовлеченности
     * @param stress уровень стресса
     * @param nfbArtifacts наличие артефактов в нейрофидбеке
     * @param cardioArtifacts наличие артефактов в кардиосигнале
     */
    fun onPhysiologicalReceived(
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
        scope.launch {
            _physiologicalData.emit(
                PhysiologicalData(
                    time,
                    relax,
                    fatigue,
                    none,
                    concentration,
                    involvement,
                    stress,
                    nfbArtifacts,
                    cardioArtifacts
                )
            )
        }
        Log.d(
            "JCAPSULE", "r = $relax, f = $fatigue, n = $none, c = $concentration, i = $involvement, na = $nfbArtifacts, ca = $cardioArtifacts"
        )
    }

    /**
     * Обработчик получения сырых данных ЭЭГ
     *
     * @param timeStampMilli временная метка в миллисекундах
     * @param channel1 значение первого канала
     * @param channel2 значение второго канала
     */
    fun onEEGRawDataReceived(
        timeStampMilli: Long,
        channel1: Float,
        channel2: Float
    ) {
        Log.d("JCAPSULE", "onEEGRawDataReceived: time=$timeStampMilli")
        scope.launch {
            _eegRawData.emit(EEGRawSample(timeStampMilli, channel1, channel2))
        }
    }

    /**
     * Обработчик получения обработанных данных ЭЭГ
     *
     * @param timeStampMilli временная метка в миллисекундах
     * @param channel1 обработанное значение первого канала
     * @param channel2 обработанное значение второго канала
     */
    fun onEEGProcessedDataReceived(
        timeStampMilli: Long,
        channel1: Float,
        channel2: Float
    ) {
        Log.d("JCAPSULE", "onEEGProcessedDataReceived: time=$timeStampMilli")
        scope.launch {
            _eegProcessedData.emit(EEGProcessedSample(timeStampMilli, channel1, channel2))
        }
    }

    /**
     * Обработчик получения информации об артефактах ЭЭГ
     *
     * @param timeStampMilli временная метка в миллисекундах
     * @param artifacts1 наличие артефактов на первом канале
     * @param artifacts2 наличие артефактов на втором канале
     * @param quality1 качество сигнала первого канала (0-1)
     * @param quality2 качество сигнала второго канала (0-1)
     */
    fun onEEGArtifactsReceived(
        timeStampMilli: Long,
        artifacts1: Boolean,
        artifacts2: Boolean,
        quality1: Float,
        quality2: Float
    ) {
        Log.d("JCAPSULE", "onEEGArtifactsReceived: time=$timeStampMilli")
        scope.launch {
            _eegArtifacts.emit(
                EEGArtifactsSample(
                    timeStampMilli,
                    artifacts1, artifacts2,
                    quality1, quality2
                )
            )
        }
    }

    companion object {
        init {
            /** Загрузка нативной библиотеки */
            System.loadLibrary("native-lib")
        }

        // Нативные методы для взаимодействия с устройством

        /**
         * Инициализация капсулы с передачей экземпляра менеджера
         *
         * @param impl экземпляр [CapsuleDeviceManager] для обратных вызовов
         */
        external fun nativeInitCapsule(impl: CapsuleDeviceManager)

        /** Начать поиск устройств */
        external fun nativeStartSearch()

        /**
         * Подключиться к устройству
         * @param id MAC-адрес устройства
         */
        external fun nativeConnect(id: String)

        /** Начать измерение сопротивления электродов */
        external fun nativeStartResistance()

        /** Остановить измерение сопротивления электродов */
        external fun nativeStopResistance()

        /** Начать сбор сигналов и ЧСС */
        external fun nativeStartSignalAndHR()

        /** Остановить сбор сигналов и ЧСС */
        external fun nativeStopSignalAndHR()

        /** Начать сессию сбора данных */
        external fun nativeStartSession()

        /** Остановить сессию сбора данных */
        external fun nativeStopSession()

        /** Начать калибровку Productivity */
        external fun nativeStartProductivity()

        /**
         * Импорт параметров калибровки
         *
         * @param indFrequency индивидуальная частота
         * @param indPeakFrequency индивидуальная пиковая частота
         * @param indPeakFrequencyPower мощность индивидуальной пиковой частоты
         * @param indPeakFrequencySuppression подавление индивидуальной пиковой частоты
         * @param indBandwidth индивидуальная ширина полосы
         * @param indNormalizedPower нормализованная мощность
         * @param lowerFrequency нижняя граница частоты
         * @param upperFrequency верхняя граница частоты
         */
        external fun nativeImportCalibration(
            indFrequency: Float, indPeakFrequency: Float, indPeakFrequencyPower: Float,
            indPeakFrequencySuppression: Float, indBandwidth: Float, indNormalizedPower: Float,
            lowerFrequency: Float, upperFrequency: Float
        )

        external fun nativeImportProductivityCalibration(
            gravity: Float,
            b_productivity: Float,
            fatigue: Float,
            reverseFatigue: Float,
            relaxation: Float,
            concentration: Float
        )

        external fun nativeImportPhysiologicalCalibration(
            alpha: Float,
            beta: Float,
            alphaGravity: Float,
            betaGravity: Float,
            concentration: Float
        )

        /** Очистка всех ресурсов */
        external fun removeAll()
    }
}