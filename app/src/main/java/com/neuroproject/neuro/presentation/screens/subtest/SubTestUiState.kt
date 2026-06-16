package com.neuroproject.neuro.presentation.screens.subtest

import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.presentation.screens.sensorchecking.ElectrodeStates

sealed class SubTestScreenState {
    data class SessionSettings(
        val durationMinutes: Int = 10,
        val category: SessionCategory = SessionCategory.MORNING,
        val isCalibrationReady: Boolean = false,
        val calibrationProgressPercent: Int = 0
    ) : SubTestScreenState()

    object Instruction : SubTestScreenState()

    data class Question(
        val questions: List<SubjectiveQuestion>,
        val currentIndex: Int,
        val currentAnswer: Int?,
        val previousAnswers: Map<Int, Int> = emptyMap() // Добавляем карту предыдущих ответов
    ) : SubTestScreenState()

    object Comment : SubTestScreenState()

    data class Waiting(
        val timeLeftMillis: Long,
        val totalDurationMillis: Long
    ) : SubTestScreenState()

    data class Result(
        val summary: FatigueSummary
    ) : SubTestScreenState()
}

data class SubTestUiState(
    val screenState: SubTestScreenState = SubTestScreenState.SessionSettings(),
    val selectedDurationMinutes: Int = 10,
    val selectedCategory: SessionCategory = SessionCategory.MORNING,
    val isCalibrationReady: Boolean = false,
    val calibrationProgressPercent: Int = 0,
    val comment: String = "",
    val errorMessage: String? = null,
    val electrodeStates: ElectrodeStates = ElectrodeStates()
)