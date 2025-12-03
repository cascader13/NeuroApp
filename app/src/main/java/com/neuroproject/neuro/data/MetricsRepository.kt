package com.neuroproject.neuro.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetricsRepository @Inject constructor(
    private val metricsDao: MetricsDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun saveNFBMetric(alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        scope.launch {
            try {
                val metric = NFBMetricEntity(
                    timestamp = System.currentTimeMillis(),
                    alpha = alpha,
                    beta = beta,
                    theta = theta,
                    delta = delta,
                    smr = smr
                )
                metricsDao.insertNFBMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving NFB metric", e)
            }
        }
    }

    fun savePhysiologicalMetric(
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
            try {
                val metric = PhysiologicalMetricEntity(
                    timestamp = System.currentTimeMillis(),
                    relax = relax,
                    fatigue = fatigue,
                    none = none,
                    concentration = concentration,
                    involvement = involvement,
                    stress = stress,
                    nfbArtifacts = nfbArtifacts,
                    cardioArtifacts = cardioArtifacts
                )
                metricsDao.insertPhysiologicalMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving physiological metric", e)
            }
        }
    }

    fun saveMEMSMetric(
        accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float
    ) {
        scope.launch {
            try {
                val metric = MEMSMetricEntity(
                    timestamp = System.currentTimeMillis(),
                    accelerometerX = accX,
                    accelerometerY = accY,
                    accelerometerZ = accZ,
                    gyroscopeX = gyroX,
                    gyroscopeY = gyroY,
                    gyroscopeZ = gyroZ
                )
                metricsDao.insertMEMSMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving MEMS metric", e)
            }
        }
    }

    fun saveProductivityMetric(
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    ) {
        scope.launch {
            try {
                val metric = ProductivityMetricEntity(
                    timestamp = System.currentTimeMillis(),
                    gravity = gravity,
                    productivity = productivity,
                    fatigue = fatigue,
                    reverseFatigue = reverseFatigue,
                    relaxation = relaxation,
                    concentration = concentration
                )
                metricsDao.insertProductivityMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving productivity metric", e)
            }
        }
    }

    fun saveEmotionalMetric(
        attention: Float,
        relaxation: Float,
        cognitiveLoad: Float,
        cognitiveControl: Float,
        selfControl: Float
    ) {
        scope.launch {
            try {
                val metric = EmotionalMetricEntity(
                    timestamp = System.currentTimeMillis(),
                    attention = attention,
                    relaxation = relaxation,
                    cognitiveLoad = cognitiveLoad,
                    cognitiveControl = cognitiveControl,
                    selfControl = selfControl
                )
                metricsDao.insertEmotionalMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving emotional metric", e)
            }
        }
    }

    fun saveCardioMetric(heartRate: Float) {
        scope.launch {
            try {
                val metric = CardioMetricEntity(
                    timestamp = System.currentTimeMillis(),
                    heartRate = heartRate
                )
                metricsDao.insertCardioMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving cardio metric", e)
            }
        }
    }

    fun clearAllMetrics() {
        scope.launch {
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
}