package com.neuroproject.neuro.domain.usecase.objective

import com.neuroproject.neuro.domain.model.FatigueResult
import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SaveMinuteFatigueResultUseCaseTest {

    private lateinit var useCase: SaveMinuteFatigueResultUseCase
    private val repository: ObjectiveMetricsRepository = mock()

    @Before
    fun setup() {
        useCase = SaveMinuteFatigueResultUseCase(repository)
    }

    @Test
    fun `invoke saves result to repository`() = runTest {
        val result = FatigueResult(minuteIndex = 0, cognitive = 50f, physiological = 60f, psychological = 70f, sessionId = 1L)
        useCase(result)
        verify(repository).saveMinuteFatigueResult(result)
    }
}
