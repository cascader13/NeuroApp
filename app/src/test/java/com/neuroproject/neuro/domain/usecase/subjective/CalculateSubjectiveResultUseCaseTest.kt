package com.neuroproject.neuro.domain.usecase.subjective

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.BlockType
import com.neuroproject.neuro.domain.model.SubjectiveAnswer
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import org.junit.Before
import org.junit.Test

class CalculateSubjectiveResultUseCaseTest {

    private lateinit var useCase: CalculateSubjectiveResultUseCase

    @Before
    fun setup() {
        useCase = CalculateSubjectiveResultUseCase()
    }

    @Test
    fun `given empty questions when invoke then returns zeros`() {
        val result = useCase(emptyList(), emptyList())
        assertThat(result.cognitiveIndex).isEqualTo(0)
        assertThat(result.emotionalIndex).isEqualTo(0)
        assertThat(result.physicalIndex).isEqualTo(0)
        assertThat(result.averageIndex).isEqualTo(0)
    }

    @Test
    fun `given questions without answers when invoke then uses default value 5`() {
        val questions = listOf(
            SubjectiveQuestion(1, "Q1", BlockType.COGNITIVE, 1, false),
            SubjectiveQuestion(2, "Q2", BlockType.COGNITIVE, 2, false)
        )
        val result = useCase(questions, emptyList())
        // default answer = 5, transformed = 5, sum = 10, minSum = 2, maxSum = 20
        // (10 - 2) / (20 - 2) * 100 = 8/18*100 = 44
        assertThat(result.cognitiveIndex).isEqualTo(44)
    }

    @Test
    fun `given reversed question when invoke then transforms answer correctly`() {
        val questions = listOf(
            SubjectiveQuestion(1, "Q1", BlockType.COGNITIVE, 1, true)
        )
        val answers = listOf(SubjectiveAnswer(1, 10))
        val result = useCase(questions, answers)
        // reversed: 11 - 10 = 1, sum = 1, minSum = 1, maxSum = 10
        // (1-1)/(10-1)*100 = 0
        assertThat(result.cognitiveIndex).isEqualTo(0)
    }

    @Test
    fun `given all max answers when invoke then returns 100`() {
        val questions = listOf(
            SubjectiveQuestion(1, "Q1", BlockType.COGNITIVE, 1, false),
            SubjectiveQuestion(2, "Q2", BlockType.COGNITIVE, 2, false)
        )
        val answers = listOf(
            SubjectiveAnswer(1, 10),
            SubjectiveAnswer(2, 10)
        )
        val result = useCase(questions, answers)
        assertThat(result.cognitiveIndex).isEqualTo(100)
    }

    @Test
    fun `given all min answers when invoke then returns 0`() {
        val questions = listOf(
            SubjectiveQuestion(1, "Q1", BlockType.EMOTIONAL, 1, false),
            SubjectiveQuestion(2, "Q2", BlockType.EMOTIONAL, 2, false)
        )
        val answers = listOf(
            SubjectiveAnswer(1, 1),
            SubjectiveAnswer(2, 1)
        )
        val result = useCase(questions, answers)
        assertThat(result.emotionalIndex).isEqualTo(0)
    }

    @Test
    fun `given mixed block types when invoke then calculates each independently`() {
        val questions = listOf(
            SubjectiveQuestion(1, "C1", BlockType.COGNITIVE, 1, false),
            SubjectiveQuestion(2, "E1", BlockType.EMOTIONAL, 2, false),
            SubjectiveQuestion(3, "P1", BlockType.PHYSICAL, 3, false)
        )
        val answers = listOf(
            SubjectiveAnswer(1, 8),
            SubjectiveAnswer(2, 6),
            SubjectiveAnswer(3, 4)
        )
        val result = useCase(questions, answers)
        // Each block has 1 question: cognitive=8->7/9*100=77, emotional=6->5/9*100=55, physical=4->3/9*100=33
        assertThat(result.cognitiveIndex).isEqualTo(77)
        assertThat(result.emotionalIndex).isEqualTo(55)
        assertThat(result.physicalIndex).isEqualTo(33)
        assertThat(result.averageIndex).isEqualTo(55)
    }

    @Test
    fun `averageIndex is average of three indices`() {
        val questions = listOf(
            SubjectiveQuestion(1, "C1", BlockType.COGNITIVE, 1, false),
            SubjectiveQuestion(2, "E1", BlockType.EMOTIONAL, 2, false),
            SubjectiveQuestion(3, "P1", BlockType.PHYSICAL, 3, false)
        )
        val answers = listOf(
            SubjectiveAnswer(1, 10),
            SubjectiveAnswer(2, 10),
            SubjectiveAnswer(3, 10)
        )
        val result = useCase(questions, answers)
        assertThat(result.averageIndex).isEqualTo(100)
    }
}
