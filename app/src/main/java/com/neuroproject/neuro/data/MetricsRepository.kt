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

    fun saveUsers(name: String, password: String, user_id: String){
        scope.launch {
            try {
                val sessionD = UsersEntity(
                    user_name = name,
                    user_password = password,
                    user_id = user_id
                )
                metricsDao.insertUsers(sessionD)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving Users", e)
            }
        }
    }

    fun saveNFBMetric(time: Long, id: String, date: java.sql.Timestamp,  alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        scope.launch {
            try {
                val metric = NFBMetricEntity(
                    timestamp = time,
                    id = id,
                    session = date,
                    alpha = alpha,
                    beta = beta,
                    theta = theta,
                    delta = delta,
                    smr = smr,
                    isMarked = false
                )
                metricsDao.insertNFBMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving NFB metric", e)
            }
        }
    }

    fun saveEEGRAWMetric(time: Long, id: String, date: java.sql.Timestamp,  channel1: Float, channel2: Float) {
        scope.launch {
            try {
                val metric = EEGRawMetricEntity(
                    timestamp = time,
                    id = id,
                    session = date,
                    channel1 = channel1,
                    channel2 = channel2,
                    isMarked = false
                )
                metricsDao.insertEEGRAWMetric(metric)
            }catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving EEG RAW metric", e)
            }
        }
    }

    fun saveEEGPROCEEDMetric(time: Long, id: String, date: java.sql.Timestamp, channel1: Float, channel2: Float) {
        scope.launch {
            try {
                val metric = EEGProceedMetricEntity(
                    timestamp = time,
                    id = id,
                    session = date,
                    channel1 = channel1,
                    channel2 = channel2,
                    isMarked = false
                )
                metricsDao.insertEEGPROCEEDMetric(metric)
            }catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving EEG PROCEED metric", e)
            }
        }
    }

    fun saveEEGArtifactMetric(time: Long, id: String, date: java.sql.Timestamp, artifactsChannel1: Boolean, artifactsChannel2: Boolean, qualityChannel1: Float, qualityChannel2: Float,) {
        scope.launch {
            try {
                val metric = EEGArtifactsMetricEntity(
                    timestamp = time,
                    id = id,
                    session = date,
                    artifactsChannel1 = artifactsChannel1,
                    artifactsChannel2 = artifactsChannel2,
                    qualityChannel1 = qualityChannel1,
                    qualityChannel2 = qualityChannel2,
                    isMarked = false
                )
                metricsDao.insertEEGArtifactsMetric(metric)
            }catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving EEG Artifact metric", e)
            }
        }
    }

    fun savePhysiologicalMetric(
        time: Long,
        id: String,
        date: java.sql.Timestamp,
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
                    timestamp = time,
                    id = id,
                    session = date,
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
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving physiological metric", e)
            }
        }
    }

    fun saveMEMSMetric(
        time: Long, id: String, date: java.sql.Timestamp, accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float
    ) {
        scope.launch {
            try {
                val metric = MEMSMetricEntity(
                    timestamp = time,
                    id = id,
                    session = date,
                    accelerometerX = accX,
                    accelerometerY = accY,
                    accelerometerZ = accZ,
                    gyroscopeX = gyroX,
                    gyroscopeY = gyroY,
                    gyroscopeZ = gyroZ,
                    isMarked = false
                )
                metricsDao.insertMEMSMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving MEMS metric", e)
            }
        }
    }

    fun saveProductivityMetric(
        time: Long,
        id: String,
        date: java.sql.Timestamp,
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
                    timestamp = time,
                    id = id,
                    session = date,
                    gravity = gravity,
                    productivity = productivity,
                    fatigue = fatigue,
                    reverseFatigue = reverseFatigue,
                    relaxation = relaxation,
                    concentration = concentration,
                    isMarked = false
                )
                metricsDao.insertProductivityMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving productivity metric", e)
            }
        }
    }

    fun saveEmotionalMetric(
        time: Long,
        id: String,
        date: java.sql.Timestamp,
        attention: Float,
        relaxation: Float,
        cognitiveLoad: Float,
        cognitiveControl: Float,
        selfControl: Float
    ) {
        scope.launch {
            try {
                val metric = EmotionalMetricEntity(
                    timestamp = time,
                    id = id,
                    session = date,
                    attention = attention,
                    relaxation = relaxation,
                    cognitiveLoad = cognitiveLoad,
                    cognitiveControl = cognitiveControl,
                    selfControl = selfControl,
                    isMarked = false
                )
                metricsDao.insertEmotionalMetric(metric)
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving emotional metric", e)
            }
        }
    }

    fun saveCardioMetric(time: Long, id: String, date: java.sql.Timestamp, heartRate: Float, hasArtifacts:Boolean, kaplanIndex: Float, metricsAvailable: Boolean, motionAtrifacts: Boolean, skinContact: Boolean, stressIndex: Float) {
        scope.launch {
            try {
                val metric = CardioMetricEntity(
                    timestamp = time,
                    id = id,
                    session = date,
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
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving cardio metric", e)
            }
        }
    }

    fun clearAllMetrics() {
        scope.launch {
            try {
                metricsDao.clearUsers()
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