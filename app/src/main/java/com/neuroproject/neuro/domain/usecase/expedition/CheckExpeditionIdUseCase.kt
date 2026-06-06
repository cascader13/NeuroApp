package com.neuroproject.neuro.domain.usecase.expedition

import com.neuroproject.neuro.domain.model.ExpeditionResult
import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject

class CheckExpeditionIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): ExpeditionResult {
        return try {
            val expeditionId = authRepository.getExpeditionId()
            if (expeditionId.isNotBlank()) {
                ExpeditionResult.Success(expeditionId)
            } else {
                ExpeditionResult.NotSet
            }
        } catch (e: Exception) {
            ExpeditionResult.Error(e.message ?: "Ошибка проверки ID экспедиции")
        }
    }
}