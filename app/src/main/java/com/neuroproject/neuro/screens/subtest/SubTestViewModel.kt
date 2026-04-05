package com.neuroproject.neuro.screens.subtest

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.data.MetricsDao
import com.neuroproject.neuro.data.session.SessionCategory
import com.neuroproject.neuro.data.session.SessionDao
import com.neuroproject.neuro.data.session.SessionEntity
import com.neuroproject.neuro.data.subtest.BlockType
import com.neuroproject.neuro.data.subtest.SubjectiveAnswerDao
import com.neuroproject.neuro.data.subtest.SubjectiveAnswerEntity
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionRepository
import com.neuroproject.neuro.services.FatigueAnalyzer
import com.neuroproject.neuro.services.RecordManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SubTestViewModel @Inject constructor(
    private val questionRepository: SubjectiveQuestionRepository,
    private val answerDao: SubjectiveAnswerDao,
    private val metricsDao: MetricsDao,
    private val sessionDao: SessionDao,
    private val recordManager: RecordManager,
    private val fatigueAnalyzer: FatigueAnalyzer
) : ViewModel() {
    private val _uiState = MutableStateFlow<SubTestScreenState>(SubTestScreenState.SessionSettings)
    val uiState: StateFlow<SubTestScreenState> = _uiState.asStateFlow()

    var productivityScore = recordManager.productivityScore

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


    private val _selectedDurationMinutes = MutableStateFlow(10) // по умолчанию 10 минут
    val selectedDurationMinutes: StateFlow<Int> = _selectedDurationMinutes.asStateFlow()

    private val _selectedCategory = MutableStateFlow(getDefaultSessionCategory())
    val selectedCategory: StateFlow<SessionCategory> = _selectedCategory.asStateFlow()

    // Флаг готовности калибровки
    val isCalibrationReady: StateFlow<Boolean> = productivityScore
        .map { it.score >= 1.0f }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Процент выполнения
    val calibrationProgressPercent: StateFlow<Int> = productivityScore
        .map { (it.score * 100).roundToInt() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private fun getDefaultSessionCategory(): SessionCategory {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> SessionCategory.MORNING
            in 12..17 -> SessionCategory.DAY
            in 18..23 -> SessionCategory.EVENING
            else -> SessionCategory.TECHNICAL
        }
    }

    fun updateDuration(durationMinutes: Int) {
        _selectedDurationMinutes.value = durationMinutes
        _timeLeftMillis.value = durationMinutes * 60 * 1000L
    }

    fun updateCategory(category: SessionCategory) {
        _selectedCategory.value = category
    }

    private val _timeLeftMillis = MutableStateFlow(_selectedDurationMinutes.value * 60 * 1000L)
    val timeLeftMillis: StateFlow<Long> = _timeLeftMillis.asStateFlow()

    private var sessionId: Long? = null
    private var timerJob: Job? = null

    data class SubTestResult(
        val totalIndex: Int?,
        val subjectiveCognitive: Int?,
        val subjectivePsychological: Int?,
        val subjectivePhysiological: Int?,
        val objectiveCognitive: Int?,
        val objectivePsychological: Int?,
        val objectivePhysiological: Int?,
        val totalCognitive: Int?,
        val totalPsychological: Int?,
        val totalPhysiological: Int?,
        val averageSubjective: Int?,
        val averageObjective: Int?,
        val objectiveFatigue: String,
        val objectiveStress: String
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
            val session = SessionEntity(
                sessionId = newSessionId,
                durationMinutes = _selectedDurationMinutes.value,
                sessionCategory = _selectedCategory.value
                )
            sessionDao.insert(session)

            sessionId = newSessionId

            // 2. Передаём sessionId в RecordManager
            recordManager.setSessionId(newSessionId)

            // 3. Запускаем запись с датчиков
            recordManager.startRecording()

            // 4. Запускаем таймер
            startTimer(timeLeftMillis.value)

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

    fun goToInstruction() {
        _uiState.value = SubTestScreenState.Instruction
    }

    fun onAnswerSelected(value: Int) {
        val question = currentQuestion.value ?: return
        _answers[question.id] = value
    }

    fun onSaveAnswer() {
        val question = currentQuestion.value ?: return
        val answer = _answers[question.id] ?: return

        viewModelScope.launch {
            sessionId?.let { sid ->
                try {
                    val answerEntity = SubjectiveAnswerEntity(
                        sessionId = sid,
                        questionId = question.id,
                        value = answer
                    )
                    answerDao.insert(answerEntity)
                } catch (e: Exception) {
                    Log.e("SubTestViewModel", "Failed to save answer for question ${question.id}", e)
                }
            }
        }

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
            // Индекс в диапазоне 0..100
            val index = ((sumTransformed - N) / (9.0 * N) * 100).roundToInt()
            return index.coerceIn(0, 100)
        }

        return Triple(
            computeIndex(BlockType.COGNITIVE),
            computeIndex(BlockType.EMOTIONAL),
            computeIndex(BlockType.PHYSICAL)
        )
    }

    fun forceStopTest() {
        viewModelScope.launch {
            timerJob?.cancel()
            timerJob = null
            recordManager.stopRecording()
            finishTest()
        }
    }

    private fun finishTest() {
        viewModelScope.launch {
            recordManager.stopRecording()
            val endTime = System.currentTimeMillis()

            val sid = sessionId ?: return@launch
            val session = sessionDao.getSession(sid) ?: return@launch

            // Субъективные индексы
            val (subjCog, subjEmo, subjPhys) = calculateSubjectiveIndexes()

            // Объективные показатели
            val objectiveResult = fatigueAnalyzer.calculateAll(_selectedDurationMinutes.value, sessionId)
            if (objectiveResult == null) {
                Log.e("SubTestViewModel", "Failed to calculate objective metrics for session $sid")
            }
            val (objCog, objPsych, objPhys) = if (objectiveResult != null) {
                Triple(
                    objectiveResult.averageCognitive.toInt(),
                    objectiveResult.averagePsychological.toInt(),
                    objectiveResult.averagePhysiological.toInt()
                )
            } else {
                Triple(null, null, null)
            }

            // Общие показатели (субъективные + объективные с весом 1:1)
            val totalCog = if (subjCog != null && objCog != null) (subjCog + objCog) / 2 else null
            val totalPsy = if (subjEmo != null && objPsych != null) (subjEmo + objPsych) / 2 else null
            val totalPhys = if (subjPhys != null && objPhys != null) (subjPhys + objPhys) / 2 else null

            // Средние
            val avgSub = listOfNotNull(subjCog, subjEmo, subjPhys).let { list ->
                if (list.isNotEmpty()) list.average().roundToInt() else null
            }
            val avgObj = listOfNotNull(objCog, objPsych, objPhys).let { list ->
                if (list.isNotEmpty()) list.average().roundToInt() else null
            }

            // Общий индекс (среднее трёх общих показателей)
            val total = listOfNotNull(totalCog, totalPsy, totalPhys).let { list ->
                if (list.isNotEmpty()) list.average().roundToInt() else null
            }

            // Получаем старые строковые показатели на всякий случай
            val fatigueList = metricsDao.getRelaxationValuesBySession(sid)
            val stressList = metricsDao.getStressValuesBySession(sid)
            val objFatigue = if (fatigueList.isNotEmpty()) fatigueList[0] else "NoRecommendation"
            val objStress = if (stressList.isNotEmpty()) stressList[0] else "NoStress"



            // Обновляем сессию
            val updatedSession = session.copy(
                subjectiveCognitive = subjCog,
                subjectivePsychological = subjEmo,
                subjectivePhysiological = subjPhys,
                objectiveCognitive = objCog,
                objectivePsychological = objPsych,
                objectivePhysiological = objPhys,
                totalCognitive = totalCog,
                totalPsychological = totalPsy,
                totalPhysiological = totalPhys,
                averageSubjective = avgSub,
                averageObjective = avgObj,
                totalIndex = total,
                objectiveFatigue = objFatigue,
                objectiveStress = objStress,
                endTime = endTime,
                comment = _comment.value.takeIf { it.isNotBlank() }
            )
            sessionDao.update(updatedSession)

            // Формируем результат для UI
            _result.value = SubTestResult(
                subjectiveCognitive = subjCog,
                subjectivePsychological = subjEmo,
                subjectivePhysiological = subjPhys,
                objectiveCognitive = objCog,
                objectivePsychological = objPsych,
                objectivePhysiological = objPhys,
                totalCognitive = totalCog,
                totalPsychological = totalPsy,
                totalPhysiological = totalPhys,
                averageSubjective = avgSub,
                averageObjective = avgObj,
                totalIndex = total,
                objectiveFatigue = objFatigue,
                objectiveStress = objStress
            )

            _uiState.value = SubTestScreenState.Result
        }
    }
}