package com.neuroproject.neuro.screens.subtest

import android.annotation.SuppressLint
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R
import com.neuroproject.neuro.data.subtest.BlockType
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import com.neuroproject.neuro.ui.theme.ThemeMode

@Composable
fun SubTestScreen(
    viewModel: SubTestViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (uiState) {
            SubTestScreenState.Instruction -> InstructionScreen(
                onStartClick = viewModel::startTest
            )

            SubTestScreenState.Question -> QuestionsScreen(
                question = viewModel.currentQuestion.collectAsState().value,
                currentIndex = viewModel.currentQuestionIndex.collectAsState().value,
                totalCount = viewModel.questions.collectAsState().value.size,
                onAnswerSelected = viewModel::onAnswerSelected,
                onSaveClick = viewModel::onSaveAnswer
            )

            SubTestScreenState.Comment -> CommentScreen(
                comment = viewModel.comment.collectAsState().value,
                onCommentChange = viewModel::onCommentChanged,
                onFinishClick = viewModel::onFinishTestClick
            )

            SubTestScreenState.Waiting -> WaitingScreen(
                timeLeftMillis = viewModel.timeLeftMillis.collectAsState().value
            )

            SubTestScreenState.Result -> {
                val result by viewModel.result.collectAsState()
                result?.let {
                    ResultScreen(
                        result = it,
                        onFinish = onFinish
                    )
                }
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Инструкция к тесту",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.instruction_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth(0.6f),
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
    var sliderValue by remember { mutableFloatStateOf(5f) }

    LaunchedEffect(question?.id) {
        sliderValue = 5f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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

        Spacer(modifier = Modifier.height(24.dp))

        // Текст вопроса
        question?.let {
            Text(
                text = it.text,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
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
            modifier = Modifier.align(Alignment.End),
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
}

@Composable
private fun CommentScreen(
    comment: String,
    onCommentChange: (String) -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Оставьте комментарий",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            label = { Text("Комментарий") },
            placeholder = { Text("Поделитесь впечатлениями...") },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = false,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFinishClick,
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Завершить тест",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun WaitingScreen(
    timeLeftMillis: Long,
    modifier: Modifier = Modifier
) {
    val minutes = (timeLeftMillis / 1000) / 60
    val seconds = (timeLeftMillis / 1000) % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    // Бесконечная плавная пульсация
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
        ) {
            // Внешний круг
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
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
        Spacer(modifier = Modifier.height(24.dp))
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
    }
}

@Composable
private fun ResultScreen(
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
            text = "Результаты тестирования",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Общий результат в карточке
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Общий индекс",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.totalIndex.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Субъективные индексы
        Text(
            text = "Субъективная оценка",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ResultCard(label = "Когнитивный", value = result.subjectiveCognitive)
        ResultCard(label = "Эмоциональный", value = result.subjectiveEmotional)
        ResultCard(label = "Физический", value = result.subjectivePhysical)

        Spacer(modifier = Modifier.height(16.dp))

        // Объективные индексы (заглушки)
        Text(
            text = "Объективные показатели",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ResultCard(label = "Когнитивный", value = result.objectiveCognitive)
        ResultCard(label = "Эмоциональный", value = result.objectiveEmotional)
        ResultCard(label = "Физический", value = result.objectivePhysical)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Завершить",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ResultCard(label: String, value: Int?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = value?.toString() ?: "—",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewInstructionScreen() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT) {
        InstructionScreen(onStartClick = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewQuestionsScreen() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT) {
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
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT) {
        CommentScreen(
            comment = "Тестовый комментарий",
            onCommentChange = {},
            onFinishClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewWaitingScreen() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT) {
        WaitingScreen(timeLeftMillis = 2 * 60 * 1000L)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewResultScreen() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT) {
        ResultScreen(
            result = SubTestViewModel.SubTestResult(
                totalIndex = 78,
                subjectiveCognitive = 85,
                subjectiveEmotional = 72,
                subjectivePhysical = 68,
                objectiveCognitive = 60,
                objectiveEmotional = 55,
                objectivePhysical = 70
            ),
            onFinish = {}
        )
    }
}