package com.neuroproject.neuro.presentation.screens.subtest

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.domain.model.ExpeditionResult
import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.usecase.device.ObserveConnectionStateUseCase
import com.neuroproject.neuro.domain.usecase.expedition.CheckExpeditionIdUseCase
import com.neuroproject.neuro.domain.usecase.expedition.SaveExpeditionIdUseCase
import com.neuroproject.neuro.domain.usecase.fatigue.CalculateTotalFatigueUseCase
import com.neuroproject.neuro.domain.usecase.recording.ObserveSensorStreamUseCase
import com.neuroproject.neuro.domain.usecase.recording.SaveSensorSampleUseCase
import com.neuroproject.neuro.domain.usecase.recording.StartRecordingUseCase
import com.neuroproject.neuro.domain.usecase.recording.StopRecordingUseCase
import com.neuroproject.neuro.domain.usecase.session.CreateSessionUseCase
import com.neuroproject.neuro.domain.usecase.session.FinishSessionUseCase
import com.neuroproject.neuro.domain.usecase.subjective.CalculateSubjectiveResultUseCase
import com.neuroproject.neuro.domain.usecase.subjective.LoadQuestionsUseCase
import com.neuroproject.neuro.domain.usecase.subjective.SaveAnswersUseCase
import com.neuroproject.neuro.domain.repository.SensorEvent
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.usecase.objective.*
import com.neuroproject.neuro.domain.usecase.sensor.ObserveResistanceUseCase
import com.neuroproject.neuro.domain.usecase.sensor.StartResistanceCheckUseCase
import com.neuroproject.neuro.domain.usecase.sensor.StopResistanceCheckUseCase
import com.neuroproject.neuro.data.device.SessionIdProvider
import com.neuroproject.neuro.domain.usecase.subjective.GetAnswerScoreByIdUseCase
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import com.neuroproject.neuro.presentation.screens.sensorchecking.toElectrodeStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * ViewModel экрана субъективного теста.
 *
 * Управляет прохождением теста: загрузкой вопросов, записью ответов,
 * таймером, сбором данных датчиков и расчётом результатов усталости.
 */
@HiltViewModel
class SubTestViewModel @Inject constructor(
    private val loadQuestionsUseCase: LoadQuestionsUseCase,
    private val saveAnswersUseCase: SaveAnswersUseCase,
    private val calculateSubjectiveResultUseCase: CalculateSubjectiveResultUseCase,
    private val calculateTotalFatigueUseCase: CalculateTotalFatigueUseCase,
    private val createSessionUseCase: CreateSessionUseCase,
    private val finishSessionUseCase: FinishSessionUseCase,
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val observeSensorStreamUseCase: ObserveSensorStreamUseCase,
    private val saveSensorSampleUseCase: SaveSensorSampleUseCase,
    private val checkExpeditionIdUseCase: CheckExpeditionIdUseCase,
    private val saveExpeditionIdUseCase: SaveExpeditionIdUseCase,
    private val getAnswerScoreByIdUseCase: GetAnswerScoreByIdUseCase,
    private val calculateObjectiveFatigueUseCase: CalculateObjectiveFatigueUseCase,
    private val getAllMinuteMetricsUseCase: GetAllMinuteMetricsUseCase,
    private val saveMinuteFatigueResultUseCase: SaveMinuteFatigueResultUseCase,
    private val getAvailableMinutesCountUseCase: GetAvailableMinutesCountUseCase,
    private val observeResistanceUseCase: ObserveResistanceUseCase,
    private val startResistanceCheckUseCase: StartResistanceCheckUseCase,
    private val stopResistanceCheckUseCase: StopResistanceCheckUseCase,
    private val authRepository: AuthRepository,
    private val sessionIdProvider: SessionIdProvider,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
    private val subjectiveTestRepository: SubjectiveTestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubTestUiState())
    val uiState: StateFlow<SubTestUiState> = _uiState.asStateFlow()

    private val _isDeviceDisconnected = MutableStateFlow(false)
    val isDeviceDisconnected: StateFlow<Boolean> = _isDeviceDisconnected.asStateFlow()

    private val _showExpeditionDialog = MutableStateFlow(false)
    val showExpeditionDialog: StateFlow<Boolean> = _showExpeditionDialog.asStateFlow()

    private val _expeditionInputError = MutableStateFlow<String?>(null)
    val expeditionInputError: StateFlow<String?> = _expeditionInputError.asStateFlow()

    private var passingPrematurely = false

    private val _questions = MutableStateFlow<List<SubjectiveQuestion>>(emptyList())
    val questions: StateFlow<List<SubjectiveQuestion>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    val currentQuestion: StateFlow<SubjectiveQuestion?> = combine(
        questions, currentQuestionIndex
    ) { questions, index ->
        questions.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _answers = mutableStateMapOf<Int, Int>()
    private val _answersList = MutableStateFlow<List<SubjectiveAnswer>>(emptyList())
    private val _previousAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val previousAnswers: StateFlow<Map<Int, Int>> = _previousAnswers.asStateFlow()


    private var currentSession: Session? = null
    private var timerJob: Job? = null
    private var sensorJob: Job? = null

    // Timer state
    private val _timeLeftMillis = MutableStateFlow(_uiState.value.selectedDurationMinutes * 60 * 1000L)
    private var isTimerRunning = false
    private var isTimerExpired = false
    val timeLeftMillis: StateFlow<Long> = _timeLeftMillis.asStateFlow()

    /*private fun getDefaultSessionCategory(): SessionCategory {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> SessionCategory.MORNING
            in 12..17 -> SessionCategory.DAY
            in 18..23 -> SessionCategory.EVENING
            else -> SessionCategory.TECHNICAL
        }
    }*/

    init {
        loadQuestions()
        observeSensorData()
        checkExpeditionId()
        observeResistance()
        startResistanceCheck()
        createSession()
        observeConnectionState()
    }

    private fun observeConnectionState() {
        observeConnectionStateUseCase()
            .catch { error ->
                Log.e("SubTestViewModel", "Error observing connection state", error)
            }
            .onEach { state ->
                if (state == DeviceConnectionState.disconnected || state == DeviceConnectionState.error) {
                    viewModelScope.launch {
                        try {
                            timerJob?.cancel()
                            stopRecordingUseCase()
                        } catch (e: Exception) {
                            Log.e("SubTestViewModel", "Error stopping on disconnect", e)
                        }
                    }
                    _isDeviceDisconnected.value = true
                }
            }
            .launchIn(viewModelScope)
    }

    private fun createSession() {
        viewModelScope.launch {
            // Убедимся, что значения синхронизированы
            val currentState = _uiState.value.screenState
            if (currentState is SubTestScreenState.SessionSettings) {
                // Синхронизируем selected значения с screenState
                _uiState.update {
                    it.copy(
                        selectedDurationMinutes = currentState.durationMinutes,
                        selectedCategory = currentState.category
                    )
                }
            }

            currentSession = createSessionUseCase(
                durationMinutes = getCurrentDurationMinutes(),
                category = getCurrentCategory()
            )
            currentSession?.let { session ->
                sessionIdProvider.setSessionId(session.sessionId.toString())
            }
        }
    }

    private fun startResistanceCheck() {
        viewModelScope.launch {
            try {
                delay(500)
                startResistanceCheckUseCase()
            } catch (e: Exception) {
                Log.e("SubTestViewModel", "Error starting resistance check", e)
            }
        }
    }

    private fun observeResistance() {
        observeResistanceUseCase()
            .catch { error ->
                Log.e("SubTestViewModel", "Error observing resistance", error)
            }
            .onEach { resistanceData ->
                _uiState.update {
                    it.copy(electrodeStates = resistanceData.toElectrodeStates())
                }
            }
            .launchIn(viewModelScope)
    }

    private fun checkExpeditionId() {
        viewModelScope.launch {
            val result = checkExpeditionIdUseCase()
            when (result) {
                is ExpeditionResult.Success -> {
                    _showExpeditionDialog.value = false
                    // Можно продолжить нормальную работу
                }
                is ExpeditionResult.NotSet -> {
                    _showExpeditionDialog.value = true
                }
                is ExpeditionResult.Error -> {
                    _showExpeditionDialog.value = true
                    _expeditionInputError.value = result.message
                }
            }
        }
    }

    fun saveExpeditionId(expeditionId: String) {
        viewModelScope.launch {
            _expeditionInputError.value = null
            val result = saveExpeditionIdUseCase(expeditionId)
            when (result) {
                is ExpeditionResult.Success -> {
                    _showExpeditionDialog.value = false
                    // После успешного сохранения можно продолжить
                }
                is ExpeditionResult.Error -> {
                    _expeditionInputError.value = result.message
                }
                else -> {}
            }
        }
    }

    fun dismissExpeditionDialog() {
        viewModelScope.launch {
            val result = checkExpeditionIdUseCase()
            if (result is ExpeditionResult.Success) {
                _showExpeditionDialog.value = false
            }
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            val questions = loadQuestionsUseCase()
            _questions.value = questions
        }
    }

    private fun observeSensorData() {
        sensorJob = viewModelScope.launch {
            observeSensorStreamUseCase().collect { event ->
                when (event) {
                    is SensorEvent.ProductivityScore -> {
                        updateCalibrationProgress(event.data)
                    }
                    else -> {
                        currentSession?.let { session ->
                            if (session.sessionId != 0L) {
                                when (event) {
                                    is SensorEvent.NFB -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.HR -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.Physiological -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.MEMS -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.Productivity -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.Emotional -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.EEGRaw -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.EEGProcessed -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.EEGArtifact -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.ProductivityBaseline -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.ProductivityIndexes -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    is SensorEvent.PhysiologicalBaseline -> saveSensorSampleUseCase(event.data.copy(sessionId = session.sessionId.toString()))
                                    else -> {}
                                }
                            } else {
                                Log.w("SubTestViewModel", "Session ID is 0, skipping save for ${event.javaClass.simpleName}")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateCalibrationProgress(sample: ProductivityScoreSample) {
        val progress = (sample.score * 100).roundToInt()
        val isReady = sample.score >= 1.0f

        val currentState = _uiState.value.screenState
        if (currentState is SubTestScreenState.SessionSettings) {
            _uiState.update {
                it.copy(
                    screenState = currentState.copy(
                        isCalibrationReady = isReady,
                        calibrationProgressPercent = progress
                    ),
                    isCalibrationReady = isReady,
                    calibrationProgressPercent = progress
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isCalibrationReady = isReady,
                    calibrationProgressPercent = progress
                )
            }
        }
    }

    fun updateDuration(durationMinutes: Int) {
        _timeLeftMillis.value = durationMinutes * 60 * 1000L
        val currentState = _uiState.value.screenState
        if (currentState is SubTestScreenState.SessionSettings) {
            _uiState.update {
                it.copy(
                    screenState = currentState.copy(
                        durationMinutes = durationMinutes
                    ),
                    selectedDurationMinutes = durationMinutes
                )
            }
        }
    }

    fun updateCategory(category: SessionCategory) {
        val currentState = _uiState.value.screenState
        if (currentState is SubTestScreenState.SessionSettings) {
            _uiState.update {
                it.copy(
                    screenState = currentState.copy(
                        category = category
                    ),
                    selectedCategory = category
                )
            }
        }
    }

    val calibrationProgress: StateFlow<Int> = _uiState.map { it.calibrationProgressPercent }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0
    )

    private suspend fun loadPreviousAnswers(questions: List<SubjectiveQuestion>): Map<Int, Int> {
        val previousAnswers = mutableMapOf<Int, Int>()
        for (question in questions) {
            try {
                val score = getAnswerScoreByIdUseCase(question.id)
                if (score != null && score > 0 && score <= 10) {
                    previousAnswers[question.id] = score
                }
            } catch (e: Exception) {
                Log.e("SubTestViewModel", "Error loading previous answer for question ${question.id}", e)
            }
        }
        return previousAnswers
    }

    fun startTest() {
        viewModelScope.launch {
            isTimerExpired = false
            _uiState.update { it.copy(isTimerExpired = false) }
            currentSession?.let { session ->
                sessionIdProvider.setSessionId(session.sessionId.toString())
            }
            currentSession = currentSession?.copy(durationMinutes = getCurrentDurationMinutes(), category = getCurrentCategory())

            startRecordingUseCase()
            startTimer(_timeLeftMillis.value)

            val previousAnswers = loadPreviousAnswers(_questions.value)

            _uiState.update {
                it.copy(
                    screenState = SubTestScreenState.Question(
                        questions = _questions.value,
                        currentIndex = 0,
                        currentAnswer = null,
                        previousAnswers = previousAnswers
                    )
                )
            }
        }
    }

    private fun currentSettings(): Pair<Int, SessionCategory> {
        return _uiState.value.selectedDurationMinutes to _uiState.value.selectedCategory
    }

    private fun getCurrentDurationMinutes(): Int = currentSettings().first

    private fun getCurrentCategory(): SessionCategory = currentSettings().second

    private fun startTimer(durationMillis: Long) {
        timerJob?.cancel()
        _timeLeftMillis.value = durationMillis
        timerJob = viewModelScope.launch {
            while (_timeLeftMillis.value > 0) {
                delay(1000L)
                _timeLeftMillis.value = (_timeLeftMillis.value - 1000L).coerceAtLeast(0)
                val currentState = _uiState.value.screenState
                if (currentState is SubTestScreenState.Waiting) {
                    _uiState.update {
                        it.copy(
                            screenState = currentState.copy(
                                timeLeftMillis = _timeLeftMillis.value
                            )
                        )
                    }
                }
            }
            if (_timeLeftMillis.value <= 0) {
                onTimerFinished()
            }
        }
    }

    private fun onTimerFinished() {
        isTimerRunning = false
        isTimerExpired = true
        _uiState.update { it.copy(isTimerExpired = true) }
        when (_uiState.value.screenState) {
            is SubTestScreenState.Waiting-> {
                finishTest()
            }
            else -> {
                // Пользователь ещё на экране вопросов или комментария — даём завершить
            }
        }
    }

    fun goToInstruction() {
        _uiState.update {
            it.copy(screenState = SubTestScreenState.Instruction)
        }
    }

    fun loadPreviousAnswerForQuestion(questionId: Int) {
        viewModelScope.launch {
            try {
                val score = getAnswerScoreByIdUseCase(questionId)
                if (score != null && score > 0) {
                    _previousAnswers.update {
                        it.toMutableMap().apply { put(questionId, score) }
                    }
                }
            } catch (e: Exception) {
                Log.e("SubTestViewModel", "Error loading previous answer for question $questionId", e)
            }
        }
    }

    fun onAnswerSelected(value: Int) {
        val question = currentQuestion.value ?: return
        _answers[question.id] = value

        val currentState = _uiState.value.screenState
        if (currentState is SubTestScreenState.Question) {
            _uiState.update {
                it.copy(
                    screenState = currentState.copy(currentAnswer = value)
                )
            }
        }
    }

    fun onSaveAnswer() {
        val currentState = _uiState.value.screenState
        if (currentState !is SubTestScreenState.Question) return

        val question = currentState.questions.getOrNull(currentState.currentIndex) ?: return
        val answerValue = _answers[question.id] ?: return

        val answer = SubjectiveAnswer(
            questionId = question.id,
            value = answerValue
        )

        val currentAnswers = _answersList.value.toMutableList()
        val existingIndex = currentAnswers.indexOfFirst { it.questionId == question.id }
        if (existingIndex >= 0) {
            currentAnswers[existingIndex] = answer
        } else {
            currentAnswers.add(answer)
        }
        _answersList.value = currentAnswers

        currentSession?.let { session ->
            viewModelScope.launch {
                saveAnswersUseCase(session.sessionId, currentAnswers)
            }
        }

        if (currentState.currentIndex < currentState.questions.lastIndex) {
            val nextIndex = currentState.currentIndex + 1
            _currentQuestionIndex.value = nextIndex

            _uiState.update {
                it.copy(
                    screenState = currentState.copy(
                        currentIndex = nextIndex,
                        currentAnswer = _answers[currentState.questions[nextIndex].id]
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(screenState = SubTestScreenState.Comment)
            }
        }
    }

    fun goToPreviousQuestion() {
        val currentState = _uiState.value.screenState
        if (currentState !is SubTestScreenState.Question || currentState.currentIndex <= 0) return

        val prevIndex = currentState.currentIndex - 1
        val prevQuestion = currentState.questions[prevIndex]
        _currentQuestionIndex.value = prevIndex

        _uiState.update {
            it.copy(
                screenState = currentState.copy(
                    currentIndex = prevIndex,
                    currentAnswer = _answers[prevQuestion.id]
                )
            )
        }
    }

    /**
     * Обновляет текст комментария к сессии.
     *
     * @param newComment новый текст комментария
     */
    fun onCommentChanged(newComment: String) {
        _uiState.update { it.copy(comment = newComment) }
    }

    /**
     * Обрабатывает нажатие кнопки завершения теста.
     * Если таймер ещё работает — показывает экран ожидания с оставшимся временем.
     * Если таймер истёк — сразу завершает тест.
     */
    fun onFinishTestClick() {
        if (_timeLeftMillis.value <= 0 || isTimerExpired) {
            finishTest()
        } else {
            _uiState.update {
                it.copy(
                    screenState = SubTestScreenState.Waiting(
                        timeLeftMillis = _timeLeftMillis.value,
                        totalDurationMillis = getCurrentDurationMinutes() * 60 * 1000L,
                        hasRetaken = it.hasRetaken
                    )
                )
            }
        }
    }

    /**
     * Начинает повторное прохождение теста.
     * Очищает ответы, загружает предыдущие ответы для отображения
     * и возвращает к первому вопросу.
     */
    fun retakeTest() {
        isTimerExpired = false
        _uiState.update { it.copy(hasRetaken = true, isTimerExpired = false) }
        viewModelScope.launch {
            _answers.clear()
            _answersList.value = emptyList()
            _currentQuestionIndex.value = 0

            val previousAnswers = loadPreviousAnswers(_questions.value)

            _uiState.update {
                it.copy(
                    screenState = SubTestScreenState.Question(
                        questions = _questions.value,
                        currentIndex = 0,
                        currentAnswer = null,
                        previousAnswers = previousAnswers
                    )
                )
            }
        }
    }

    /**
     * Принудительно останавливает тест.
     * Отменяет таймер, останавливает запись данных и помечает сессию
     * как завершённую досрочно.
     */
    fun forceStopTest() {
        viewModelScope.launch {
            isTimerRunning = false
            timerJob?.cancel()
            timerJob = null
            stopRecordingUseCase()
            sensorJob?.cancel()
            sensorJob = null
            passingPrematurely = true
            finishTest()
        }
    }

    private suspend fun calculateObjectiveResult(): ObjectiveFatigueResult? {
        val session = currentSession ?: return null

        return try {
            // Получаем количество доступных минут
            val availableMinutes = getAvailableMinutesCountUseCase(session.sessionId)

            if (availableMinutes == 0) {
                Log.w("SubTestViewModel", "No minute data available for objective calculation")
                return null
            }

            // Получаем метрики по всем минутам
            val allMinuteMetrics = getAllMinuteMetricsUseCase(session.sessionId)

            if (allMinuteMetrics.isEmpty()) {
                Log.w("SubTestViewModel", "Failed to load minute metrics")
                return null
            }

            // Рассчитываем объективный результат
            val objectiveResult = calculateObjectiveFatigueUseCase(allMinuteMetrics)

            // Сохраняем результаты по каждой минуте
            allMinuteMetrics.forEach { minuteData ->
                val cognitiveIndex = calculateCognitiveIndexForMinute(minuteData)
                val physiologicalIndex = calculatePhysiologicalIndexForMinute(minuteData)
                val psychologicalIndex = calculatePsychologicalIndexForMinute(minuteData)

                saveMinuteFatigueResultUseCase(
                    FatigueResult(
                        minuteIndex = minuteData.minuteIndex,
                        cognitive = cognitiveIndex,
                        physiological = physiologicalIndex,
                        psychological = psychologicalIndex,
                        sessionId = session.sessionId
                    )
                )
            }

            objectiveResult
        } catch (e: Exception) {
            Log.e("SubTestViewModel", "Error calculating objective result", e)
            null
        }
    }

    private fun calculateCognitiveIndexForMinute(minuteData: MinuteFatigueData): Float {
        return FatigueCoefficients.cognitiveFatigue * minuteData.cognitive.fatigue +
                FatigueCoefficients.cognitiveConcentration * minuteData.cognitive.concentration +
                FatigueCoefficients.cognitiveProductivity * minuteData.cognitive.productivity +
                FatigueCoefficients.cognitiveCognitiveLoad * minuteData.cognitive.cognitiveLoad
    }

    private fun calculatePhysiologicalIndexForMinute(minuteData: MinuteFatigueData): Float {
        return FatigueCoefficients.physiologicalFatigue * minuteData.physiological.fatigue +
                FatigueCoefficients.physiologicalStress * minuteData.physiological.stress +
                FatigueCoefficients.physiologicalRelax * minuteData.physiological.relax +
                FatigueCoefficients.physiologicalInvolvement * minuteData.physiological.involvement
    }

    private fun calculatePsychologicalIndexForMinute(minuteData: MinuteFatigueData): Float {
        return FatigueCoefficients.psychologicalCognitiveLoad * minuteData.psychological.cognitiveLoad +
                FatigueCoefficients.psychologicalRelaxation * minuteData.psychological.relaxation +
                FatigueCoefficients.psychologicalSelfControl * minuteData.psychological.selfControl +
                FatigueCoefficients.psychologicalCognitiveControl * minuteData.psychological.cognitiveControl
    }

    private fun finishTest() {
        viewModelScope.launch {
            try {
                stopRecordingUseCase()
                sensorJob?.cancel()
                sensorJob = null

                var finalAnswers = _answersList.value

                if (_uiState.value.hasRetaken) {
                    val session = currentSession
                    if (session != null) {
                        try {
                            val existingAnswers = subjectiveTestRepository.getAnswers(session.sessionId)
                            finalAnswers = finalAnswers.map { newAnswer ->
                                val existing = existingAnswers.find { it.questionId == newAnswer.questionId }
                                if (existing != null) {
                                    SubjectiveAnswer(
                                        questionId = newAnswer.questionId,
                                        value = (newAnswer.value + existing.value) / 2
                                    )
                                } else {
                                    newAnswer
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("SubTestViewModel", "Error loading existing answers for averaging", e)
                        }
                    }
                }

                currentSession?.let { session ->
                    saveAnswersUseCase(session.sessionId, finalAnswers)
                }

                val subjectiveResult = calculateSubjectiveResultUseCase(
                    questions = _questions.value,
                    answers = finalAnswers
                )

                val objectiveResult = calculateObjectiveResult()

                val fatigueSummary = if (objectiveResult != null) {
                    calculateTotalFatigueUseCase(
                        subjective = subjectiveResult,
                        objective = objectiveResult
                    )
                } else {
                    FatigueSummary(
                        subjective = subjectiveResult,
                        objective = ObjectiveFatigueResult(
                            cognitiveIndex = 0,
                            psychologicalIndex = 0,
                            physiologicalIndex = 0,
                            averageIndex = 0,
                            fatigueLevel = "Недостаточно данных",
                            stressLevel = "Недостаточно данных"
                        ),
                        total = TotalFatigueResult(
                            cognitiveIndex = subjectiveResult.cognitiveIndex,
                            psychologicalIndex = subjectiveResult.emotionalIndex,
                            physiologicalIndex = subjectiveResult.physicalIndex,
                            averageIndex = subjectiveResult.averageIndex
                        )
                    )
                }

                val deviceName = authRepository.getDeviceName()
                val baseComment = _uiState.value.comment
                val finalComment = if (deviceName.isNotBlank()) {
                    if (baseComment.isNotBlank()) {
                        "$baseComment\n[Устройство: $deviceName]"
                    } else {
                        "[Устройство: $deviceName]"
                    }
                } else {
                    baseComment.takeIf { it.isNotBlank() }
                }

                currentSession?.let { session ->
                    finishSessionUseCase(
                        session = session,
                        fatigueSummary = fatigueSummary,
                        comment = finalComment,
                        passedPrematurely = passingPrematurely
                    )
                }

                _uiState.update {
                    it.copy(
                        screenState = SubTestScreenState.Result(fatigueSummary)
                    )
                }

            } catch (e: Exception) {
                Log.e("SubTestViewModel", "Error finishing test", e)
                _uiState.update {
                    it.copy(
                        screenState = SubTestScreenState.SessionSettings(),
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isTimerRunning = false
        timerJob?.cancel()
        sensorJob?.cancel()
        viewModelScope.launch {
            stopRecordingUseCase()
            try {
                stopResistanceCheckUseCase()
            } catch (e: Exception) {
                Log.e("SubTestViewModel", "Error stopping resistance check", e)
            }
        }
    }
}

private object FatigueCoefficients {
    const val cognitiveFatigue = 0.30f
    const val cognitiveConcentration = 0.25f
    const val cognitiveProductivity = 0.20f
    const val cognitiveCognitiveLoad = 0.25f

    const val physiologicalFatigue = 0.35f
    const val physiologicalStress = 0.25f
    const val physiologicalRelax = 0.20f
    const val physiologicalInvolvement = 0.20f

    const val psychologicalCognitiveLoad = 0.30f
    const val psychologicalRelaxation = 0.25f
    const val psychologicalSelfControl = 0.25f
    const val psychologicalCognitiveControl = 0.20f
}