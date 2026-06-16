package com.neuroproject.neuro.domain.usecase.expedition

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.ExpeditionResult
import com.neuroproject.neuro.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SaveExpeditionIdUseCaseTest {

    private lateinit var useCase: SaveExpeditionIdUseCase
    private val authRepository: AuthRepository = mock()

    @Before
    fun setup() {
        useCase = SaveExpeditionIdUseCase(authRepository)
    }

    @Test
    fun `given blank expeditionId when invoke then returns Error`() = runTest {
        val result = useCase("")
        assertThat(result).isInstanceOf(ExpeditionResult.Error::class.java)
        assertThat((result as ExpeditionResult.Error).message).contains("пустым")
    }

    @Test
    fun `given whitespace expeditionId when invoke then returns Error`() = runTest {
        val result = useCase("   ")
        assertThat(result).isInstanceOf(ExpeditionResult.Error::class.java)
    }

    @Test
    fun `given valid expeditionId when invoke then saves and returns Success`() = runTest {
        val result = useCase("EXP-456")
        assertThat(result).isInstanceOf(ExpeditionResult.Success::class.java)
        assertThat((result as ExpeditionResult.Success).expeditionId).isEqualTo("EXP-456")
        verify(authRepository).saveExpeditionId("EXP-456")
    }

    @Test
    fun `given exception when invoke then returns Error`() = runTest {
        whenever(authRepository.saveExpeditionId("EXP-789")).thenThrow(RuntimeException("Save failed"))
        val result = useCase("EXP-789")
        assertThat(result).isInstanceOf(ExpeditionResult.Error::class.java)
        assertThat((result as ExpeditionResult.Error).message).isEqualTo("Save failed")
    }
}
