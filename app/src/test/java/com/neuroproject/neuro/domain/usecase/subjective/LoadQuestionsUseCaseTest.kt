package com.neuroproject.neuro.domain.usecase.subjective

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.model.BlockType
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify

class LoadQuestionsUseCaseTest {

    private lateinit var useCase: LoadQuestionsUseCase
    private val repository: SubjectiveTestRepository = mock()

    @Before
    fun setup() {
        useCase = LoadQuestionsUseCase(repository)
    }

    @Test
    fun `given questions exist when invoke then returns them`() = runTest {
        val questions = listOf(
            SubjectiveQuestion(1, "Q1", BlockType.COGNITIVE, 1, false)
        )
        whenever(repository.getQuestions()).thenReturn(questions)

        val result = useCase()
        assertThat(result).hasSize(1)
        assertThat(result[0].text).isEqualTo("Q1")
    }

    @Test
    fun `given no questions when invoke then imports and returns`() = runTest {
        whenever(repository.getQuestions())
            .thenReturn(emptyList())
            .thenReturn(listOf(SubjectiveQuestion(1, "Q1", BlockType.COGNITIVE, 1, false)))

        val result = useCase()
        verify(repository).importQuestions()
        assertThat(result).hasSize(1)
    }
}
