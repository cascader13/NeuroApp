package com.neuroproject.neuro.domain.usecase

import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(userId: String): Result<Unit> {
        return authRepository.login(userId)
    }
}