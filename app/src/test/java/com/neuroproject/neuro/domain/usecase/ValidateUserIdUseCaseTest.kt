package com.neuroproject.neuro.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateUserIdUseCaseTest {
    private val useCase = ValidateUserIdUseCase()

    @Test
    fun blankId_returnsUserFriendlyError() {
        // Проверяем бизнес-правило: пустой ID нельзя отправлять в репозиторий/логин.
        val result = useCase("")

        assertTrue(result is ValidateUserIdUseCase.ValidationResult.Error)
        assertEquals("Введите ID пользователя", (result as ValidateUserIdUseCase.ValidationResult.Error).message)
    }

    @Test
    fun oneCharacterId_returnsLengthError() {
        // Минимальная длина защищает от случайных коротких идентификаторов.
        val result = useCase("a")

        assertTrue(result is ValidateUserIdUseCase.ValidationResult.Error)
        assertEquals("ID должен содержать минимум 2 символа", (result as ValidateUserIdUseCase.ValidationResult.Error).message)
    }

    @Test
    fun idWithSpacesOrSymbols_returnsFormatError() {
        // Формат ID ограничен буквами, цифрами, дефисом и подчёркиванием.
        val result = useCase("ivan 42!")

        assertTrue(result is ValidateUserIdUseCase.ValidationResult.Error)
        assertEquals(
            "ID может содержать только буквы, цифры, дефис и подчеркивание",
            (result as ValidateUserIdUseCase.ValidationResult.Error).message
        )
    }

    @Test
    fun validId_returnsSuccess() {
        // Валидный ID должен проходить без ошибок, иначе LoginScreen останется заблокированным.
        val result = useCase("ivan_42-A")

        assertTrue(result is ValidateUserIdUseCase.ValidationResult.Success)
    }
}
