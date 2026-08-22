package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.domain.model.SubjectiveAnswer
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SaveAnswersUseCaseTest {

    private lateinit var useCase: SaveAnswersUseCase
    private val repository: SubjectiveTestRepository = mock()

    @Before
    fun setup() {
        useCase = SaveAnswersUseCase(repository)
    }

    @Test
    fun `invoke saves answers to repository`() = runTest {
        val answers = listOf(
            SubjectiveAnswer(1, 5),
            SubjectiveAnswer(2, 8)
        )
        useCase(1L, answers)
        verify(repository).saveAnswers(1L, answers)
    }
}
