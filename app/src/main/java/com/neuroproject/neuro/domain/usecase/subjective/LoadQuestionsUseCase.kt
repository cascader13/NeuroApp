package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import javax.inject.Inject

class LoadQuestionsUseCase @Inject constructor(
    private val repository: SubjectiveTestRepository
) {
    suspend operator fun invoke(): List<SubjectiveQuestion> {
        var questions = repository.getQuestions()
        android.util.Log.d("LoadQuestionsUseCase", "Loaded ${questions.size} questions")
        questions.forEach {
            android.util.Log.d("LoadQuestionsUseCase", "Question: ${it.id} - ${it.text}")
        }
        if (questions.isEmpty()){
            repository.importQuestions()
        }
        questions = repository.getQuestions()
        android.util.Log.d("LoadQuestionsUseCase", "Loaded ${questions.size} questions")
        questions.forEach {
            android.util.Log.d("LoadQuestionsUseCase", "Question: ${it.id} - ${it.text}")
        }
        return questions
    }
}