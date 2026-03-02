package com.neuroproject.neuro.screens.subtest

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.data.SessionDao
import com.neuroproject.neuro.data.SessionEntity
import com.neuroproject.neuro.data.subtest.BlockType
import com.neuroproject.neuro.data.subtest.SubjectiveAnswerDao
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionRepository
import com.neuroproject.neuro.services.RecordManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SubTestViewModel @Inject constructor(
    private val questionRepository: SubjectiveQuestionRepository,
    private val answerDao: SubjectiveAnswerDao,
    private val sessionDao: SessionDao,
    private val recordManager: RecordManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<SubTestScreenState>(SubTestScreenState.Instruction)
    val uiState: StateFlow<SubTestScreenState> = _uiState.asStateFlow()

    private val _questions = MutableStateFlow<List<SubjectiveQuestionEntity>>(emptyList())
    val questions: StateFlow<List<SubjectiveQuestionEntity>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    val currentQuestion: StateFlow<SubjectiveQuestionEntity?> = combine(
        questions, currentQuestionIndex
    ) { questions, index ->
        questions.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _answers = mutableStateMapOf<Int, Int>()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    private val _timeLeftMillis = MutableStateFlow(10 * 60 * 1000L)
    val timeLeftMillis: StateFlow<Long> = _timeLeftMillis.asStateFlow()

    private var sessionId: Long? = null
    private var timerJob: Job? = null

    data class SubTestResult(
        val totalIndex: Int,
        val subjectiveCognitive: Int?,
        val subjectiveEmotional: Int?,
        val subjectivePhysical: Int?,
        val objectiveCognitive: Int,
        val objectiveEmotional: Int,
        val objectivePhysical: Int
    )

    private val _result = MutableStateFlow<SubTestResult?>(null)
    val result: StateFlow<SubTestResult?> = _result.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            val questions = questionRepository.getAllQuestions()
            _questions.value = questions
        }
    }

    fun startTest() {
        viewModelScope.launch {
            // 1. Создаём запись сессии в БД
            val newSessionId = System.currentTimeMillis()
            val session = SessionEntity(sessionId = newSessionId)
            sessionDao.insert(session)

            sessionId = newSessionId

            // 2. Передаём sessionId в RecordManager
            recordManager.setSessionId(newSessionId)

            // 3. Запускаем запись с датчиков
            recordManager.startRecording()

            // 4. Запускаем таймер на 10 минут
            startTimer(10 * 60 * 1000L)

            // 5. Переходим к вопросам
            _uiState.value = SubTestScreenState.Question
        }
    }

    private fun startTimer(durationMillis: Long) {
        timerJob?.cancel()
        _timeLeftMillis.value = durationMillis
        timerJob = viewModelScope.launch {
            while (_timeLeftMillis.value > 0) {
                delay(1000L)
                _timeLeftMillis.value = (_timeLeftMillis.value - 1000L).coerceAtLeast(0)
            }
            onTimerFinished()
        }
    }

    private fun onTimerFinished() {
        when (val state = _uiState.value) {
            SubTestScreenState.Waiting, SubTestScreenState.Comment -> finishTest()
            else -> { /* время истекло, но мы не в состоянии ожидания или комментария – ничего не делаем */
            }
        }
    }

    fun onAnswerSelected(value: Int) {
        val question = currentQuestion.value ?: return
        _answers[question.id] = value
    }

    fun onSaveAnswer() {
        val question = currentQuestion.value ?: return
        val answer = _answers[question.id] ?: return

        // Сохраняем ответ в БД
        viewModelScope.launch {
            sessionId?.let { sid ->
//                val answerEntity = SubjectiveAnswerEntity(
//                    sessionId = Timestamp(sid),
//                    questionId = question.id,
//                    value = answer
//                )
//                answerDao.insert(answerEntity)
            }
        }

        // Переход к следующему вопросу или в состояние комментария
        if (_currentQuestionIndex.value < _questions.value.lastIndex) {
            _currentQuestionIndex.value++
        } else {
            _uiState.value = SubTestScreenState.Comment
        }
    }

    fun onCommentChanged(newComment: String) {
        _comment.value = newComment
    }

    fun onFinishTestClick() {
        if (_timeLeftMillis.value <= 0) {
            // Время истекло – завершаем тест сразу
            finishTest()
        } else {
            // Переходим в ожидание, таймер продолжает идти
            _uiState.value = SubTestScreenState.Waiting
        }
    }

    private fun calculateSubjectiveIndexes(): Triple<Int?, Int?, Int?> {
        val answersByBlock = _answers.entries.groupBy { entry ->
            _questions.value.find { it.id == entry.key }?.blockType
        }

        fun computeIndex(block: BlockType): Int? {
            val blockAnswers = answersByBlock[block] ?: return null
            if (blockAnswers.isEmpty()) return null
            val N = blockAnswers.size
            // Сумма трансформированных значений (1..10 → после инверсии)
            val sumTransformed = blockAnswers.sumOf { (questionId, value) ->
                val question = _questions.value.find { it.id == questionId }!!
                if (question.isReversed) 11 - value else value
            }
            // Индекс в диапазоне 0..100, можно переработать
            val index = ((sumTransformed - N) / (9.0 * N) * 100).roundToInt()
            return index.coerceIn(0, 100)
        }

        return Triple(
            computeIndex(BlockType.COGNITIVE),
            computeIndex(BlockType.EMOTIONAL),
            computeIndex(BlockType.PHYSICAL)
        )
    }


    private fun finishTest() {
        viewModelScope.launch {
            // Останавливаем запись с датчиков
            recordManager.stopRecording()

            val sid = sessionId ?: return@launch

            // Получаем существующую сессию
            val session = sessionDao.getSession(sid) ?: return@launch

            // Вычисляем субъективные индексы
            val (subjCog, subjEmo, subjPhys) = calculateSubjectiveIndexes()

            // Позже здесь сделать рассчет объективных метрик и total

            // Обновляем сессию
            val updatedSession = session.copy(
                subjectiveCognitive = subjCog,
                subjectiveEmotional = subjEmo,
                subjectivePhysical = subjPhys,
                comment = _comment.value.takeIf { it.isNotBlank() }
                // objective* и total пока остаются как есть (null)
            )
            sessionDao.update(updatedSession)

            val resultData = SubTestResult(
                totalIndex = 0, // Заглушка
                subjectiveCognitive = subjCog,
                subjectiveEmotional = subjEmo,
                subjectivePhysical = subjPhys,
                objectiveCognitive = 0,   // Заглушка
                objectiveEmotional = 0,   // Заглушка
                objectivePhysical = 0      // Заглушка
            )
            _result.value = resultData

            // Переходим на экран результатов
            _uiState.value = SubTestScreenState.Result
        }
    }

}

