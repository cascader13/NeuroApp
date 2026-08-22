package com.neuroproject.neuro.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.neuroproject.neuro.domain.repository.AuthRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class SaveMobileIdUseCaseTest {

    private lateinit var useCase: SaveMobileIdUseCase
    private val authRepository: AuthRepository = mock()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        useCase = SaveMobileIdUseCase(authRepository)
    }

    @Test
    fun `given valid mobileId when invoke then calls repository saveUserId`() {
        val mobileId = "user_98765"
        useCase.invoke(mobileId)
        verify(authRepository).saveUserId(mobileId)
    }

    @Test
    fun `given empty mobileId when invoke then still calls repository`() {
        val emptyId = ""
        useCase.invoke(emptyId)
        verify(authRepository).saveUserId(emptyId)
    }

    @Test
    fun `should call repository exactly once`() {
        val mobileId = "test_user_001"
        useCase.invoke(mobileId)
        verify(authRepository).saveUserId(mobileId)
        verifyNoMoreInteractions(authRepository)   // важно: нет лишних вызовов
    }
}