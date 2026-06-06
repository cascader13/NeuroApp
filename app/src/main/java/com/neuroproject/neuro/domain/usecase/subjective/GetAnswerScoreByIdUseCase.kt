package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.data.repository.AuthRepositoryImpl
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import javax.inject.Inject

class GetAnswerScoreByIdUseCase @Inject constructor(
    private val subjectiveTestRepository: SubjectiveTestRepository,
    private val authRepositoryImpl: AuthRepositoryImpl
) {
    suspend operator fun invoke(id: Int): Int? {
        val userId = authRepositoryImpl.getUserId()
        return subjectiveTestRepository.getLastAnswerScoreById(id, userId)
    }

}