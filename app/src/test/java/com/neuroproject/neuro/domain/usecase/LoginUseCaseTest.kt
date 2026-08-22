package com.neuroproject.neuro.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.repository.AuthRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoginUseCaseTest {

    private lateinit var useCase: LoginUseCase
    private val authRepository: AuthRepository = mock()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        useCase = LoginUseCase(authRepository)
    }

    @Test
    fun `given valid userId when invoke then returns repository result`() {
        val userId = "user_123"
        val expectedResult = Result.Success(Unit)
        whenever(authRepository.login(userId)).thenReturn(expectedResult)
        val result = useCase.invoke(userId)
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `given repository returns Error when invoke then returns that Error`() {
        val userId = "user_123"
        val errorResult = Result.Error(Exception("Invalid credentials"), "Не удалось войти")

        whenever(authRepository.login(userId)).thenReturn(errorResult)
        val result = useCase.invoke(userId)
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat(result).isEqualTo(errorResult)
    }

    @Test
    fun `given empty userId when invoke then still delegates to repository`() {
        val emptyUserId = ""
        val repositoryResult = Result.Error(Exception("Empty userId"), "Пользователь не указан")

        whenever(authRepository.login(emptyUserId)).thenReturn(repositoryResult)
        val result = useCase.invoke(emptyUserId)
        assertThat(result).isEqualTo(repositoryResult)
    }
}