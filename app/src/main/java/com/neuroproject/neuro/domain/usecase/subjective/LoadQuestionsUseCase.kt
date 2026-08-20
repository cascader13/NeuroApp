package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import javax.inject.Inject

/**
 * Загружает вопросы субъективного опросника.
 *
 * Если вопросы отсутствует в БД, автоматически импортирует их из ассетов.
 *
 * @return список вопросов опросника
 */
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