package com.neuroproject.neuro.screens.subtest

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
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.data.subtest.BlockType
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
import kotlinx.coroutines.delay

@Composable
fun SubTestScreen(
    viewModel: SubTestViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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

@Composable
fun InstructionScreen(onStartClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Перенести инструкцию к тесту
        Text("Инструкция к тесту...", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartClick) {
            Text("Начать")
        }
    }
}

@Composable
fun QuestionsScreen(
    question: SubjectiveQuestionEntity?,
    currentIndex: Int,
    totalCount: Int,
    onAnswerSelected: (Int) -> Unit,
    onSaveClick: () -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(5f) }

    LaunchedEffect(question?.id) {
        sliderValue = 5f
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Прогресс
        LinearProgressIndicator(
            progress = (currentIndex + 1) / totalCount.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
        Text("Вопрос ${currentIndex + 1} из $totalCount")

        // Текст вопроса
        question?.let {
            Text(it.text, fontSize = 24.sp, modifier = Modifier.weight(1f))
        }

        // Слайдер (шаг = 1, диапазон 1..10)
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Значение: ${sliderValue.toInt()}")

        Button(
            onClick = {
                onAnswerSelected(sliderValue.toInt())
                onSaveClick()
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Сохранить ответ")
        }
    }
}

@Composable
fun WaitingScreen(timeLeftMillis: Long) {
    val minutes = (timeLeftMillis / 1000) / 60
    val seconds = (timeLeftMillis / 1000) % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Анимация пульсирующего круга
        var scale by remember { mutableFloatStateOf(1f) }
        LaunchedEffect(Unit) {
            while(true) {
                scale = 1.2f
                delay(500)
                scale = 1f
                delay(500)
            }
        }
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .background(Color.Blue, CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ожидайте окончания тестирования", fontSize = 18.sp)
        Text("Осталось: $timeText", fontSize = 16.sp)
    }
}

@Composable
fun CommentScreen(
    comment: String,
    onCommentChange: (String) -> Unit,
    onFinishClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = comment,
            onValueChange = onCommentChange,
            label = { Text("Комментарий") },
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onFinishClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Завершить тест")
        }
    }
}


@Composable
fun ResultScreen(
    result: SubTestViewModel.SubTestResult,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Результаты тестирования", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Общий результат
        Text("Общий результат: ${result.totalIndex}", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Субъективные индексы
        Text("Субъективные:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        ResultItem("Когнитивный", result.subjectiveCognitive)
        ResultItem("Эмоциональный", result.subjectiveEmotional)
        ResultItem("Физический", result.subjectivePhysical)

        Spacer(modifier = Modifier.height(16.dp))

        // Объективные индексы (заглушки)
        Text("Объективные:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        ResultItem("Когнитивный", result.objectiveCognitive)
        ResultItem("Эмоциональный", result.objectiveEmotional)
        ResultItem("Физический", result.objectivePhysical)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onFinish) {
            Text("Завершить")
        }
    }
}

@Composable
fun ResultItem(label: String, value: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label:")
        Text(value?.toString() ?: "—")
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewInstructionScreen() {
        InstructionScreen(onStartClick = {})

}

@Preview(showBackground = true)
@Composable
fun PreviewQuestionsScreen() {
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

@Preview(showBackground = true)
@Composable
fun PreviewCommentScreen() {
        CommentScreen(
            comment = "Тестовый комментарий",
            onCommentChange = {},
            onFinishClick = {}
        )
}

@Preview(showBackground = true)
@Composable
fun PreviewWaitingScreen() {
        WaitingScreen(timeLeftMillis = 2 * 60 * 1000L)
}

@Preview(showBackground = true)
@Composable
fun PreviewResultScreen() {
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





