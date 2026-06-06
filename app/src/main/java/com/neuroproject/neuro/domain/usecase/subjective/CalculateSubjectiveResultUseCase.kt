package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.domain.model.BlockType
import com.neuroproject.neuro.domain.model.SubjectiveAnswer
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.model.SubjectiveResult
import javax.inject.Inject

class CalculateSubjectiveResultUseCase @Inject constructor() {

    operator fun invoke(
        questions: List<SubjectiveQuestion>,
        answers: List<SubjectiveAnswer>
    ): SubjectiveResult {
        val answersMap = answers.associateBy { it.questionId }

        fun calculateIndex(blockType: BlockType): Int {
            val blockQuestions = questions.filter { it.blockType == blockType }
            if (blockQuestions.isEmpty()) return 0

            var sum = 0
            for (question in blockQuestions) {
                val answer = answersMap[question.id]?.value ?: 5
                val transformedValue = if (question.isReversed) 11 - answer else answer
                sum += transformedValue
            }

            val minSum = blockQuestions.size  // все ответы = 1
            val maxSum = blockQuestions.size * 10  // все ответы = 10

            return ((sum - minSum).toDouble() / (maxSum - minSum) * 100).toInt()
                .coerceIn(0, 100)
        }

        return SubjectiveResult(
            cognitiveIndex = calculateIndex(BlockType.COGNITIVE),
            emotionalIndex = calculateIndex(BlockType.EMOTIONAL),
            physicalIndex = calculateIndex(BlockType.PHYSICAL),
            averageIndex = listOf(
                calculateIndex(BlockType.COGNITIVE),
                calculateIndex(BlockType.EMOTIONAL),
                calculateIndex(BlockType.PHYSICAL)
            ).average().toInt()
        )
    }
}