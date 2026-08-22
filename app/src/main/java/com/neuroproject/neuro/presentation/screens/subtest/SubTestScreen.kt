package com.neuroproject.neuro.presentation.screens.subtest

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R
import com.neuroproject.neuro.components.ResistanceIndicatorBar
import com.neuroproject.neuro.domain.model.BlockType
import com.neuroproject.neuro.domain.model.FatigueSummary
import com.neuroproject.neuro.domain.model.ObjectiveFatigueResult
import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.model.SubjectiveResult
import com.neuroproject.neuro.domain.model.TotalFatigueResult
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import com.neuroproject.neuro.ui.theme.ThemeMode
import kotlin.math.roundToInt

object SubTestScreenTags {
    const val NextFromSettingsButton = "subtest_settings_next_button"
    const val StartTestButton = "subtest_start_test_button"
    const val AnswerSlider = "subtest_answer_slider"
    const val SaveAnswerButton = "subtest_save_answer_button"
    const val CommentField = "subtest_comment_field"
    const val FinishCommentButton = "subtest_finish_comment_button"
    const val ForceStopButton = "subtest_force_stop_button"
    const val FinishResultButton = "subtest_finish_result_button"
    const val RetakeTestButton = "subtest_retake_test_button"
}

/**
 * Экран прохождения теста субъективной оценки утомления.
 */
@Composable
fun SubTestScreen(
    modifier: Modifier = Modifier,
    viewModel: SubTestViewModel = hiltViewModel(),
    onFinish: () -> Unit,
    onDeviceUnconnected: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val calibrationProgress by viewModel.calibrationProgress.collectAsState()
    val showExpeditionDialog by viewModel.showExpeditionDialog.collectAsState()
    val expeditionError by viewModel.expeditionInputError.collectAsState()
    val isDeviceDisconnected by viewModel.isDeviceDisconnected.collectAsState()

    BackHandler {
        onFinish()
    }

    if (isDeviceDisconnected) {
        AlertDialog(
            onDismissRequest = { onDeviceUnconnected() },
            title = {
                Text(
                    text = "Устройство отключено",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Связь с устройством потеряна. Возврат в главное меню...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onDeviceUnconnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }

    SubTestScreenContent(
        modifier = modifier,
        uiState = uiState,
        calibrationProgress = calibrationProgress,
        showExpeditionDialog = showExpeditionDialog,
        expeditionError = expeditionError,
        isTimerExpired = uiState.isTimerExpired,
        onUpdateDuration = { viewModel.updateDuration(it) },
        onUpdateCategory = { viewModel.updateCategory(it) },
        onGoToInstruction = { viewModel.goToInstruction() },
        onStartTest = { viewModel.startTest() },
        onAnswerSelected = { viewModel.onAnswerSelected(it) },
        onSaveAnswer = { viewModel.onSaveAnswer() },
        onGoToPreviousQuestion = { viewModel.goToPreviousQuestion() },
        onCommentChanged = { viewModel.onCommentChanged(it) },
        onFinishTestClick = { viewModel.onFinishTestClick() },
        onRetakeTest = { viewModel.retakeTest() },
        onForceStop = { viewModel.forceStopTest() },
        onSaveExpeditionId = { viewModel.saveExpeditionId(it) },
        onDismissExpeditionDialog = { viewModel.dismissExpeditionDialog() },
        onFinish = onFinish
    )
}

@Composable
fun SubTestScreenContent(
    uiState: SubTestUiState,
    calibrationProgress: Int,
    showExpeditionDialog: Boolean,
    expeditionError: String?,
    isTimerExpired: Boolean = false,
    onUpdateDuration: (Int) -> Unit,
    onUpdateCategory: (SessionCategory) -> Unit,
    onGoToInstruction: () -> Unit,
    onStartTest: () -> Unit,
    onAnswerSelected: (Int) -> Unit,
    onSaveAnswer: () -> Unit,
    onGoToPreviousQuestion: () -> Unit,
    onCommentChanged: (String) -> Unit,
    onFinishTestClick: () -> Unit,
    onRetakeTest: () -> Unit,
    onForceStop: () -> Unit,
    onSaveExpeditionId: (String) -> Unit,
    onDismissExpeditionDialog: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showExpeditionDialog) {
        ExpeditionIdDialog(
            onSave = onSaveExpeditionId,
            onDismiss = onDismissExpeditionDialog,
            errorMessage = expeditionError
        )
    }

    Surface(
        modifier = modifier.fillMaxSize().systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ResistanceIndicatorBar(
                electrodeStates = uiState.electrodeStates,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val screenState = uiState.screenState) {
                    is SubTestScreenState.SessionSettings -> {
                        SessionSettingsContent(
                            durationMinutes = screenState.durationMinutes,
                            selectedCategory = screenState.category,
                            isCalibrationReady = screenState.isCalibrationReady,
                            calibrationProgressPercent = screenState.calibrationProgressPercent.coerceAtLeast(calibrationProgress),
                            onDurationChange = onUpdateDuration,
                            onCategoryChange = onUpdateCategory,
                            onNext = onGoToInstruction
                        )
                    }
                    is SubTestScreenState.Instruction -> {
                        InstructionContent(onStartClick = onStartTest)
                    }
                    is SubTestScreenState.Question -> {
                        val question = screenState.questions.getOrNull(screenState.currentIndex)
                        if (question == null) {
                            EmptyQuestionsContent()
                        } else {
                            val previousAnswer = screenState.previousAnswers[question.id]

                            QuestionsContent(
                                question = question,
                                currentIndex = screenState.currentIndex,
                                totalCount = screenState.questions.size,
                                currentAnswer = screenState.currentAnswer,
                                previousAnswer = previousAnswer,
                                isTimerExpired = isTimerExpired,
                                onAnswerSelected = onAnswerSelected,
                                onSaveClick = onSaveAnswer,
                                onPreviousClick = onGoToPreviousQuestion
                            )
                        }
                    }
                    is SubTestScreenState.Comment -> {
                        CommentContent(
                            comment = uiState.comment,
                            onCommentChange = onCommentChanged,
                            onFinishClick = onFinishTestClick
                        )
                    }
                    is SubTestScreenState.Waiting -> {
                        WaitingContent(
                            timeLeftMillis = screenState.timeLeftMillis,
                            totalDurationMillis = screenState.totalDurationMillis,
                            hasRetaken = screenState.hasRetaken,
                            onRetakeTest = onRetakeTest,
                            onForceStop = onForceStop
                        )
                    }
                    is SubTestScreenState.Result -> {
                        ResultContent(
                            result = screenState.summary,
                            onFinish = onFinish
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SessionSettingsContent(
    durationMinutes: Int,
    selectedCategory: SessionCategory,
    isCalibrationReady: Boolean,
    calibrationProgressPercent: Int,
    onDurationChange: (Int) -> Unit,
    onCategoryChange: (SessionCategory) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Настройки сессии",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Длительность: $durationMinutes мин",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Slider(
            value = durationMinutes.toFloat(),
            onValueChange = { onDurationChange(it.toInt()) },
            valueRange = 1f..25f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Spacer(modifier = Modifier.height(8.dp))
        EnumSlider(
            enumClass = SessionCategory::class.java,
            selectedValue = selectedCategory,
            onValueChange = onCategoryChange,
            getDisplayText = {getSessionTime(it)}
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Калибровка датчиков: ${calibrationProgressPercent.coerceIn(0, 100)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { calibrationProgressPercent.coerceIn(0, 100) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNext,
                enabled = isCalibrationReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag(SubTestScreenTags.NextFromSettingsButton),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Далее", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun InstructionContent(onStartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Инструкция к тесту",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(0.5f))
        Text(
            text = "Ответьте на вопросы после записи. Используйте шкалу от 1 до 10, где 1 — минимальное проявление, 10 — максимальное.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(0.5f))
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp)
                .testTag(SubTestScreenTags.StartTestButton),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Начать",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}


@Composable
fun QuestionsContent(
    question: SubjectiveQuestion,
    currentIndex: Int,
    totalCount: Int,
    currentAnswer: Int?,
    previousAnswer: Int? = null,
    isTimerExpired: Boolean = false,
    onAnswerSelected: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onPreviousClick: () -> Unit = {}
) {
    if (totalCount == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var sliderValue by remember { mutableFloatStateOf((currentAnswer ?: 5).toFloat()) }

    LaunchedEffect(question.id) {
        sliderValue = (previousAnswer ?: currentAnswer)?.toFloat() ?: 5f
        onAnswerSelected(sliderValue.toInt())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Вопрос ${currentIndex + 1} из $totalCount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${((currentIndex + 1) / totalCount.toFloat() * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = (currentIndex + 1) / totalCount.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        if (isTimerExpired) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    text = "Время вышло. Завершите текущий вопрос.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(44.dp))

        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Заголовок текущего ответа
            Text(
                text = "Ваш ответ:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Шкала значений
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Совсем нет",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Полностью согласен",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Активный ползунок
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    onAnswerSelected(it.toInt())
                },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.testTag(SubTestScreenTags.AnswerSlider),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Text(
                text = "Выбрано: ${sliderValue.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Предыдущий ответ (серый ползунок)
            if (previousAnswer != null && previousAnswer in 1..10) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = "Ваш прошлый ответ:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Шкала для прошлого ответа
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "5",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "10",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Неактивный ползунок (только для отображения)
                    Slider(
                        value = previousAnswer.toFloat(),
                        onValueChange = {},
                        valueRange = 1f..10f,
                        steps = 8,
                        enabled = false,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            activeTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            disabledActiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Text(
                        text = "Был выбран ответ: $previousAnswer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Назад",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(SubTestScreenTags.SaveAnswerButton),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (currentIndex == totalCount - 1) "Завершить" else "Далее",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CommentContent(
    comment: String,
    onCommentChange: (String) -> Unit,
    onFinishClick: () -> Unit
) {
    val maxLength = 500
    val isMaxLength = comment.length >= maxLength

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Ваш комментарий",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            OutlinedTextField(
                value = comment,
                onValueChange = {
                    if (it.length <= maxLength) onCommentChange(it)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .testTag(SubTestScreenTags.CommentField),
                placeholder = { Text("Поделитесь впечатлениями...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = false,
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = 10
            )
        }

        AnimatedVisibility(
            visible = comment.isNotEmpty(),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${comment.length}/$maxLength",
                    color = if (isMaxLength) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = onFinishClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag(SubTestScreenTags.FinishCommentButton),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Завершить тест", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun WaitingContent(
    timeLeftMillis: Long,
    totalDurationMillis: Long,
    hasRetaken: Boolean,
    onRetakeTest: () -> Unit,
    onForceStop: () -> Unit
) {
    var showStopDialog by remember { mutableStateOf(false) }

    val progress = 1f - (timeLeftMillis.toFloat() / totalDurationMillis)

    val minutes = (timeLeftMillis / 1000) / 60
    val seconds = (timeLeftMillis / 1000) % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    val inhaleDuration = 2200
    val exhaleDuration = 2500
    val cycleDuration = inhaleDuration + exhaleDuration

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = cycleDuration
                1.25f at inhaleDuration with FastOutSlowInEasing
                1.0f at cycleDuration with FastOutSlowInEasing
            }
        ), label = "scale"
    )

    val outerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = cycleDuration
                0.5f at inhaleDuration with FastOutSlowInEasing
                0.2f at cycleDuration with FastOutSlowInEasing
            }
        ), label = "outerAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = outerAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(220.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 4.dp
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Идёт запись данных",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Осталось: $timeText",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Дышите ровно и ожидайте окончания",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onRetakeTest,
                enabled = !hasRetaken,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag(SubTestScreenTags.RetakeTestButton),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = if (hasRetaken) "Повторное прохождение использовано" else "Пройти тест снова",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showStopDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag(SubTestScreenTags.ForceStopButton),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Остановить тест")
            }
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = {
                Text(
                    text = "Остановить тест?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите прервать тест? Все собранные данные будут сохранены, но тест завершится досрочно.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopDialog = false
                        onForceStop()
                    }
                ) {
                    Text(
                        text = "Остановить",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("Продолжить")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ResultContent(
    result: FatigueSummary,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Результаты",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Общий итоговый индекс - акцентная карточка
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Общий индекс утомления",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = result.total.averageIndex.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )

                // Добавляем текстовую интерпретацию
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getFatigueLevelText(result.total.averageIndex),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Субъективные показатели
        ResultSection(
            title = "Субъективные показатели",
            cognitive = result.subjective.cognitiveIndex,
            psychological = result.subjective.emotionalIndex,
            physiological = result.subjective.physicalIndex,
            average = result.subjective.averageIndex
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Объективные показатели
        ResultSection(
            title = "Объективные показатели",
            cognitive = result.objective.cognitiveIndex,
            psychological = result.objective.psychologicalIndex,
            physiological = result.objective.physiologicalIndex,
            average = result.objective.averageIndex
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Интегральные показатели
        ResultSection(
            title = "Интегральные показатели",
            cognitive = result.total.cognitiveIndex,
            psychological = result.total.psychologicalIndex,
            physiological = result.total.physiologicalIndex,
            average = result.total.averageIndex
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Дополнительная информация для объективных показателей
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Дополнительно",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Уровень усталости:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = result.objective.fatigueLevel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Уровень стресса:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = result.objective.stressLevel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag(SubTestScreenTags.FinishResultButton),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Завершить", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ResultSection(
    title: String,
    cognitive: Int,
    psychological: Int,
    physiological: Int,
    average: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Строка со средним значением
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Средний индекс:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = average.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Детальные показатели
            ResultDetailRow("Когнитивный", cognitive)
            ResultDetailRow("Психологический", psychological)
            ResultDetailRow("Физический", physiological)
        }
    }
}

@Composable
private fun ResultDetailRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Цветовое выделение в зависимости от значения
        val valueColor = when {
            value >= 70 -> MaterialTheme.colorScheme.error
            value >= 40 -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Мини-прогресс бар
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = value / 100f)
                        .fillMaxHeight()
                        .background(valueColor)
                )
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}

// Slider для работы с перечислениями(Enum)
@Composable
inline fun <reified T : Enum<T>> EnumSlider(
    enumClass: Class<T>,
    selectedValue: T,
    crossinline onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    getDisplayText: (T) -> String = { it.name }
) {
    val enumValues = enumClass.enumConstants ?: emptyArray()
    require(enumValues.isNotEmpty()) { "Enum must have at least one value" }

    val index = enumValues.indexOf(selectedValue)
    val maxIndex = enumValues.size - 1

    Column(modifier = modifier) {

        // Текущее значение
        Text(
            text = "Время: ${getDisplayText(selectedValue)}",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Slider(
            value = index.toFloat(),
            onValueChange = { newIndex ->
                val roundedIndex = newIndex.roundToInt().coerceIn(0, maxIndex)
                onValueChange(enumValues[roundedIndex])
            },
            valueRange = 0f..maxIndex.toFloat(),
            steps = maxIndex - 1
        )


    }
}





@Composable
fun EmptyQuestionsContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Вопросы не загружены", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun ExpeditionIdDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null
) {
    var expeditionId by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Введите ID экспедиции",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    "Для продолжения работы необходимо указать ID экспедиции.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = expeditionId,
                    onValueChange = {
                        expeditionId = it
                        showError = false
                    },
                    label = { Text("ID экспедиции") },
                    isError = showError || errorMessage != null,
                    supportingText = {
                        if (showError && expeditionId.isBlank()) {
                            Text("ID экспедиции не может быть пустым")
                        } else if (errorMessage != null) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (expeditionId.isBlank()) {
                        showError = true
                    } else {
                        onSave(expeditionId)
                    }
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}


private fun getFatigueLevelText(index: Int): String {
    return when (index) {
        in 0..20 -> "Низкий уровень утомления"
        in 21..40 -> "Умеренный уровень утомления"
        in 41..60 -> "Средний уровень утомления"
        in 61..80 -> "Повышенный уровень утомления"
        else -> "Высокий уровень утомления"
    }
}
private fun SessionCategory.titleRu(): String = when (this) { // !!!! ЗАМЕНИТЬ
    SessionCategory.H24_3 -> "0-3"
    SessionCategory.H3_6 -> "3-6"
    SessionCategory.H6_9 -> "6-9"
    SessionCategory.H9_12 -> "9-12"
    SessionCategory.H12_15 -> "12-15"
    SessionCategory.H15_18 -> "15-18"
    SessionCategory.H18_21 -> "18-21"
    SessionCategory.H21_24 -> "21-24"
    SessionCategory.TECHNICAL -> "Техническая"
}

private fun getSessionTime(category: SessionCategory): String{
    return when (category) {
        SessionCategory.H24_3 -> "0-3"
        SessionCategory.H3_6 -> "3-6"
        SessionCategory.H6_9 -> "6-9"
        SessionCategory.H9_12 -> "9-12"
        SessionCategory.H12_15 -> "12-15"
        SessionCategory.H15_18 -> "15-18"
        SessionCategory.H18_21 -> "18-21"
        SessionCategory.H21_24 -> "21-24"
        SessionCategory.TECHNICAL -> "Техническая"
    }
}

// ==================== PREVIEW ====================

@Preview(name = "Светлая тема - Настройки сессии")
@Composable
fun PreviewSessionSettingsLight() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
        SessionSettingsContent(
            durationMinutes = 10,
            selectedCategory = SessionCategory.H6_9,
            isCalibrationReady = true,
            calibrationProgressPercent = 100,
            onDurationChange = {},
            onCategoryChange = {},
            onNext = {}
        )
    }
}

@Preview(name = "Тёмная тема - Вопрос")
@Composable
fun PreviewQuestionDark() {
    NeuroApplicationTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
        QuestionsContent(
            question = SubjectiveQuestion(
                id = 1,
                text = "Чувствуете ли вы эмоциональное истощение?",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 1,
                isReversed = false
            ),
            currentIndex = 0,
            totalCount = 5,
            currentAnswer = 5,
            onAnswerSelected = {},
            onSaveClick = {}
        )
    }
}

@Preview(name = "Результаты")
@Composable
fun PreviewResult() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
        ResultContent(
            result = FatigueSummary(
                subjective = SubjectiveResult(60, 70, 80, 70),
                objective = ObjectiveFatigueResult(50, 60, 70, 60, "Средний", "Низкий"),
                total = TotalFatigueResult(55, 65, 75, 65)
            ),
            onFinish = {}
        )
    }
}