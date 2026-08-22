package com.neuroproject.neuro.domain.usecase.expedition

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.ExpeditionResult
import com.neuroproject.neuro.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CheckExpeditionIdUseCaseTest {

    private lateinit var useCase: CheckExpeditionIdUseCase
    private val authRepository: AuthRepository = mock()

    @Before
    fun setup() {
        useCase = CheckExpeditionIdUseCase(authRepository)
    }

    @Test
    fun `given blank expeditionId when invoke then returns NotSet`() = runTest {
        whenever(authRepository.getExpeditionId()).thenReturn("")
        val result = useCase()
        assertThat(result).isEqualTo(ExpeditionResult.NotSet)
    }

    @Test
    fun `given valid expeditionId when invoke then returns Success`() = runTest {
        whenever(authRepository.getExpeditionId()).thenReturn("EXP-123")
        val result = useCase()
        assertThat(result).isInstanceOf(ExpeditionResult.Success::class.java)
        assertThat((result as ExpeditionResult.Success).expeditionId).isEqualTo("EXP-123")
    }

    @Test
    fun `given exception when invoke then returns Error`() = runTest {
        whenever(authRepository.getExpeditionId()).thenThrow(RuntimeException("DB error"))
        val result = useCase()
        assertThat(result).isInstanceOf(ExpeditionResult.Error::class.java)
        assertThat((result as ExpeditionResult.Error).message).isEqualTo("DB error")
    }
}
