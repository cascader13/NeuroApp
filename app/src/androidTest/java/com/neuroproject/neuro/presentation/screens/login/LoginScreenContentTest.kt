package com.neuroproject.neuro.presentation.screens.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoginScreenContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun invalidState_disablesLoginButtonAndShowsError() {
        // Невалидный UI state должен блокировать вход, чтобы не дергать use case с плохими данными.
        composeRule.setContent {
            NeuroApplicationTheme {
                LoginScreenContent(
                    uiState = LoginUIState(userId = "!", isValid = false, errorMessage = "Ошибка"),
                    onUserIdChange = {},
                    onLoginClick = {},
                    onClearSavedData = {},
                    onRemoveFromHistory = {},
                    onSelectFromHistory = {}
                )
            }
        }

        composeRule.onNodeWithText("Ошибка").assertIsDisplayed()
        composeRule.onNodeWithTag(LoginScreenTags.LoginButton).assertIsNotEnabled()
    }

    @Test
    fun validState_enablesLoginAndCallsCallback() {
        // Экран не делает логин сам: он вызывает callback, а ViewModel решает бизнес-логику.
        var loginClicks = 0

        composeRule.setContent {
            NeuroApplicationTheme {
                LoginScreenContent(
                    uiState = LoginUIState(userId = "ivan", isValid = true),
                    onUserIdChange = {},
                    onLoginClick = { loginClicks++ },
                    onClearSavedData = {},
                    onRemoveFromHistory = {},
                    onSelectFromHistory = {}
                )
            }
        }

        composeRule.onNodeWithTag(LoginScreenTags.LoginButton).assertIsEnabled().performClick()
        assertEquals(1, loginClicks)
    }

    @Test
    fun typingInUserIdField_emitsTextChanges() {
        // Тестируем контракт TextField: изменения текста уходят наверх, а не хранятся внутри Composable.
        var typedValue = ""

        composeRule.setContent {
            NeuroApplicationTheme {
                LoginScreenContent(
                    uiState = LoginUIState(),
                    onUserIdChange = { typedValue = it },
                    onLoginClick = {},
                    onClearSavedData = {},
                    onRemoveFromHistory = {},
                    onSelectFromHistory = {}
                )
            }
        }

        composeRule.onNodeWithTag(LoginScreenTags.UserIdField).performTextInput("ab")
        assertEquals("ab", typedValue)
    }
}
