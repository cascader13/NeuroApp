
package com.neuroproject.neuro.presentation.screens.calibration


data class CalibrationUiState(
    val isCalibrating: Boolean = false,
    val isComplete: Boolean = false,
    val progress: Float = 0f,
    val timeRemaining: Long = 60000L,
    val showPreviousCalibrationDialog: Boolean = false,
    val errorMessage: String? = null
)