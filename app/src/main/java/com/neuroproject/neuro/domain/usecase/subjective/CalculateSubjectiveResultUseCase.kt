package com.neuroproject.neuro.domain.usecase.subjective

import com.neuroproject.neuro.domain.model.BlockType
import com.neuroproject.neuro.domain.model.SubjectiveAnswer
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.model.SubjectiveResult
import javax.inject.Inject

/**
 * Use case расчёта субъективных показателей утомления.
 *
 * Преобразует ответы пользователя на опросник в числовые индексы (0-100)
 * по трём блокам: когнитивный, эмоциональный, физический.
 *
 * Алгоритм:
 * 1. Группирует вопросы по [BlockType]
 * 2. Для каждого вопроса применяет трансформацию: если вопрос обратный
 *    ([SubjectiveQuestion.isReversed]), значение инвертируется (11 - ответ)
 * 3. Суммирует ответы и нормализует к диапазону 0-100
 * 4. Ответы по умолчанию (нет ответа) = 5
 */
class CalculateSubjectiveResultUseCase @Inject constructor() {

    /**
     * Рассчитывает субъективные показатели.
     *
     * @param questions список вопросов опросника.
     * @param answers ответы пользователя.
     * @return [SubjectiveResult] с индексами по блокам и средним.
     */
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