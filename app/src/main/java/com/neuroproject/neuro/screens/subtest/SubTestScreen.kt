//package com.neuroproject.neuro.screens.subtest
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.Slider
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
//
//@Composable
//fun SubTestScreen(
//    viewModel: SubTestViewModel = hiltViewModel(),
//    onFinished: () -> Unit
//) {
//
//    when (viewModel.screenState) {
//
//        is SubTestScreenState.Instruction -> {
//            InstructionContent(
//                onStart = { viewModel.startTest() }
//            )
//        }
//
//        is SubTestScreenState.Question -> {
//            QuestionContent(
//                question = viewModel.questions[viewModel.currentQuestionIndex],
//                sliderValue = viewModel.currentSliderValue,
//                onSliderChange = { viewModel.currentSliderValue = it },
//                progress = viewModel.currentQuestionIndex + 1,
//                total = viewModel.questions.size,
//                timeLeft = viewModel.timeLeft,
//                onNext = { viewModel.onNextClicked() }
//            )
//        }
//
//        is SubTestScreenState.Comment -> {
//            CommentContent(
//                onFinish = {
//                    viewModel.finishTest(onFinished)
//                }
//            )
//        }
//    }
//}
//
//@Composable
//fun CommentContent(onFinish: () -> Unit) {
//    TODO("Not yet implemented")
//}
//
//@Composable
//fun QuestionContent(
//    question: SubjectiveQuestionEntity,
//    sliderValue: Float,
//    onSliderChange: (Float) -> Unit,
//    progress: Int,
//    total: Int,
//    timeLeft: Int,
//    onNext: () -> Unit
//) {
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.SpaceBetween
//    ) {
//
//        Column {
//
//            Text(text = "Вопрос $progress из $total")
//
//            LinearProgressIndicator(
//                progress = progress / total.toFloat(),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(text = "Осталось времени: $timeLeft сек")
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Text(text = question.text)
//        }
//
//        Column {
//
//            Slider(
//                value = sliderValue,
//                onValueChange = onSliderChange,
//                valueRange = 1f..10f,
//                steps = 8
//            )
//
//            Text(text = sliderValue.toInt().toString())
//
//            Button(
//                onClick = onNext,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Далее")
//            }
//        }
//    }
//}
//
//@Composable
//fun InstructionContent(onStart: () -> Unit) {
//    TODO("Not yet implemented")
//}