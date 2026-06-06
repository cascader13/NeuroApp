package com.neuroproject.neuro.domain.usecase

import javax.inject.Inject

class ValidateUserIdUseCase @Inject constructor() {

    operator fun invoke(userId: String): ValidationResult {
        return when {
            userId.isBlank() -> ValidationResult.Error("Введите ID пользователя")
            userId.length < 2 -> ValidationResult.Error("ID должен содержать минимум 2 символа")
            !userId.matches(Regex("^[a-zA-Z0-9_-]+$")) ->
                ValidationResult.Error("ID может содержать только буквы, цифры, дефис и подчеркивание")
            else -> ValidationResult.Success
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}