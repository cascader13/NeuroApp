package com.neuroproject.neuro.services

import android.util.Log
import com.neuroproject.neuro.ApplicationScope
import com.neuroproject.neuro.domain.model.ProductivityScoreSample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingController @Inject constructor(
    private val sensorDataCollector: SensorDataCollector,
    private val persistenceService: MetricsPersistenceService,
    @ApplicationScope private val appScope: CoroutineScope
) {
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var currentSessionId: Long? = null
    private var userId: String = ""
    private var expeditionId: String = ""
    private var isSetup = false

    val productivityScore: Flow<ProductivityScoreSample> = sensorDataCollector.observeProductivityScore()

    fun setSessionId(sessionId: Long) {
        currentSessionId = sessionId
    }

    fun getSessionId(): Long? = currentSessionId

    fun setCredentials(userId: String, expeditionId: String) {
        this.userId = userId
        this.expeditionId = expeditionId
    }

    fun startRecording() {
        if (!isSetup) setupListeners()
        _isRecording.value = true
    }

    suspend fun stopRecording() {
        _isRecording.value = false
        persistenceService.flushAllBuffers()
    }

    private fun setupListeners() {
        isSetup = true
        val sessionId = currentSessionId ?: return

        sensorDataCollector.observeNFB().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveNFBMetric(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observePhysiological().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.savePhysiologicalMetric(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observeHR().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveCardioMetric(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observeMEMS().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveMEMSMetric(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observeProductivity().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveProductivityMetric(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observeProductivityIndexes().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveProductivityIndexData(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observeProductivityBaseline().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveProductivityBaselineData(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observePhysiologicalBaseline().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.savePhysiologicalBaselineData(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observeEmotional().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveEmotionalMetric(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observeEEGRaw().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveEEGRawMetric(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observeEEGProcessed().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveEEGProcessedMetric(sessionId, userId, expeditionId, data)
            }
        }

        sensorDataCollector.observeEEGArtifacts().collectInScope(appScope) { data ->
            if (_isRecording.value) {
                persistenceService.saveEEGArtifactsMetric(sessionId, userId, expeditionId, data)
            }
        }
    }

    private fun <T> Flow<T>.collectInScope(scope: CoroutineScope, action: (T) -> Unit) {
        scope.launch {
            this@collectInScope.collect { value ->
                action(value)
            }
        }
    }
}
