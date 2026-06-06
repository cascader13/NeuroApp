package com.neuroproject.neuro.presentation.screens.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainScreenContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun content_showsMainActions() {
        // Проверяем экран как stateless content: тест не зависит от Hilt и навигации.
        composeRule.setContent {
            NeuroApplicationTheme {
                MainScreenContent(uiState = MainScreenState())
            }
        }

        composeRule.onNodeWithText("НейроСтат").assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.StartSessionButton).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.ResultsButton).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.SettingsButton).assertIsDisplayed()
    }

    @Test
    fun actionButtons_callExpectedCallbacks() {
        // UI-тест фиксирует контракт экрана: кнопки только эмитят события, а навигация остается снаружи.
        var settingsClicks = 0
        var startClicks = 0
        var resultClicks = 0

        composeRule.setContent {
            NeuroApplicationTheme {
                MainScreenContent(
                    uiState = MainScreenState(),
                    onSettingsClick = { settingsClicks++ },
                    onStartSessionClick = { startClicks++ },
                    onViewResultsClick = { resultClicks++ }
                )
            }
        }

        composeRule.onNodeWithTag(MainScreenTags.SettingsButton).performClick()
        composeRule.onNodeWithTag(MainScreenTags.StartSessionButton).performClick()
        composeRule.onNodeWithTag(MainScreenTags.ResultsButton).performClick()

        assertEquals(1, settingsClicks)
        assertEquals(1, startClicks)
        assertEquals(1, resultClicks)
    }
}
