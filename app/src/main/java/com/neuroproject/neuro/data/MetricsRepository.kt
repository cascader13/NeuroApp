package com.neuroproject.neuro.data

import android.util.Log
import androidx.compose.material3.Card
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
import kotlin.math.exp

@Singleton
class MetricsRepository @Inject constructor(
    private val metricsDao: MetricsDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private data class NfbBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<NFBMetricEntity> = mutableListOf()
    )

    private data class EEGRAWBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<EEGRawMetricEntity> = mutableListOf()
    )

    private data class EEGPROCEEDBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<EEGProceedMetricEntity> = mutableListOf()
    )

    private data class EEGArtifactBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<EEGArtifactsMetricEntity> = mutableListOf()
    )

    private data class PhysiologicalBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<PhysiologicalMetricEntity> = mutableListOf()
    )

    private data class EmotionalBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<EmotionalMetricEntity> = mutableListOf()
    )

    private data class ProductivityBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<ProductivityMetricEntity> = mutableListOf()
    )

    private data class CardioBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<CardioMetricEntity> = mutableListOf()
    )

    private data class MEMSBuffer(
        var firstTimestamp: Long? = null,
        val values: MutableList<MEMSMetricEntity> = mutableListOf()
    )

    private val nfbBuffer = NfbBuffer()
    private val EEGRAWBuffer = EEGRAWBuffer()
    private val EEGPROCEEDBuffer = EEGPROCEEDBuffer()
    private val EEGArtifactBuffer = EEGArtifactBuffer()
    private val PhysiologicalBuffer = PhysiologicalBuffer()
    private val ProductivityBuffer = ProductivityBuffer()
    private val EmotionalBuffer = EmotionalBuffer()
    private val CardioBuffer = CardioBuffer()
    private val MEMSBuffer = MEMSBuffer()


    private val mutex = Mutex()












    fun saveNFBMetric(time: Long, id: String, exp_id: String,  date: java.sql.Timestamp,  alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float) {
        scope.launch {
            try {
                val metric = NFBMetricEntity(
                    timestamp = time,
                    id = id,
                    exp_id,
                    session = date,
                    alpha = alpha,
                    beta = beta,
                    theta = theta,
                    delta = delta,
                    smr = smr,
                    isMarked = false
                )
                metricsDao.insertNFBMetric(metric)
                mutex.withLock {
                    if(nfbBuffer.firstTimestamp == null){
                        nfbBuffer.firstTimestamp = time
                    }
                    nfbBuffer.values.add(metric)

                    if(time - nfbBuffer.firstTimestamp!! >= 10_000) {
                        flushNfbBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving NFB metric", e)
            }
        }
    }

    fun saveEEGRAWMetric(time: Long, id: String,  exp_id: String, date: java.sql.Timestamp,  channel1: Float, channel2: Float) {
        scope.launch {
            try {
                val metric = EEGRawMetricEntity(
                    timestamp = time,
                    id = id,
                    expedition_id = exp_id,
                    session = date,
                    channel1 = channel1,
                    channel2 = channel2,
                    isMarked = false
                )
                metricsDao.insertEEGRAWMetric(metric)
                mutex.withLock {
                    if(EEGRAWBuffer.firstTimestamp == null){
                        EEGRAWBuffer.firstTimestamp = time
                    }
                    EEGRAWBuffer.values.add(metric)
                    if(time - EEGRAWBuffer.firstTimestamp!! >= 10_000){
                        flushEEGRAWBuffer()
                    }
                }
            }catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving EEG RAW metric", e)
            }
        }
    }

    fun saveEEGPROCEEDMetric(time: Long, id: String,  exp_id: String, date: java.sql.Timestamp, channel1: Float, channel2: Float) {
        scope.launch {
            try {
                val metric = EEGProceedMetricEntity(
                    timestamp = time,
                    id = id,
                    expedition_id = exp_id,
                    session = date,
                    channel1 = channel1,
                    channel2 = channel2,
                    isMarked = false
                )
                metricsDao.insertEEGPROCEEDMetric(metric)
                mutex.withLock {
                    if(EEGPROCEEDBuffer.firstTimestamp == null){
                        EEGPROCEEDBuffer.firstTimestamp = time
                    }
                    EEGPROCEEDBuffer.values.add(metric)

                    if(time - EEGPROCEEDBuffer.firstTimestamp!! >= 10_000){
                        flushEEGPROCEEDBuffer()
                    }
                }
            }catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving EEG PROCEED metric", e)
            }
        }
    }

    fun saveEEGArtifactMetric(time: Long, id: String,  exp_id: String, date: java.sql.Timestamp, artifactsChannel1: Boolean, artifactsChannel2: Boolean, qualityChannel1: Float, qualityChannel2: Float,) {
        scope.launch {
            try {
                val metric = EEGArtifactsMetricEntity(
                    timestamp = time,
                    id = id,
                    expedition_id = exp_id,
                    session = date,
                    artifactsChannel1 = artifactsChannel1,
                    artifactsChannel2 = artifactsChannel2,
                    qualityChannel1 = qualityChannel1,
                    qualityChannel2 = qualityChannel2,
                    isMarked = false
                )
                metricsDao.insertEEGArtifactsMetric(metric)
                mutex.withLock {
                    if(EEGArtifactBuffer.firstTimestamp == null){
                        EEGArtifactBuffer.firstTimestamp = time
                    }
                    EEGArtifactBuffer.values.add(metric)
                    if(time - EEGArtifactBuffer.firstTimestamp!! >= 10_000){
                        flushEEGArtifactBuffer()
                    }
                }
            }catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving EEG Artifact metric", e)
            }
        }
    }

    fun savePhysiologicalMetric(
        time: Long,
        id: String,
        exp_id: String,
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
                    expedition_id = exp_id,
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
                mutex.withLock {
                    if(PhysiologicalBuffer.firstTimestamp == null){
                        PhysiologicalBuffer.firstTimestamp = time
                    }
                    PhysiologicalBuffer.values.add(metric)
                    if(time - PhysiologicalBuffer.firstTimestamp!! >= 10_000){
                        flushPhysiologicalBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving physiological metric", e)
            }
        }
    }

    fun saveMEMSMetric(
        time: Long, id: String,  exp_id: String, date: java.sql.Timestamp, accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float
    ) {
        scope.launch {
            try {
                val metric = MEMSMetricEntity(
                    timestamp = time,
                    id = id,
                    expedition_id = exp_id,
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
                mutex.withLock {
                    if(MEMSBuffer.firstTimestamp == null){
                        MEMSBuffer.firstTimestamp = time
                    }
                    MEMSBuffer.values.add(metric)
                    if(time - MEMSBuffer.firstTimestamp!! >= 10_000){
                        flushMEMSBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving MEMS metric", e)
            }
        }
    }

    fun saveProductivityMetric(
        time: Long,
        id: String,
        exp_id: String,
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
                    expedition_id = exp_id,
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
                mutex.withLock {
                    if(ProductivityBuffer.firstTimestamp == null){
                        ProductivityBuffer.firstTimestamp = time
                    }

                    ProductivityBuffer.values.add(metric)

                    if(time - ProductivityBuffer.firstTimestamp!! >= 10_000){
                        flushProductivityBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving productivity metric", e)
            }
        }
    }

    fun saveEmotionalMetric(
        time: Long,
        id: String,
        exp_id: String,
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
                    expedition_id = exp_id,
                    session = date,
                    attention = attention,
                    relaxation = relaxation,
                    cognitiveLoad = cognitiveLoad,
                    cognitiveControl = cognitiveControl,
                    selfControl = selfControl,
                    isMarked = false
                )
                metricsDao.insertEmotionalMetric(metric)
                mutex.withLock {
                    if(EmotionalBuffer.firstTimestamp == null){
                        EmotionalBuffer.firstTimestamp = time
                    }

                    EmotionalBuffer.values.add(metric)

                    if(time - EmotionalBuffer.firstTimestamp!! >= 10_000){
                        flushEmotionalBuffer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MetricsRepository", "Error saving emotional metric", e)
            }
        }
    }

    fun saveCardioMetric(time: Long, id: String,  exp_id: String, date: java.sql.Timestamp, heartRate: Float, hasArtifacts:Boolean, kaplanIndex: Float, metricsAvailable: Boolean, motionAtrifacts: Boolean, skinContact: Boolean, stressIndex: Float) {
        scope.launch {
            try {
                val metric = CardioMetricEntity(
                    timestamp = time,
                    id = id,
                    expedition_id = exp_id,
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
                mutex.withLock {
                    if(CardioBuffer.firstTimestamp == null){
                        CardioBuffer.firstTimestamp = time
                    }

                    CardioBuffer.values.add(metric)

                    if(time - CardioBuffer.firstTimestamp!! >= 10_000){
                        flushCardioBuffer()
                    }
                }
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

    private suspend fun flushNfbBuffer() {
        if (nfbBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = NFBMetricCompressedEntity(
            timestamp = nfbBuffer.firstTimestamp!!, // начало минутного интервала
            id = nfbBuffer.values.first().id,
            expedition_id = nfbBuffer.values.first().expedition_id,
            session = nfbBuffer.values.first().session,
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

    private suspend fun flushEEGRAWBuffer() {
        if (EEGRAWBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = EEGRawMetricCompressedEntity(
            timestamp = EEGRAWBuffer.firstTimestamp!!, // начало минутного интервала
            id = EEGRAWBuffer.values.first().id,
            expedition_id = EEGRAWBuffer.values.first().expedition_id,
            session = EEGRAWBuffer.values.first().session,
            channel1 = EEGRAWBuffer.values.map { it.channel1 }.median(),
            channel2 = EEGRAWBuffer.values.map {it.channel2}.median(),
            isMarked = false
        )
        metricsDao.insertEEGRAWCompressedMetric(compressed)

        // Очищаем буфер
        EEGRAWBuffer.values.clear()
        EEGRAWBuffer.firstTimestamp = null
    }

    private suspend fun flushEEGPROCEEDBuffer() {
        if (EEGPROCEEDBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = EEGProceedMetricCompressedEntity(
            timestamp = EEGPROCEEDBuffer.firstTimestamp!!, // начало минутного интервала
            id = EEGPROCEEDBuffer.values.first().id,
            expedition_id = EEGPROCEEDBuffer.values.first().expedition_id,
            session = EEGPROCEEDBuffer.values.first().session,
            channel1 = EEGPROCEEDBuffer.values.map { it.channel1 }.median(),
            channel2 = EEGPROCEEDBuffer.values.map {it.channel2}.median(),
            isMarked = false
        )
        metricsDao.insertEEGPROCEEDCompressedMetric(compressed)

        // Очищаем буфер
        EEGPROCEEDBuffer.values.clear()
        EEGPROCEEDBuffer.firstTimestamp = null
    }

    private suspend fun flushEEGArtifactBuffer() {
        if (EEGArtifactBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = EEGArtifactsMetricCompressedEntity(
            timestamp = EEGArtifactBuffer.firstTimestamp!!, // начало минутного интервала
            id = EEGArtifactBuffer.values.first().id,
            expedition_id = EEGArtifactBuffer.values.first().expedition_id,
            session = EEGArtifactBuffer.values.first().session,
            artifactsChannel1 = EEGArtifactBuffer.values.map { it.artifactsChannel1 }.majority(),
            artifactsChannel2 = EEGArtifactBuffer.values.map { it.artifactsChannel2 }.majority(),
            qualityChannel1 = EEGArtifactBuffer.values.map { it.qualityChannel1 }.median(),
            qualityChannel2 = EEGArtifactBuffer.values.map {it.qualityChannel2}.median(),
            isMarked = false
        )
        metricsDao.insertEEGArtifactsCompressedMetric(compressed)

        // Очищаем буфер
        EEGArtifactBuffer.values.clear()
        EEGArtifactBuffer.firstTimestamp = null
    }

    private suspend fun flushPhysiologicalBuffer(){
        if (PhysiologicalBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = PhysiologicalMetricCompressedEntity(
            timestamp = PhysiologicalBuffer.firstTimestamp!!, // начало минутного интервала
            id = PhysiologicalBuffer.values.first().id,
            expedition_id = PhysiologicalBuffer.values.first().expedition_id,
            session = PhysiologicalBuffer.values.first().session,
            relax = PhysiologicalBuffer.values.map { it.relax }.median(),
            fatigue = PhysiologicalBuffer.values.map { it.fatigue }.median(),
            none = PhysiologicalBuffer.values.map { it.none }.median(),
            concentration = PhysiologicalBuffer.values.map { it.concentration }.median(),
            involvement = PhysiologicalBuffer.values.map { it.involvement }.median(),
            stress = PhysiologicalBuffer.values.map { it.stress }.median(),
            nfbArtifacts = PhysiologicalBuffer.values.map { it.nfbArtifacts }.majority(),
            cardioArtifacts = PhysiologicalBuffer.values.map { it.cardioArtifacts }.majority(),
            isMarked = false
        )
        metricsDao.insertPhysiologicalCompressedMetric(compressed)

        // Очищаем буфер
        PhysiologicalBuffer.values.clear()
        PhysiologicalBuffer.firstTimestamp = null
    }

    private suspend fun flushEmotionalBuffer(){
        if (EmotionalBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = EmotionalMetricCompressedEntity(
            timestamp = EmotionalBuffer.firstTimestamp!!, // начало минутного интервала
            id = EmotionalBuffer.values.first().id,
            expedition_id = EmotionalBuffer.values.first().expedition_id,
            session = EmotionalBuffer.values.first().session,
            attention = EmotionalBuffer.values.map { it.attention }.median(),
            relaxation = EmotionalBuffer.values.map { it.relaxation }.median(),
            cognitiveLoad = EmotionalBuffer.values.map { it.cognitiveLoad }.median(),
            cognitiveControl = EmotionalBuffer.values.map { it.cognitiveControl }.median(),
            selfControl = EmotionalBuffer.values.map {it.selfControl}.median(),
            isMarked = false
        )
        metricsDao.insertEmotionalCompressedMetric(compressed)

        // Очищаем буфер
        EmotionalBuffer.values.clear()
        EmotionalBuffer.firstTimestamp = null
    }

    private suspend fun flushProductivityBuffer(){
        if (ProductivityBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = ProductivityMetricCompressedEntity(
            timestamp = ProductivityBuffer.firstTimestamp!!, // начало минутного интервала
            id = ProductivityBuffer.values.first().id,
            expedition_id = ProductivityBuffer.values.first().id,
            session = ProductivityBuffer.values.first().session,
            gravity = ProductivityBuffer.values.map { it.gravity }.median(),
            productivity = ProductivityBuffer.values.map { it.productivity }.median(),
            fatigue = ProductivityBuffer.values.map { it.fatigue }.median(),
            reverseFatigue = ProductivityBuffer.values.map { it.reverseFatigue }.median(),
            relaxation = ProductivityBuffer.values.map { it.relaxation }.median(),
            concentration = ProductivityBuffer.values.map { it.concentration }.median(),
            isMarked = false
        )
        metricsDao.insertProductivityCompressedMetric(compressed)

        // Очищаем буфер
        ProductivityBuffer.values.clear()
        ProductivityBuffer.firstTimestamp = null
    }

    suspend fun flushMEMSBuffer(){
        if (MEMSBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = MEMSMetricCompressedEntity(
            timestamp = MEMSBuffer.firstTimestamp!!, // начало минутного интервала
            id = MEMSBuffer.values.first().id,
            expedition_id = MEMSBuffer.values.first().id,
            session = MEMSBuffer.values.first().session,
            accelerometerX = MEMSBuffer.values.map { it.accelerometerX }.median(),
            accelerometerY = MEMSBuffer.values.map { it.accelerometerY }.median(),
            accelerometerZ = MEMSBuffer.values.map { it.accelerometerZ }.median(),
            gyroscopeX = MEMSBuffer.values.map { it.gyroscopeX }.median(),
            gyroscopeY = MEMSBuffer.values.map { it.gyroscopeY }.median(),
            gyroscopeZ = MEMSBuffer.values.map { it.gyroscopeZ }.median(),
            isMarked = false
        )
        metricsDao.insertMEMSCompressedMetric(compressed)

        // Очищаем буфер
        MEMSBuffer.values.clear()
        MEMSBuffer.firstTimestamp = null
    }

    private suspend fun flushCardioBuffer(){
        if (CardioBuffer.values.isEmpty()) return

        // Вычисляем медиану для каждого поля
        val compressed = CardioMetricCompressedEntity(
            timestamp = CardioBuffer.firstTimestamp!!, // начало минутного интервала
            id = CardioBuffer.values.first().id,
            expedition_id = CardioBuffer.values.first().expedition_id,
            session = CardioBuffer.values.first().session,
            heartRate = CardioBuffer.values.map { it.heartRate }.median(),
            hasArtifacts = CardioBuffer.values.map { it.hasArtifacts }.majority(),
            kaplanIndex = CardioBuffer.values.map { it.kaplanIndex }.median(),
            metricsAvailable = CardioBuffer.values.map { it.metricsAvailable }.majority(),
            motionArtifacts = CardioBuffer.values.map { it.motionArtifacts }.majority(),
            skinContact = CardioBuffer.values.map { it.skinContact }.majority(),
            stressIndex = CardioBuffer.values.map { it.stressIndex }.median(),
            isMarked = false
        )
        metricsDao.insertCardioCompressedMetric(compressed)

        // Очищаем буфер
        CardioBuffer.values.clear()
        CardioBuffer.firstTimestamp = null
    }



    private fun List<Float>.median(): Float {
        if (isEmpty()) return 0f
        val sorted = sorted()
        val size = size
        return if (size % 2 == 0) (sorted[size / 2 - 1] + sorted[size / 2]) / 2 else sorted[size / 2]
    }

    private fun List<Boolean>.majority(): Boolean {
        if (isEmpty()) return false
        return count { it } > size / 2
    }
}