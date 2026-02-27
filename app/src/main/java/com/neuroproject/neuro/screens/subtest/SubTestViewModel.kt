//package com.neuroproject.neuro.screens.subtest
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
//import com.neuroproject.neuro.data.subtest.SubjectiveQuestionRepository
//import com.neuroproject.neuro.services.RecordManager
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class SubTestViewModel @Inject constructor(
//    private val questionRepository: SubjectiveQuestionRepository,
//    private val testRepository: SubTestRepository,
//    private val recordManager: RecordManager
//) : ViewModel() {
//
//    private val _answers = MutableStateFlow<Map<Int, Int>>(emptyMap())
//    val answers = _answers.asStateFlow()
//
//    private val _questions = MutableStateFlow<List<SubjectiveQuestionEntity>>(emptyList())
//    val questions = _questions.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(true)
//    val isLoading = _isLoading.asStateFlow()
//
//    init {
//        loadQuestions()
//        recordManager.startRecording()
//    }
//
//    private fun loadQuestions() {
//        viewModelScope.launch {
//            try {
//                // Получаем вопросы из БД
//                val questionsFromDb = questionRepository.getAllQuestions()
//
//                // Если БД пустая, предзаполняем
//                if (questionsFromDb.isEmpty()) {
//                    questionRepository.prefillQuestions()
//                    _questions.value = questionRepository.getAllQuestions()
//                } else {
//                    _questions.value = questionsFromDb
//                }
//
//                // Инициализируем все ответы значением 1
//                _answers.value = questionsFromDb.associate { it.id to 1 }
//
//            } catch (e: Exception) {
//                // Обработка ошибок
//                e.printStackTrace()
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun updateAnswer(questionId: Int, value: Int) {
//        _answers.value = _answers.value.toMutableMap().apply {
//            this[questionId] = value
//        }
//    }
//
//    suspend fun saveResults() {
//        val answersMap = _answers.value
//        val questionsList = _questions.value
//
//        // Вычисляем баллы по категориям
//        val cognitiveScore = questionsList
//            .filter { it.category == "cognitive" }
//            .sumOf { answersMap[it.id] ?: 0 }
//
//        val emotionalScore = questionsList
//            .filter { it.category == "emotional" }
//            .sumOf { answersMap[it.id] ?: 0 }
//
//        val physicalScore = questionsList
//            .filter { it.category == "physical" }
//            .sumOf { answersMap[it.id] ?: 0 }
//
//        val totalScore = cognitiveScore + emotionalScore + physicalScore
//
//        // Создаем JSON строку ответов
//        val gson = com.google.gson.Gson()
//        val answersJson = gson.toJson(answersMap)
//
//        // Создаем entity и сохраняем
//        val result = com.neuroproject.neuro.data.subtest.SubTestResultEntity(
//            timestamp = System.currentTimeMillis(),
//            totalScore = totalScore,
//            cognitiveScore = cognitiveScore,
//            emotionalScore = emotionalScore,
//            physicalScore = physicalScore,
//            answersJson = answersJson
//        )
//
//        testRepository.saveResult(result)
//    }
//
//    fun getResult(): Map<Int, Int> = _answers.value
//}
//
//
//// Extension function для удобного сбора Flow данных
//private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectInScope(
//    scope: CoroutineScope,
//    action: (T) -> Unit
//) {
//    scope.launch {
//        this@collectInScope.collect { value ->
//            action(value)
//        }
//    }
//}