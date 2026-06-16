package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import javax.inject.Inject

class GetAnswerScoreByIdUseCase @Inject constructor(
    private val subjectiveTestRepository: SubjectiveTestRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(id: Int): Int? {
        val userId = authRepository.getUserId()
        return subjectiveTestRepository.getLastAnswerScoreById(id, userId)
    }
}