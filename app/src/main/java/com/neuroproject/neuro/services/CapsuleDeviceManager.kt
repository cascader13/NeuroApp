package com.neuroproject.neuro.services

import android.content.Context
import android.util.Log
import com.neuroproject.neuro.data.CalibrationHistoryEntity
import com.neuroproject.neuro.data.MetricsDao
import com.neuroproject.neuro.models.BaselineValues
import com.neuroproject.neuro.models.CapsuleInitializedState
import com.neuroproject.neuro.models.DeviceConnectionState
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

enum class CapsuleStages(val value: Int) {
    CALIBRATOR_UNKNOWN_STAGE(-2),
    CALIBRATOR_READY_STAGE(-1),
    CALIBRATOR_STAGE1(0),
    CALIBRATOR_STAGE2(1),
    CALIBRATOR_STAGE3(2),
    CALIBRATOR_STAGE4(3),
    PHYSIO_INIT_STAGE(4),
    PHYSIO_BASELINE_STAGE(5),
    CALIBRATOR_ERROR_STAGE(6);

    companion object {
        fun fromInt(value: Int) =
            CapsuleStages.entries.firstOrNull { it.value == value } ?: CALIBRATOR_UNKNOWN_STAGE
    }
}
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

data class NFBData(
    val timeStampMilli: Long = 0,
    val alpha: Float = 0f,
    val beta: Float = 0f,
    val theta: Float = 0f,
    val delta: Float = 0f,
    val smr: Float = 0f
)

data class MEMSdata(
    val timeStampMilli: Long = 0,
    val accelerometer_x: Float = 0f,
    val accelerometer_y: Float = 0f,
    val accelerometer_z: Float = 0f,
    val gyroscope_x: Float = 0f,
    val gyroscope_y: Float = 0f,
    val gyroscope_z: Float = 0f
)

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

data class Emotionaldata(
    val timeStampMilli: Long = 0,
    val attention: Float = 0f,
    val relaxation: Float = 0f,
    val cognitive_load: Float = 0f,
    val cognitive_control: Float = 0f,
    val self_control: Float = 0f
)

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


data class EEGRawSample(
    val timeStampMilli: Long = 0,
    val channel1: Float = 0f,
    val channel2: Float = 0f
)

data class EEGProcessedSample(
    val timeStampMilli: Long = 0,
    val channel1: Float = 0f,
    val channel2: Float = 0f
)

data class EEGArtifactsSample(
    val timeStampMilli: Long = 0,
    val artifactsChannel1: Boolean = false,
    val artifactsChannel2: Boolean = false,
    val qualityChannel1: Float = 0f,
    val qualityChannel2: Float = 0f
)

@Singleton
class CapsuleDeviceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metricsDao: MetricsDao
) {

    private var _instance = this

    init {
        _instance = this
        Log.d("CapsuleDeviceManager", "init")
    }

    var scope = CoroutineScope(EmptyCoroutineContext)

    var devicesFound: (Array<DeviceInfo>) -> Unit = {}

    private var _connectionState = MutableStateFlow(DeviceConnectionState.disconnected)
    private var _calibrationState = MutableStateFlow(
        CapsuleStages.CALIBRATOR_UNKNOWN_STAGE
        )
    private var _hrData = MutableStateFlow(Cardiodata(0, 0f, false, 0f, false, false, false, 0f))
    private var _physiologicalData = MutableStateFlow(PhysiologicalData())
    private var _nfbData = MutableStateFlow(NFBData())
    private var _baseLineData = MutableStateFlow(BaselineValues(0f, 0f, 0f, 0f))
    private var _memsData = MutableStateFlow(MEMSdata(0, 0f, 0f, 0f, 0f, 0f, 0f))
    private var _productivityData = MutableStateFlow(Productivitydata(0, 0.0, 0f, 0f, 0f, 0f, 0f, 0f))
    private var _emotionalData = MutableStateFlow(Emotionaldata(0, 0f, 0f, 0f, 0f, 0f))

    // НОВЫЕ ПОТОКИ ДЛЯ EEG ДАННЫХ
    private var _eegRawData = MutableStateFlow(EEGRawSample())
    private var _eegProcessedData = MutableStateFlow(EEGProcessedSample())
    private var _eegArtifacts = MutableStateFlow(EEGArtifactsSample())

    var calibrationStage = _calibrationState.asStateFlow()
    var hrData = _hrData.asStateFlow()
    var physiologicalData = _physiologicalData.asStateFlow()
    var memsData = _memsData.asStateFlow()
    var productivityData = _productivityData.asStateFlow()
    var emotionalData = _emotionalData.asStateFlow()
    var nfbData = _nfbData.asStateFlow()
    var baseLineData = _baseLineData.asStateFlow()

    var eegRawData = _eegRawData.asStateFlow()
    var eegProcessedData = _eegProcessedData.asStateFlow()
    var eegArtifacts = _eegArtifacts.asStateFlow()

    var connectionState = _connectionState.asStateFlow()
    var calibrationState = _calibrationState.asStateFlow()

    var batteryChanged: (Int) -> Unit = {}

    var resistanceReceived: (o1: Double, o2: Double, t3: Double, t4: Double) -> Unit =
        { o1: Double, o2: Double, t3: Double, t4: Double -> }

    var nfbReceived: (time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) -> Unit =
        { time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float -> }

    var stageCalibrationProgress: (stage: Int) -> Unit = {}
    var initializeStateChanged: (state: CapsuleInitializedState) -> Unit = {}

    fun onCapsuleStateChanged(state: Int) {
        initializeStateChanged(CapsuleInitializedState.entries[state])
    }

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

    fun calibrationStateChanged(stageNum: Int) {
        val stage = CapsuleStages.fromInt(stageNum)
        Log.d("JCAPSULE", "StateChanged $stageNum")
        MainScope().launch {
            _calibrationState.emit(stage)
        }
    }

    fun startSearch() {
        nativeStartSearch()
    }

    fun connect(id: String) {
        nativeConnect(id)
    }

    fun startResistance() {
        nativeStartResistance()
    }

    fun stopResistance() {
        nativeStopResistance()
    }

    fun startSignalAndHR() {
        MainScope().launch {
            _calibrationState.emit(
                    CapsuleStages.CALIBRATOR_UNKNOWN_STAGE)
        }
        nativeStartSignalAndHR()
    }

    fun stopSignalAndHR() {
        nativeStopSignalAndHR()
    }

    fun startSession() {
        nativeStartSession()
    }

    fun stopSession() {
        nativeStopSession()
    }

    fun locatorEvent(devices: Array<DeviceInfo>) {
        devicesFound(devices)
        Log.d("JCAPSULE", "locatorEvent")
    }

    fun deviceConnectionState(state: Int) {
        scope.launch {
            _connectionState.emit(DeviceConnectionState.entries[state])
        }
        Log.d("JCAPSULE", "deviceConnectionState")
    }

    fun onMEMSReceived(time: Long, accx: Float, accy: Float, accz: Float, hyrx: Float, hyry: Float, hyrz: Float) {
        Log.d("JCAPSULE", "onMEMSReceived: smth")
        scope.launch {
            _memsData.emit(MEMSdata(time, accx, accy, accz, hyrx, hyry, hyrz))
        }
    }

    fun onEmotionReceived(time: Long, attention: Float, relaxation: Float, cognitive_load: Float, cognitive_control: Float, self_control: Float) {
        Log.d("JCAPSULE", "onEmotionReceived: smth")
        scope.launch {
            _emotionalData.emit(Emotionaldata(time, attention, relaxation, cognitive_load, cognitive_control, self_control))
        }
    }

    fun onProductivityReceived(time: Long, timestamp_prod: Double, gravity: Float, productivity: Float, fatigue: Float, reverse_fatique: Float, relaxation: Float, concentration: Float) {
        Log.d("JCAPSULE", "onProductivityReceived: smth")
        scope.launch {
            _productivityData.emit(Productivitydata(time, timestamp_prod, gravity, productivity, fatigue, reverse_fatique, relaxation, concentration))
        }
    }

    fun onResistanceReceived(o1: Double, o2: Double, t3: Double, t4: Double) {
        Log.d("JCAPSULE", "onResistanceReceived: smth")
        resistanceReceived(o1 / 10e3, o2 / 10e3, t3 / 10e3, t4 / 10e3)
    }


    fun onCalibrationReceived(indFrequency: Float, indPeakFrequency: Float, indPeakFrequencyPower: Float, indPeakFrequencySuppression : Float, indBandwidth : Float,indNormalizedPower: Float, lowerFrequency: Float, upperFrequency: Float){
        val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        scope.launch {
            try {
                val prob: CalibrationHistoryEntity = CalibrationHistoryEntity(user_name = sharedPreferences.getString("saved_username", "").toString(),
                    individualFrequency = indFrequency,
                    individualPeakFrequency = indPeakFrequency,
                    individualPeakFrequencyPower = indPeakFrequencyPower,
                    individualPeakFrequencySuppression = indPeakFrequencySuppression,
                    individualBandwidth = indBandwidth,
                    individualNormalizedPower = indNormalizedPower,
                    lowerFrequency = lowerFrequency,
                    upperFrequency = upperFrequency)
                metricsDao.insertCalibrationData(prob)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving Calibration data", e)
            }
        }

    }

    fun onEEGCalibrationReceived(stage: Int) {
        stageCalibrationProgress(stage)
    }

    fun onBaselineReceived(alpha: Float, alphaGravity: Float, beta: Float, betaGravity: Float) {
        scope.launch {
            _baseLineData.emit(BaselineValues(alpha, alphaGravity, beta, betaGravity))
        }
        Log.d("JCAPSULE", "onCalibrationReceived")
    }

    fun onNFBReceived(time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        scope.launch {
            _nfbData.emit(NFBData(time, alpha, beta, theta, delta, smr))
        }
        nfbReceived(time, alpha, beta, theta, delta, smr)
        Log.d("JCAPSULE", "alpha = " + alpha + ", beta = " + beta + ", theta = " + theta)
    }

    fun onCardioReceived(time: Long, heartRate: Float, hasArtifacts: Boolean, kaplanIndex: Float, metricsAvailable: Boolean, motionArtifacts: Boolean, skinContact: Boolean, stress: Float) {
        scope.launch {
            _hrData.emit(Cardiodata(time, heartRate, hasArtifacts, kaplanIndex, metricsAvailable, motionArtifacts, skinContact, stress))
        }
        Log.d("JCAPSULE", "HR = " + heartRate)
    }

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
            "JCAPSULE", "r = " + relax +
                    ", f = " + fatigue + ", n = " + none
                    + ", c = " + concentration + ", i = " + involvement + ", na = " + nfbArtifacts + ", ca = " + cardioArtifacts
        )
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ EEG ДАННЫХ
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
            System.loadLibrary("native-lib")
        }

        external fun nativeInitCapsule(impl: CapsuleDeviceManager)
        external fun nativeStartSearch()
        external fun nativeConnect(id: String)
        external fun nativeStartResistance()
        external fun nativeStopResistance()
        external fun nativeStartSignalAndHR()
        external fun nativeStopSignalAndHR()
        external fun nativeStartSession()
        external fun nativeStopSession()
        external fun nativeImportCalibration(indFrequency: Float, indPeakFrequency: Float, indPeakFrequencyPower: Float, indPeakFrequencySuppression : Float, indBandwidth : Float,indNormalizedPower: Float, lowerFrequency: Float, upperFrequency: Float)
        external fun removeAll()
    }
}