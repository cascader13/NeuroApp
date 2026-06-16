package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import javax.inject.Inject

class LoadQuestionsUseCase @Inject constructor(
    private val repository: SubjectiveTestRepository
) {
    suspend operator fun invoke(): List<SubjectiveQuestion> {
        var questions = repository.getQuestions()
        if (questions.isEmpty()) {
            repository.importQuestions()
        }
        questions = repository.getQuestions()
        return questions
    }
}