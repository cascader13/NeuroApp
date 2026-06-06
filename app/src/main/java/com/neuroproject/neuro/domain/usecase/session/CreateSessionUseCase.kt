// domain/usecase/session/CreateSessionUseCase.kt
package com.neuroproject.neuro.domain.usecase.session

import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.domain.repository.SessionRepository
import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject

class CreateSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        durationMinutes: Int,
        category: SessionCategory
    ): Session {
        val sessionId = System.currentTimeMillis()  // timestamp = sessionId
        val userId = authRepository.getUserId()
        val expeditionId = authRepository.getExpeditionId()

        return sessionRepository.createSession(
            sessionId = sessionId,
            durationMinutes = durationMinutes,
            category = category,
            userId = userId,
            expeditionId = expeditionId
        )
    }
}