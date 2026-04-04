package com.neuroproject.neuro.screens.subtest

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R
import com.neuroproject.neuro.data.session.SessionCategory
import com.neuroproject.neuro.data.subtest.BlockType
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme

@Composable
fun SubTestScreen(
    viewModel: SubTestViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()


    val productivityScoreState by viewModel.productivityScore.collectAsState() // Для забора изменений
    LaunchedEffect(productivityScoreState) { // Сделал через Launched effect получение измененённых данных
        Log.d("SUB_TEST", "productivity score ${productivityScoreState.score}")
    }


    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (uiState) {

            SubTestScreenState.SessionSettings -> SessionSettingsScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                onNext = { viewModel.goToInstruction() }
            )

            SubTestScreenState.Instruction -> InstructionScreen(
                onStartClick = viewModel::startTest,
                modifier = Modifier.padding(innerPadding)
            )

            SubTestScreenState.Question -> QuestionsScreen(
                question = viewModel.currentQuestion.collectAsState().value,
                currentIndex = viewModel.currentQuestionIndex.collectAsState().value,
                totalCount = viewModel.questions.collectAsState().value.size,
                onAnswerSelected = viewModel::onAnswerSelected,
                onSaveClick = viewModel::onSaveAnswer,
                modifier = Modifier.padding(innerPadding)
            )

            SubTestScreenState.Comment -> CommentScreen(
                comment = viewModel.comment.collectAsState().value,
                onCommentChange = viewModel::onCommentChanged,
                onFinishClick = viewModel::onFinishTestClick,
                modifier = Modifier.padding(innerPadding)
            )

            SubTestScreenState.Waiting -> WaitingScreen(
                timeLeftMillis = viewModel.timeLeftMillis.collectAsState().value,
                totalDuration = viewModel.selectedDurationMinutes.collectAsState().value * 60 * 1000L,
                modifier = Modifier.padding(innerPadding)
            )

            SubTestScreenState.Result -> {
                val result by viewModel.result.collectAsState()
                result?.let {
                    ResultScreen(
                        result = it,
                        onFinish = onFinish,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SessionSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SubTestViewModel,
    onNext: () -> Unit
) {
    val durationMinutes by viewModel.selectedDurationMinutes.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isReady by viewModel.isCalibrationReady.collectAsState()
    val progressPercent by viewModel.calibrationProgressPercent.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Заголовок
        Text(
            text = "Настройки сессии",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Выбор длительности
        Text(
            text = "Длительность: $durationMinutes мин",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Slider(
            value = durationMinutes.toFloat(),
            onValueChange = { viewModel.updateDuration(it.toInt()) },
            valueRange = 4f..25f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Выбор категории (вертикальный список)
        Text(
            text = "Категория",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SessionCategory.values().forEach { category ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateCategory(category) }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedCategory == category,
                        onClick = { viewModel.updateCategory(category) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (category) {
                            SessionCategory.MORNING -> "Утро"
                            SessionCategory.DAY -> "День"
                            SessionCategory.EVENING -> "Вечер"
                            SessionCategory.TECHNICAL -> "Технический"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Калибровка датчиков: $progressPercent%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNext,
                enabled = isReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
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
private fun InstructionScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
            text = stringResource(R.string.instruction_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(0.5f))
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp),
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
private fun QuestionsScreen(
    question: SubjectiveQuestionEntity?,
    currentIndex: Int,
    totalCount: Int,
    onAnswerSelected: (Int) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalCount == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var sliderValue by remember { mutableFloatStateOf(5f) }

    LaunchedEffect(question?.id) {
        sliderValue = 5f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Верхняя панель с прогрессом
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

        Spacer(modifier = Modifier.height(44.dp))

        // Текст вопроса
        question?.let {
            Text(
                text = it.text,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Слайдер
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Text(
                text = "Выбрано: ${sliderValue.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onAnswerSelected(sliderValue.toInt())
                onSaveClick()
            },
            modifier = Modifier
                .align(Alignment.End)
                .height(56.dp),
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CommentScreen(
    comment: String,
    onCommentChange: (String) -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxLength = 500
    val isMaxLength = comment.length >= maxLength

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
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

        // Карточка с полем ввода
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
                    .padding(8.dp),
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

        // Счётчик символов
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

        // Кнопка отправки
        Button(
            onClick = onFinishClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
private fun WaitingScreen(
    timeLeftMillis: Long,
    modifier: Modifier = Modifier,
    totalDuration: Long
) {
    val progress = 1f - (timeLeftMillis.toFloat() / totalDuration)

    val minutes = (timeLeftMillis / 1000) / 60
    val seconds = (timeLeftMillis / 1000) % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    // Длительности фаз дыхания
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Круг с анимацией дыхания и прогрессом
        Box(contentAlignment = Alignment.Center) {
            // Дышащий круг
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                // Внешний круг
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = outerAlpha))
                )
                // Внутренний круг
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            // Круговой прогресс (таймер)
            CircularProgressIndicator(
                progress = { progress },
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
}

@Composable
fun ResultScreen(
    result: SubTestViewModel.SubTestResult,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Результаты",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Общий индекс
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Общий индекс", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(result.totalIndex.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ResultSection(
            "Субъективные показатели",
            listOf(
                "Когнитивный" to result.subjectiveCognitive,
                "Психологический" to result.subjectivePsychological,
                "Физический" to result.subjectivePhysiological
            )
        )

        ResultSection(
            "Объективные показатели",
            listOf(
                "Когнитивный" to result.objectiveCognitive,
                "Психологический" to result.objectivePsychological,
                "Физический" to result.objectivePhysiological
            )
        )

        ResultSection(
            "Интегральные показатели",
            listOf(
                "Когнитивный" to result.totalCognitive,
                "Психологический" to result.totalPsychological,
                "Физический" to result.totalPhysiological
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Завершить", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ResultSection(title: String, items: List<Pair<String, Int?>>) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(8.dp))
    items.forEach { (label, value) -> ResultCard(label, value) }
}

@Composable
fun ResultCard(label: String, value: Int?) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value?.toString() ?: "—", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewInstructionScreen() {
    NeuroApplicationTheme() {
        InstructionScreen(onStartClick = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewQuestionsScreen() {
    NeuroApplicationTheme() {
        QuestionsScreen(
            question = SubjectiveQuestionEntity(
                id = 1,
                text = "Чувствуете ли вы эмоциональное истощение?",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 1,
                isReversed = false
            ),
            currentIndex = 0,
            totalCount = 5,
            onAnswerSelected = {},
            onSaveClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCommentScreen() {
    NeuroApplicationTheme() {
        CommentScreen(
            comment = "Тестовый комментарий",
            onCommentChange = {},
            onFinishClick = {}
        )
    }
}


