package com.neuroproject.neuro.presentation.screens.analysis

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.model.NFBSample
import com.neuroproject.neuro.domain.repository.DeviceGateway
import com.neuroproject.neuro.domain.repository.SensorStreamGateway
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel экрана анализа.
 *
 * Экран больше не зависит от JNI-менеджера напрямую: данные приходят через
 * SensorStreamGateway, а состояние подключения — через DeviceGateway. Это сохраняет
 * direction of dependencies: presentation -> domain contracts -> data adapters.
 */
@HiltViewModel
class AnalysisScreenViewModel @Inject constructor(
    private val sensorStreamGateway: SensorStreamGateway,
    private val deviceGateway: DeviceGateway
) : ViewModel() {

    private val _nfb = MutableStateFlow(NFBSample())
    val nfb: StateFlow<NFBSample> = _nfb.asStateFlow()

    private val _plotData = MutableStateFlow(PlotData())
    val plotData: StateFlow<PlotData> = _plotData.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _connectionState = MutableStateFlow(DeviceConnectionState.disconnected)
    val connectionState: StateFlow<DeviceConnectionState> = _connectionState.asStateFlow()

    private var timeCounter = 0f
    private val timeStep = 0.1f
    private val maxPoints = 200

    init {
        observeConnectionState()
        observeNfbStream()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            deviceGateway.observeConnectionState().collect { state ->
                _connectionState.value = state
            }
        }
    }

    private fun observeNfbStream() {
        viewModelScope.launch {
            sensorStreamGateway.observeNFB().collect { sample ->
                _nfb.value = sample
                appendSampleToPlot(sample)
            }
        }
    }

    private fun appendSampleToPlot(sample: NFBSample) {
        val alphaPoint = DataPoint(timeCounter, sample.alpha)
        val betaPoint = DataPoint(timeCounter, sample.beta)
        val deltaPoint = DataPoint(timeCounter, sample.delta)

        _plotData.value = _plotData.value.copy(
            alphaPoints = (_plotData.value.alphaPoints + alphaPoint).takeLast(maxPoints),
            betaPoints = (_plotData.value.betaPoints + betaPoint).takeLast(maxPoints),
            deltaPoints = (_plotData.value.deltaPoints + deltaPoint).takeLast(maxPoints)
        )
        timeCounter += timeStep
    }

    fun startRecording() {
        _isRecording.value = true
        Log.d("AnalysisScreen", "Recording started")
    }

    fun stopRecording() {
        _isRecording.value = false
        Log.d("AnalysisScreen", "Recording stopped")
    }

    fun clearPlotData() {
        _plotData.value = PlotData()
        timeCounter = 0f
    }
}

data class DataPoint(
    val x: Float,
    val y: Float
)

data class PlotData(
    val alphaPoints: List<DataPoint> = emptyList(),
    val betaPoints: List<DataPoint> = emptyList(),
    val deltaPoints: List<DataPoint> = emptyList()
)
