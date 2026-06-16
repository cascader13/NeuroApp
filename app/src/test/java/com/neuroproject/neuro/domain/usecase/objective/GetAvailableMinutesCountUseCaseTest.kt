package com.neuroproject.neuro.domain.usecase.objective

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetAvailableMinutesCountUseCaseTest {

    private lateinit var useCase: GetAvailableMinutesCountUseCase
    private val repository: ObjectiveMetricsRepository = mock()

    @Before
    fun setup() {
        useCase = GetAvailableMinutesCountUseCase(repository)
    }

    @Test
    fun `invoke returns count from repository`() = runTest {
        whenever(repository.getAvailableMinutesCount(1L)).thenReturn(5)
        val result = useCase(1L)
        assertThat(result).isEqualTo(5)
    }

    @Test
    fun `invoke returns zero when no data`() = runTest {
        whenever(repository.getAvailableMinutesCount(1L)).thenReturn(0)
        val result = useCase(1L)
        assertThat(result).isEqualTo(0)
    }
}
