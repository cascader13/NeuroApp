package com.neuroproject.neuro.domain.usecase.expedition

import com.neuroproject.neuro.domain.model.ExpeditionResult
import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject

class SaveExpeditionIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(expeditionId: String): ExpeditionResult {
        return try {
            if (expeditionId.isBlank()) {
                return ExpeditionResult.Error("ID экспедиции не может быть пустым")
            }
            authRepository.saveExpeditionId(expeditionId)
            ExpeditionResult.Success(expeditionId)
        } catch (e: Exception) {
            ExpeditionResult.Error(e.message ?: "Ошибка сохранения ID экспедиции")
        }
    }
}