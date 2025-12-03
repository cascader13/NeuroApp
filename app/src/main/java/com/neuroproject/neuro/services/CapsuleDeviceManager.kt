package com.neuroproject.neuro.services

import android.util.Log
import com.neuroproject.neuro.models.BaselineValues
import com.neuroproject.neuro.models.CapsuleInitializedState
import com.neuroproject.neuro.models.DeviceConnectionState
import com.neuroproject.neuro.models.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

// здесь как раз работа с устройством на kotlin. В принципе данный код мало как будет изменяться. Надо только добавить enum состояний экземпляра класса и добавить методы с новыми метриками. Посмотрим
enum class CapsuleStages(val value: Int) {
    CALIBRATOR_UNKNOWN_STAGE(-2),
    CALIBRATOR_READY_STAGE(-1),
    CALIBRATOR_STAGE1(0),
    CALIBRATOR_STAGE2(1),
    CALIBRATOR_STAGE3(2),
    CALIBRATOR_STAGE4(3),
    PHYSIO_INIT_STAGE(4),
    PHYSIO_BASELINE_STAGE(5),
    PHYSIO_SAMPLES_STAGE(6);

    companion object {
        fun fromInt(value: Int) =
            CapsuleStages.entries.firstOrNull { it.value == value } ?: CALIBRATOR_UNKNOWN_STAGE
    }
}

enum class StageState(val value: Int) {
    STAGE_UNKNOWN(-1),
    STAGE_STARTED(0),
    STAGE_FINISHED(1);

    companion object {
        fun fromInt(value: Int) =
            StageState.entries.firstOrNull { it.value == value } ?: STAGE_UNKNOWN
    }
}

data class CalibrationStateInfo(val stage: CapsuleStages, val state: StageState)
data class PhysiologicalData(
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
    val alpha: Float = 0f,
    val beta: Float = 0f,
    val theta: Float = 0f,
    val delta: Float = 0f,
    val smr: Float = 0f
)

data class MEMSdata(
    val accelerometer_x: Float = 0f,
    val accelerometer_y: Float = 0f,
    val accelerometer_z: Float = 0f,
    val gyroscope_x: Float = 0f,
    val gyroscope_y: Float = 0f,
    val gyroscope_z: Float = 0f
)

data class Productivitydata(
    val timestamp_prod: Double = 0.0,
    val gravity: Float = 0f,
    val productivity: Float = 0f,
    val fatigue: Float = 0f,
    val reverse_fatique: Float = 0f,
    val relaxation: Float = 0f,
    val concentration: Float

)

data class Emotionaldata(
    val attention:Float = 0f,
    val relaxation:Float = 0f,
    val cognitive_load:Float = 0f,
    val cognitive_control:Float = 0f,
    val self_control: Float = 0f
)


@Singleton
class CapsuleDeviceManager @Inject constructor(){

    private var _instance = this

    init {
        _instance = this
        Log.d("CapsuleDeviceManager", "init")
    }

    var scope = CoroutineScope(EmptyCoroutineContext)

    var devicesFound: (Array<DeviceInfo>) -> Unit = { }


    private var _connectionState = MutableStateFlow(DeviceConnectionState.disconnected)
    private var _licenseState = MutableStateFlow(false)
    private var _calibrationState = MutableStateFlow(
        CalibrationStateInfo(
            CapsuleStages.CALIBRATOR_UNKNOWN_STAGE,
            StageState.STAGE_UNKNOWN
        )
    )
    private var _hrData = MutableStateFlow(0f)
    private var _physiologicalData = MutableStateFlow(PhysiologicalData())
    private var _nfbData = MutableStateFlow(NFBData())
    private var _baseLineData = MutableStateFlow(BaselineValues(0f, 0f, 0f, 0f))
    private var _memsData = MutableStateFlow(MEMSdata(0f, 0f, 0f, 0f, 0f, 0f))
    private var _productivityData = MutableStateFlow(Productivitydata(0.0, 0f, 0f, 0f, 0f, 0f , 0f))
    private var _emotionalData = MutableStateFlow(Emotionaldata(0f, 0f, 0f, 0f, 0f))

    var hrData = _hrData.asStateFlow()
    var physiologicalData = _physiologicalData.asStateFlow()
    var memsData = _memsData.asStateFlow()
    var productivityData = _productivityData.asStateFlow()
    var emotionalData = _emotionalData.asStateFlow()
    var nfbData = _nfbData.asStateFlow()
    var baseLineData = _baseLineData.asStateFlow()

    var connectionState = _connectionState.asStateFlow()
    var calibrationState = _calibrationState.asStateFlow()

    var batteryChanged: (Int) -> Unit = { }

    var resistanceReceived: (o1: Double, o2: Double, t3: Double, t4: Double) -> Unit =
        { o1: Double, o2: Double, t3: Double, t4: Double -> }

    var nfbReceived: (alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) -> Unit = { alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float ->}
    var stageCalibrationProgress: (stage: Int) -> Unit = { }


    var initializeStateChanged: (state: CapsuleInitializedState) -> Unit = {}


    fun onCapsuleStateChanged(state: Int){
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

    fun calibrationStateChanged(stageNum: Int, state: Int) {
        val stage = CapsuleStages.fromInt(stageNum)
        val type = StageState.fromInt(state)
        MainScope().launch {
            _calibrationState.emit(CalibrationStateInfo(stage, type))
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
                CalibrationStateInfo(
                    CapsuleStages.CALIBRATOR_UNKNOWN_STAGE,
                    StageState.STAGE_UNKNOWN
                )
            )
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


    fun onMEMSReceived(accx: Float, accy: Float, accz: Float, hyrx: Float, hyry: Float, hyrz: Float){
        Log.d("JCAPSULE", "onMEMSReceived: smth")
        scope.launch {
            _memsData.emit(MEMSdata(accx, accy, accz, hyrx, hyry, hyrz))
        }
    }

    fun onEmotionReceived(attention: Float, relaxation: Float, cognitive_load: Float, cognitive_control: Float, self_control: Float){
        Log.d("JCAPSULE", "onEmotionReceived: smth")
        scope.launch {
            _emotionalData.emit(Emotionaldata(attention, relaxation, cognitive_load, cognitive_control, self_control))
        }
    }

    fun onProductivityReceived(timestamp_prod: Double, gravity: Float, productivity: Float, fatigue: Float, reverse_fatique: Float, relaxation: Float, concentration: Float){
        Log.d("JCAPSULE", "onProductivityReceived: smth")
        scope.launch {
            _productivityData.emit(Productivitydata(timestamp_prod, gravity, productivity, fatigue, reverse_fatique, relaxation, concentration))
        }

    }

    fun onResistanceReceived(o1: Double, o2: Double, t3: Double, t4: Double) {
        Log.d("JCAPSULE", "onResistanceReceived: smth")
        resistanceReceived(o1 / 10e3, o2 / 10e3, t3 / 10e3, t4 / 10e3)
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

    fun onNFBReceived(alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        scope.launch {
            _nfbData.emit(NFBData(alpha, beta, theta, delta, smr))
        }
        nfbReceived(alpha, beta, theta, delta, smr)
        Log.d("JCAPSULE", "alpha = " + alpha + ", beta = " + beta + ", theta = " + theta)
    }

    fun onCardioReceived(artefacted: Boolean, hr: Float) {
        scope.launch {
            _hrData.emit(hr)
        }
        Log.d("JCAPSULE", "HR = " + hr)
    }

    fun onPhysiologicalReceived(
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
        external fun removeAll()
    }

}