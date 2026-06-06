package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.domain.model.SubjectiveAnswer
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import javax.inject.Inject

class SaveAnswersUseCase @Inject constructor(
    private val repository: SubjectiveTestRepository
) {
    suspend operator fun invoke(sessionId: Long, answers: List<SubjectiveAnswer>) {
        repository.saveAnswers(sessionId, answers)
    }
}