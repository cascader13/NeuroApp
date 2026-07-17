package com.neuroproject.neuro.presentation.screens.history

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.neuroproject.neuro.domain.model.HistoryState
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.TotalFatigueResult
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HistoryScreenContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsProgressOnly() {
        // Экран должен корректно отрисовывать состояние загрузки без Hilt и ViewModel.
        composeRule.setContent {
            NeuroApplicationTheme {
                HistoryScreenContent(
                    state = HistoryState(isLoading = true),
                    onBackClick = {},
                    onChartClick = {},
                    onSessionClick = {},
                    onRetry = {},
                    onClearError = {},
                    onDeleteSession = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeRule.onNodeWithTag("history_loading").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        // Пустая история — отдельное состояние UI, его нельзя смешивать с ошибкой или загрузкой.
        composeRule.setContent {
            NeuroApplicationTheme {
                HistoryScreenContent(
                    state = HistoryState(sessions = emptyList()),
                    onBackClick = {},
                    onChartClick = {},
                    onSessionClick = {},
                    onRetry = {},
                    onClearError = {},
                    onDeleteSession = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeRule.onNodeWithTag("history_empty").assertIsDisplayed()
        composeRule.onNodeWithText("Нет сессий").assertIsDisplayed()
    }

    @Test
    fun errorState_callsRetryAndClearCallbacks() {
        // Ошибка не обрабатывается внутри Composable: экран только сообщает о действиях пользователя наверх.
        var retryClicks = 0
        var clearClicks = 0

        composeRule.setContent {
            NeuroApplicationTheme {
                HistoryScreenContent(
                    state = HistoryState(error = "Нет базы"),
                    onBackClick = {},
                    onChartClick = {},
                    onSessionClick = {},
                    onRetry = { retryClicks++ },
                    onClearError = { clearClicks++ },
                    onDeleteSession = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeRule.onNodeWithText("Нет базы").assertIsDisplayed()
        composeRule.onNodeWithTag("history_retry").performClick()
        composeRule.onNodeWithTag("history_clear_error").performClick()

        assertEquals(1, retryClicks)
        assertEquals(1, clearClicks)
    }

    @Test
    fun sessionsList_callsSessionClick() {
        // Карточка сессии должна передавать наверх именно domain sessionId.
        var clickedSessionId: Long? = null
        val session = Session(
            sessionId = 42L,
            startTime = 42L,
            totalResult = TotalFatigueResult(10, 20, 30, 20)
        )

        composeRule.setContent {
            NeuroApplicationTheme {
                HistoryScreenContent(
                    state = HistoryState(sessions = listOf(session)),
                    onBackClick = {},
                    onChartClick = {},
                    onSessionClick = { clickedSessionId = it },
                    onRetry = {},
                    onClearError = {},
                    onDeleteSession = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeRule.onNodeWithTag("history_session_42").assertIsDisplayed().performClick()
        assertEquals(42L, clickedSessionId)
    }
}
