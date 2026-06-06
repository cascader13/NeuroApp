package com.neuroproject.neuro.presentation.screens.analysis

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.model.NFBSample
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AnalysisScreenContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun content_showsCurrentNfbValuesAndConnectionState() {
        // Проверяем stateless UI: экран принимает готовые domain-модели и не зависит от JNI.
        composeRule.setContent {
            NeuroApplicationTheme {
                AnalysisScreenContent(
                    nfbData = NFBSample(alpha = 1.23f, beta = 2.34f, delta = 3.45f),
                    plotData = PlotData(),
                    isRecording = false,
                    connectionState = DeviceConnectionState.connected,
                    onBackPressed = {},
                    onClear = {},
                    onStartRecording = {},
                    onStopRecording = {}
                )
            }
        }

        composeRule.onNodeWithTag("analysis_screen").assertIsDisplayed()
        composeRule.onNodeWithText("1.23").assertIsDisplayed()
        composeRule.onNodeWithText("2.34").assertIsDisplayed()
        composeRule.onNodeWithText("3.45").assertIsDisplayed()
        composeRule.onNodeWithText("connected").assertIsDisplayed()
    }

    @Test
    fun recordButton_switchesCallbackByRecordingState() {
        // В неактивной записи кнопка вызывает start, в активной — stop.
        var startClicks = 0
        var stopClicks = 0

        composeRule.setContent {
            NeuroApplicationTheme {
                AnalysisScreenContent(
                    nfbData = NFBSample(),
                    plotData = PlotData(),
                    isRecording = false,
                    connectionState = DeviceConnectionState.connected,
                    onBackPressed = {},
                    onClear = {},
                    onStartRecording = { startClicks++ },
                    onStopRecording = { stopClicks++ }
                )
            }
        }

        composeRule.onNodeWithTag("analysis_record_toggle").performClick()
        assertEquals(1, startClicks)
        assertEquals(0, stopClicks)
    }

    @Test
    fun headerButtons_emitCallbacks() {
        // Навигация назад и очистка графиков остаются внешними событиями.
        var backClicks = 0
        var clearClicks = 0

        composeRule.setContent {
            NeuroApplicationTheme {
                AnalysisScreenContent(
                    nfbData = NFBSample(),
                    plotData = PlotData(
                        alphaPoints = listOf(DataPoint(0f, 1f), DataPoint(1f, 2f)),
                        betaPoints = listOf(DataPoint(0f, 1f), DataPoint(1f, 2f)),
                        deltaPoints = listOf(DataPoint(0f, 1f), DataPoint(1f, 2f))
                    ),
                    isRecording = true,
                    connectionState = DeviceConnectionState.connected,
                    onBackPressed = { backClicks++ },
                    onClear = { clearClicks++ },
                    onStartRecording = {},
                    onStopRecording = {}
                )
            }
        }

        composeRule.onNodeWithTag("analysis_back").performClick()
        composeRule.onNodeWithTag("analysis_clear").performClick()

        assertEquals(1, backClicks)
        assertEquals(1, clearClicks)
    }
}
