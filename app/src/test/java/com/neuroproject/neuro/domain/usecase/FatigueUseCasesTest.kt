package com.neuroproject.neuro.domain.usecase

import com.neuroproject.neuro.domain.model.BlockType
import com.neuroproject.neuro.domain.model.ObjectiveFatigueResult
import com.neuroproject.neuro.domain.model.SubjectiveAnswer
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.model.SubjectiveResult
import com.neuroproject.neuro.domain.usecase.fatigue.CalculateTotalFatigueUseCase
import com.neuroproject.neuro.domain.usecase.subjective.CalculateSubjectiveResultUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class FatigueUseCasesTest {

    @Test
    fun calculateSubjectiveResult_mapsAnswersToZeroHundredIndexes() {
        // Ответ 10 для обычного вопроса даёт максимум, ответ 1 для reverse-вопроса тоже превращается в максимум.
        val questions = listOf(
            SubjectiveQuestion(1, "Когнитивная усталость", BlockType.COGNITIVE, 1, isReversed = false),
            SubjectiveQuestion(2, "Эмоциональный ресурс", BlockType.EMOTIONAL, 2, isReversed = true),
            SubjectiveQuestion(3, "Физическая усталость", BlockType.PHYSICAL, 3, isReversed = false)
        )
        val answers = listOf(
            SubjectiveAnswer(1, 10),
            SubjectiveAnswer(2, 1),
            SubjectiveAnswer(3, 5)
        )

        val result = CalculateSubjectiveResultUseCase()(questions, answers)

        assertEquals(100, result.cognitiveIndex)
        assertEquals(100, result.emotionalIndex)
        assertEquals(44, result.physicalIndex)
        assertEquals(81, result.averageIndex)
    }

    @Test
    fun calculateTotalFatigue_averagesSubjectiveAndObjectiveBranches() {
        // Итоговый индекс — простое среднее субъективных и объективных индексов по каждой ветке.
        val subjective = SubjectiveResult(
            cognitiveIndex = 80,
            emotionalIndex = 60,
            physicalIndex = 40,
            averageIndex = 60
        )
        val objective = ObjectiveFatigueResult(
            cognitiveIndex = 40,
            psychologicalIndex = 80,
            physiologicalIndex = 60,
            averageIndex = 60,
            fatigueLevel = "Средний",
            stressLevel = "Низкий"
        )

        val result = CalculateTotalFatigueUseCase()(subjective, objective)

        assertEquals(60, result.total.cognitiveIndex)
        assertEquals(70, result.total.psychologicalIndex)
        assertEquals(50, result.total.physiologicalIndex)
        assertEquals(60, result.total.averageIndex)
    }
}
