package com.neuroproject.neuro.services

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.repository.SensorStreamGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingControllerTest {

    private lateinit var controller: RecordingController
    private lateinit var sensorDataCollector: SensorDataCollector
    private lateinit var persistenceService: MetricsPersistenceService
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sensorDataCollector = mock()
        persistenceService = mock()
        controller = RecordingController(
            sensorDataCollector = sensorDataCollector,
            persistenceService = persistenceService,
            appScope = kotlinx.coroutines.CoroutineScope(testDispatcher)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not recording`() = runTest {
        advanceUntilIdle()

        assertThat(controller.isRecording.value).isFalse()
    }

    @Test
    fun `setSessionId stores session id`() {
        controller.setSessionId(123L)

        assertThat(controller.getSessionId()).isEqualTo(123L)
    }

    @Test
    fun `setCredentials stores userId and expeditionId`() {
        controller.setCredentials("user_123", "exp_456")

        controller.startRecording()

        assertThat(controller.isRecording.value).isTrue()
    }

    @Test
    fun `startRecording sets isRecording to true`() = runTest {
        controller.startRecording()

        advanceUntilIdle()

        assertThat(controller.isRecording.value).isTrue()
    }

    @Test
    fun `stopRecording sets isRecording to false`() = runTest {
        controller.startRecording()
        advanceUntilIdle()

        controller.stopRecording()
        advanceUntilIdle()

        assertThat(controller.isRecording.value).isFalse()
    }

    @Test
    fun `stopRecording flushes all buffers`() = runTest {
        controller.startRecording()
        advanceUntilIdle()

        controller.stopRecording()
        advanceUntilIdle()

        verify(persistenceService).flushAllBuffers()
    }

    @Test
    fun `setupListeners subscribes to all sensor streams`() = runTest {
        whenever(sensorDataCollector.observeNFB()).thenReturn(flowOf())
        whenever(sensorDataCollector.observePhysiological()).thenReturn(flowOf())
        whenever(sensorDataCollector.observeHR()).thenReturn(flowOf())
        whenever(sensorDataCollector.observeMEMS()).thenReturn(flowOf())
        whenever(sensorDataCollector.observeProductivity()).thenReturn(flowOf())
        whenever(sensorDataCollector.observeEmotional()).thenReturn(flowOf())
        whenever(sensorDataCollector.observeEEGRaw()).thenReturn(flowOf())
        whenever(sensorDataCollector.observeEEGProcessed()).thenReturn(flowOf())
        whenever(sensorDataCollector.observeEEGArtifacts()).thenReturn(flowOf())
        whenever(sensorDataCollector.observeProductivityIndexes()).thenReturn(flowOf())
        whenever(sensorDataCollector.observeProductivityBaseline()).thenReturn(flowOf())
        whenever(sensorDataCollector.observePhysiologicalBaseline()).thenReturn(flowOf())

        controller.setSessionId(1L)
        controller.startRecording()
        advanceUntilIdle()

        verify(sensorDataCollector).observeNFB()
        verify(sensorDataCollector).observePhysiological()
        verify(sensorDataCollector).observeHR()
        verify(sensorDataCollector).observeMEMS()
        verify(sensorDataCollector).observeProductivity()
        verify(sensorDataCollector).observeEmotional()
        verify(sensorDataCollector).observeEEGRaw()
        verify(sensorDataCollector).observeEEGProcessed()
        verify(sensorDataCollector).observeEEGArtifacts()
        verify(sensorDataCollector).observeProductivityIndexes()
        verify(sensorDataCollector).observeProductivityBaseline()
        verify(sensorDataCollector).observePhysiologicalBaseline()
    }
}
