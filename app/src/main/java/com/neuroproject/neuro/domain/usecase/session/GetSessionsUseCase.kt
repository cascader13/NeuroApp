package com.neuroproject.neuro.domain.usecase.session

import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.SessionRepository
import javax.inject.Inject

class GetSessionsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<List<Session>> {
        return try {
            val userId = authRepository.getUserId()
            if (userId.isEmpty()) {
                return Result.Error(Exception("Пользователь не авторизован"), "Пользователь не авторизован")
            }
            val sessions = sessionRepository.getSessionsByUserId(userId)
            Result.Success(sessions)
        } catch (e: Exception) {
            Result.Error(e, "Не удалось загрузить сессии: ${e.message}")
        }
    }
}