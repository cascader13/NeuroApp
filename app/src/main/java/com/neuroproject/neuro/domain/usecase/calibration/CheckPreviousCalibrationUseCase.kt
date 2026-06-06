package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.repository.CalibrationRepository
import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject

class CheckPreviousCalibrationUseCase @Inject constructor(
    private val calibrationRepository: CalibrationRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        val userId = authRepository.getUserId()
        if (userId.isBlank()) return false
        return calibrationRepository.hasPreviousCalibration(userId)
    }
}