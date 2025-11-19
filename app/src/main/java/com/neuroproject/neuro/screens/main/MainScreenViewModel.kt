package com.neuroproject.neuro.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import com.neuroproject.neuro.screens.sensorchecking.ResistStateRecord
import com.neuroproject.neuro.services.CapsuleDeviceManager
import com.neuroproject.neuro.services.NFBData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

// Модель для точки данных графика
data class DataPoint(
    val x: Float, // время
    val y: Float  // значение
)

// Модель для хранения данных всех графиков
data class PlotData(
    val alphaPoints: List<DataPoint>,
    val betaPoints: List<DataPoint>,
    val deltaPoints: List<DataPoint>
)

@HiltViewModel
class MainScreenViewModel @Inject constructor(dm: CapsuleDeviceManager) : ViewModel() {
    private val capsuleDM = dm
    private val _scope = CoroutineScope(EmptyCoroutineContext)
    private val _nfbState = MutableStateFlow(NFBData())
    val nfb = _nfbState.asStateFlow()

    private val _plotData = MutableStateFlow(
        PlotData(
            alphaPoints = emptyList(),
            betaPoints = emptyList(),
            deltaPoints = emptyList()
        )
    )
    val plotData = _plotData.asStateFlow()

    private var timeCounter = 0f
    private val timeStep = 0.1f
    private val maxPoints = 200 // Максимальное количество точек на графике

    init {
        capsuleDM.nfbReceived = { alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float ->
            _scope.launch {
                // Обновляем NFB данные
                _nfbState.emit(NFBData(alpha, beta, theta, delta, smr))

                // Добавляем точки на графики
                val newAlphaPoint = DataPoint(timeCounter, alpha)
                val newBetaPoint = DataPoint(timeCounter, beta)
                val newDeltaPoint = DataPoint(timeCounter, delta)

                updatePlotData { current ->
                    current.copy(
                        alphaPoints = (current.alphaPoints + newAlphaPoint).takeLast(maxPoints),
                        betaPoints = (current.betaPoints + newBetaPoint).takeLast(maxPoints),
                        deltaPoints = (current.deltaPoints + newDeltaPoint).takeLast(maxPoints)
                    )
                }

                timeCounter += timeStep
            }
        }
    }

    // Очистка данных графиков
    fun clearPlotData() {
        _scope.launch {
            updatePlotData {
                PlotData(emptyList(), emptyList(), emptyList())
            }
            timeCounter = 0f
        }
    }

    private fun updatePlotData(transform: (PlotData) -> PlotData) {
        _plotData.value = transform(_plotData.value)
    }
}