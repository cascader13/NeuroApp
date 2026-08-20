package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import javax.inject.Inject

/**
 * Получает балл последнего ответа пользователя по идентификатору вопроса.
 *
 * @param id идентификатор вопроса
 * @return балл ответа или null, если ответа нет
 */
class GetAnswerScoreByIdUseCase @Inject constructor(
    private val subjectiveTestRepository: SubjectiveTestRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(id: Int): Int? {
        val userId = authRepository.getUserId()
        return subjectiveTestRepository.getLastAnswerScoreById(id, userId)
    }
}