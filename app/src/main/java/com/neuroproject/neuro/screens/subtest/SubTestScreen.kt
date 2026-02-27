//package com.neuroproject.neuro.screens.subtest
//import androidx.compose.runtime.Composable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.viewModelScope
//import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
//import kotlinx.coroutines.launch
//
//@Composable
//fun SubTestScreen(
//    modifier: Modifier = Modifier,
//    viewModel: SubTestViewModel = hiltViewModel(),
//    onTestFinished: () -> Unit
//) {
//    val answers by viewModel.answers.collectAsState()
//    val questions by viewModel.questions.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//
//    if (isLoading) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator()
//        }
//        return
//    }
//
//    val scrollState = rememberScrollState()
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(scrollState)
//    ) {
//        Text(
//            text = "Оцените своё текущее состояние",
//            style = MaterialTheme.typography.headlineSmall
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        LazyColumn(
//            modifier = Modifier.weight(1f)
//        ) {
//            items(questions) { question ->
//                QuestionItem(
//                    question = question,
//                    value = (answers[question.id] ?: 1).toFloat(),
//                    onValueChange = { newValue ->
//                        viewModel.updateAnswer(
//                            question.id,
//                            newValue.toInt()
//                        )
//                    }
//                )
//            }
//        }
//
//        Button(
//            onClick = {
//                viewModel.viewModelScope.launch {
//                    viewModel.saveResults()
//                    onTestFinished()
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Завершить тест")
//        }
//    }
//}
//
//@Composable
//private fun QuestionItem(
//    question: SubjectiveQuestionEntity,
//    value: Float,
//    onValueChange: (Float) -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 12.dp)
//    ) {
//        Text(text = "${question.id}. ${question.text}")
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Slider(
//            value = value,
//            onValueChange = onValueChange,
//            valueRange = 1f..10f,
//            steps = 8
//        )
//
//        Text("Оценка: ${value.toInt()}")
//    }
//}
