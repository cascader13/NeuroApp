// domain/usecase/session/FinishSessionUseCase.kt
package com.neuroproject.neuro.domain.usecase.session

import com.neuroproject.neuro.domain.model.FatigueSummary
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.repository.SessionRepository
import javax.inject.Inject

class FinishSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(
        session: Session,
        fatigueSummary: FatigueSummary,
        comment: String?,
        passedPrematurely: Boolean
    ) {
        // Обновляем сессию с новыми данными
        val updatedSession = session.copy(
            endTime = System.currentTimeMillis(),
            comment = comment,
            passedPrematurely = passedPrematurely,
            subjectiveResult = fatigueSummary.subjective,
            objectiveResult = fatigueSummary.objective,
            totalResult = fatigueSummary.total
        )

        // Используем updateSession из репозитория
        sessionRepository.updateSession(updatedSession)
    }
}