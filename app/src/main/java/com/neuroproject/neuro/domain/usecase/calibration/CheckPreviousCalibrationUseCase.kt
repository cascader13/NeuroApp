package com.neuroproject.neuro.domain.usecase.calibration

import com.neuroproject.neuro.domain.repository.CalibrationRepository
import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Проверяет наличие сохранённой калибровки для текущего пользователя.
 *
 * @return true если прошлая калибровка найдена, false если отсутствует или пользователь не авторизован
 */
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