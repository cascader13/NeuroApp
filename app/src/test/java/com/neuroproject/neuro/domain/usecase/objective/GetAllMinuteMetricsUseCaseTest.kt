package com.neuroproject.neuro.domain.usecase.objective

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.MinuteFatigueData
import com.neuroproject.neuro.domain.model.CognitiveMetrics
import com.neuroproject.neuro.domain.model.PhysiologicalMetrics
import com.neuroproject.neuro.domain.model.PsychologicalMetrics
import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetAllMinuteMetricsUseCaseTest {

    private lateinit var useCase: GetAllMinuteMetricsUseCase
    private val repository: ObjectiveMetricsRepository = mock()

    @Before
    fun setup() {
        useCase = GetAllMinuteMetricsUseCase(repository)
    }

    @Test
    fun `invoke returns list from repository`() = runTest {
        val data = listOf(
            MinuteFatigueData(0, CognitiveMetrics(0.5f, 0.5f, 0.5f, 0.5f), PhysiologicalMetrics(0.5f, 0.5f, 0.5f, 0.5f), PsychologicalMetrics(0.5f, 0.5f, 0.5f, 0.5f))
        )
        whenever(repository.getAllMinuteMetrics(1L)).thenReturn(data)
        val result = useCase(1L)
        assertThat(result).hasSize(1)
        assertThat(result[0].minuteIndex).isEqualTo(0)
    }
}
