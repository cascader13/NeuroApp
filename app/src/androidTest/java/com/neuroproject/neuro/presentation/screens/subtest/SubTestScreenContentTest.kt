package com.neuroproject.neuro.presentation.screens.subtest

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.neuroproject.neuro.domain.model.BlockType
import com.neuroproject.neuro.domain.model.FatigueSummary
import com.neuroproject.neuro.domain.model.ObjectiveFatigueResult
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.model.SubjectiveResult
import com.neuroproject.neuro.domain.model.TotalFatigueResult
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SubTestScreenContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsState_blocksNextUntilCalibrationReady() {
        // Кнопка перехода к инструкции включается только после готовой калибровки.
        composeRule.setContent {
            NeuroApplicationTheme {
                SubTestScreenContent(
                    uiState = SubTestUiState(
                        screenState = SubTestScreenState.SessionSettings(
                            isCalibrationReady = false,
                            calibrationProgressPercent = 40
                        )
                    ),
                    calibrationProgress = 40,
                    onUpdateDuration = {},
                    onUpdateCategory = {},
                    onGoToInstruction = {},
                    onStartTest = {},
                    goToComment = {},
                    onAnswerSelected = {},
                    onSaveAnswer = {},
                    onCommentChanged = {},
                    onFinishTestClick = {},
                    onForceStop = {},
                    onFinish = {},
                    showExpeditionDialog = false,
                    expeditionError = "",
                    onGoToPreviousQuestion = {},
                    onRetakeTest = {},
                    onSaveExpeditionId = {},
                    onDismissExpeditionDialog = { },
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("Калибровка: 40%").assertIsDisplayed()
        composeRule.onNodeWithTag(SubTestScreenTags.NextFromSettingsButton).assertIsNotEnabled()
    }

    @Test
    fun settingsState_readyCalibrationCallsNext() {
        // Когда калибровка завершена, экран только сообщает о клике наверх.
        var nextClicks = 0

        composeRule.setContent {
            NeuroApplicationTheme {
                SubTestScreenContent(
                    uiState = SubTestUiState(
                        screenState = SubTestScreenState.SessionSettings(
                            isCalibrationReady = true,
                            calibrationProgressPercent = 100
                        )
                    ),
                    calibrationProgress = 100,
                    onUpdateDuration = {},
                    onUpdateCategory = {},
                    onGoToInstruction = { nextClicks++ },
                    onStartTest = {},
                    onAnswerSelected = {},
                    onSaveAnswer = {},
                    onCommentChanged = {},
                    onFinishTestClick = {},
                    goToComment = {},
                    onForceStop = {},
                    onFinish = {},
                    showExpeditionDialog = false,
                    expeditionError = "",
                    onGoToPreviousQuestion = {},
                    onRetakeTest = {},
                    onSaveExpeditionId = {},
                    onDismissExpeditionDialog = { },
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithTag(SubTestScreenTags.NextFromSettingsButton).assertIsEnabled().performClick()
        assertEquals(1, nextClicks)
    }

    @Test
    fun questionState_rendersQuestionAndSaveButton() {
        // Вопросы безопасно передаются из state, а сохранение ответа остается событием для ViewModel.
        var saveClicks = 0
        val question = SubjectiveQuestion(
            id = 1,
            text = "Есть ли усталость?",
            blockType = BlockType.PHYSICAL,
            displayOrder = 1,
            isReversed = false
        )

        composeRule.setContent {
            NeuroApplicationTheme {
                SubTestScreenContent(
                    uiState = SubTestUiState(
                        screenState = SubTestScreenState.Question(
                            questions = listOf(question),
                            currentIndex = 0,
                            currentAnswer = 5
                        )
                    ),
                    calibrationProgress = 0,
                    onUpdateDuration = {},
                    onUpdateCategory = {},
                    onGoToInstruction = {},
                    onStartTest = {},
                    onAnswerSelected = {},
                    onSaveAnswer = { saveClicks++ },
                    onCommentChanged = {},
                    onFinishTestClick = {},
                    onForceStop = {},
                    onFinish = {},
                    goToComment = {},
                    showExpeditionDialog = false,
                    expeditionError = "",
                    onGoToPreviousQuestion = {},
                    onRetakeTest = {},
                    onSaveExpeditionId = {},
                    onDismissExpeditionDialog = { },
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("Есть ли усталость?").assertIsDisplayed()
        composeRule.onNodeWithTag(SubTestScreenTags.SaveAnswerButton).performClick()
        assertEquals(1, saveClicks)
    }

    @Test
    fun commentState_emitsCommentChangesAndFinishClick() {
        // Комментарий тестируется отдельно от finish-usecase: экран не знает, как сохраняются данные.
        var comment = ""
        var finishClicks = 0

        composeRule.setContent {
            NeuroApplicationTheme {
                SubTestScreenContent(
                    uiState = SubTestUiState(screenState = SubTestScreenState.Comment),
                    calibrationProgress = 0,
                    onUpdateDuration = {},
                    onUpdateCategory = {},
                    onGoToInstruction = {},
                    onStartTest = {},
                    onAnswerSelected = {},
                    onSaveAnswer = {},
                    onCommentChanged = { comment = it },
                    onFinishTestClick = { finishClicks++ },
                    onForceStop = {},
                    onFinish = {},
                    showExpeditionDialog = false,
                    expeditionError = "",
                    goToComment = {},
                    onGoToPreviousQuestion = {},
                    onRetakeTest = {},
                    onSaveExpeditionId = {},
                    onDismissExpeditionDialog = { },
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithTag(SubTestScreenTags.CommentField).performTextInput("ok")
        composeRule.onNodeWithTag(SubTestScreenTags.FinishCommentButton).performClick()

        assertEquals("ok", comment)
        assertEquals(1, finishClicks)
    }

    @Test
    fun resultState_callsFinish() {
        // Финальный экран не навигирует напрямую: он отдаёт событие в NavGraph.
        var finishClicks = 0
        val summary = FatigueSummary(
            subjective = SubjectiveResult(10, 20, 30, 20),
            objective = ObjectiveFatigueResult(20, 30, 40, 30, "Низкий", "Низкий"),
            total = TotalFatigueResult(15, 25, 35, 25)
        )

        composeRule.setContent {
            NeuroApplicationTheme {
                SubTestScreenContent(
                    uiState = SubTestUiState(screenState = SubTestScreenState.Result(summary)),
                    calibrationProgress = 0,
                    onUpdateDuration = {},
                    onUpdateCategory = {},
                    onGoToInstruction = {},
                    onStartTest = {},
                    onAnswerSelected = {},
                    onSaveAnswer = {},
                    onCommentChanged = {},
                    onFinishTestClick = {},
                    onForceStop = {},
                    onFinish = { finishClicks++ },
                    goToComment = {},
                    showExpeditionDialog = false,
                    expeditionError = "",
                    onGoToPreviousQuestion = {},
                    onRetakeTest = {},
                    onSaveExpeditionId = {},
                    onDismissExpeditionDialog = { },
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("Итоговый индекс").assertIsDisplayed()
        composeRule.onNodeWithTag(SubTestScreenTags.FinishResultButton).performClick()
        assertEquals(1, finishClicks)
    }
}
