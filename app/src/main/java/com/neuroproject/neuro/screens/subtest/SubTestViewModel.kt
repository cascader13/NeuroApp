//package com.neuroproject.neuro.screens.subtest
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.neuroproject.neuro.data.subtest.SubjectiveAnswerDao
//import com.neuroproject.neuro.data.subtest.SubjectiveAnswerEntity
//import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
//import com.neuroproject.neuro.data.subtest.SubjectiveQuestionRepository
//import com.neuroproject.neuro.services.RecordManager
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.sql.Timestamp
//import javax.inject.Inject
//
//@HiltViewModel
//class SubTestViewModel @Inject constructor(
//    private val questionRepository: SubjectiveQuestionRepository,
//    private val answerDao: SubjectiveAnswerDao,
//    private val recManager: RecordManager
//) : ViewModel() {
//
//
//    val sessionId : java.sql.Timestamp = recManager.getSession()
//    var screenState by mutableStateOf<SubTestScreenState>(
//        SubTestScreenState.Instruction
//    )
//        private set
//
//    var questions by mutableStateOf<List<SubjectiveQuestionEntity>>(emptyList())
//        private set
//
//    var currentQuestionIndex by mutableStateOf(0)
//        private set
//
//    var currentSliderValue by mutableStateOf(5f)
//
//    var answers = mutableMapOf<Int, Int>()  // questionId -> value
//
//    private fun loadQuestions() {
//        viewModelScope.launch {
//            questions = questionRepository.getAllQuestions()
//        }
//    }
//    init {
//        loadQuestions()
//    }
//
//    fun startTest() {
//        recManager.startRecording()
//        screenState = SubTestScreenState.Question
//        startTimer()
//    }
//
//    fun onNextClicked() {
//        val question = questions[currentQuestionIndex]
//        answers[question.id] = currentSliderValue.toInt()
//
//        if (currentQuestionIndex < questions.lastIndex) {
//            currentQuestionIndex++
//            currentSliderValue = 5f
//        } else {
//            screenState = SubTestScreenState.Comment
//        }
//    }
//
//    var timeLeft by mutableStateOf(600)
//        private set
//
//    private fun startTimer() {
//        viewModelScope.launch {
//            while (timeLeft > 0) {
//                delay(1000)
//                timeLeft--
//            }
//            // если время закончилось, то по идее надо останавливать запись с устройства, но тест он может дорешать
//            TODO("Остановка записи датчиков если закончилось время")
//
//        }
//    }
//
//fun finishTest(onFinished: () -> Unit) {
//        viewModelScope.launch {
//
//            answers.forEach { (questionId, value) ->
//                answerDao.insert(
//                    SubjectiveAnswerEntity(
//                        sessionId = sessionId,
//                        questionId = questionId,
//                        value = value
//                    )
//                )
//            }
//            recManager.stopRecording()
//            onFinished()
//        }
//    }
//}