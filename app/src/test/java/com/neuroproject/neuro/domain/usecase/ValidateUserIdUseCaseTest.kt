package com.neuroproject.neuro.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

class ValidateUserIdUseCaseTest {

    private lateinit var useCase: ValidateUserIdUseCase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        useCase = ValidateUserIdUseCase()
    }


    @Test
    fun `given valid userId when invoke then returns Success`() {
        val result = useCase.invoke("user_123")
        assertThat(result).isInstanceOf(ValidateUserIdUseCase.ValidationResult.Success::class.java)
    }

    @Test
    fun `given userId with letters numbers and underscores when invoke then returns Success`() {
        val result = useCase.invoke("Test_User-42")
        assertThat(result).isInstanceOf(ValidateUserIdUseCase.ValidationResult.Success::class.java)
    }


    @Test
    fun `given blank userId when invoke then returns Error`() {
        val result = useCase.invoke("   ")

        assertThat(result).isInstanceOf(ValidateUserIdUseCase.ValidationResult.Error::class.java)
        val error = result as ValidateUserIdUseCase.ValidationResult.Error
        assertThat(error.message).isEqualTo("Введите ID пользователя")
    }

    @Test
    fun `given too short userId when invoke then returns Error`() {
        val result = useCase.invoke("a")

        assertThat(result).isInstanceOf(ValidateUserIdUseCase.ValidationResult.Error::class.java)
        val error = result as ValidateUserIdUseCase.ValidationResult.Error
        assertThat(error.message).isEqualTo("ID должен содержать минимум 2 символа")
    }

    @Test
    fun `given userId with invalid characters when invoke then returns Error`() {
        val result = useCase.invoke("user@123")

        assertThat(result).isInstanceOf(ValidateUserIdUseCase.ValidationResult.Error::class.java)
        val error = result as ValidateUserIdUseCase.ValidationResult.Error
        assertThat(error.message).isEqualTo("ID может содержать только буквы, цифры, дефис и подчеркивание")
    }


    @Test
    fun `given invalid userId when invoke then returns appropriate error`() {
        val invalidCases = listOf(
            "" to "Введите ID пользователя",
            "   " to "Введите ID пользователя",
            "a" to "ID должен содержать минимум 2 символа",
            "user@name" to "ID может содержать только буквы, цифры, дефис и подчеркивание",
            "user#name" to "ID может содержать только буквы, цифры, дефис и подчеркивание",
            "user id" to "ID может содержать только буквы, цифры, дефис и подчеркивание"
        )

        invalidCases.forEach { (input, expectedMessage) ->
            val result = useCase.invoke(input)
            assertThat(result).isInstanceOf(ValidateUserIdUseCase.ValidationResult.Error::class.java)

            val error = result as ValidateUserIdUseCase.ValidationResult.Error
            assertThat(error.message).isEqualTo(expectedMessage)
        }
    }
}