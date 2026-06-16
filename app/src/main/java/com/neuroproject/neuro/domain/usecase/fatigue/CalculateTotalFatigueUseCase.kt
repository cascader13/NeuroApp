package com.neuroproject.neuro.domain.usecase.fatigue

import com.neuroproject.neuro.domain.model.FatigueSummary
import com.neuroproject.neuro.domain.model.ObjectiveFatigueResult
import com.neuroproject.neuro.domain.model.SubjectiveResult
import com.neuroproject.neuro.domain.model.TotalFatigueResult
import javax.inject.Inject

/**
 * Use case расчёта интегрального показателя утомления.
 *
 * Объединяет субъективные ([SubjectiveResult]) и объективные
 * ([ObjectiveFatigueResult]) показатели в единый [FatigueSummary].
 *
 * Интегральный показатель рассчитывается как среднее арифметическое
 * субъективного и объективного индексов по каждому направлению.
 */
class CalculateTotalFatigueUseCase @Inject constructor() {

    /**
     * Рассчитывает интегральный показатель утомления.
     *
     * @param subjective результат субъективного теста.
     * @param objective результат объективного анализа.
     * @return [FatigueSummary] со всеми тремя типами результатов.
     */
    operator fun invoke(
        subjective: SubjectiveResult,
        objective: ObjectiveFatigueResult
    ): FatigueSummary {
        return FatigueSummary(
            subjective = subjective,
            objective = objective,
            total = TotalFatigueResult(
                cognitiveIndex = (subjective.cognitiveIndex + objective.cognitiveIndex) / 2,
                psychologicalIndex = (subjective.emotionalIndex + objective.psychologicalIndex) / 2,
                physiologicalIndex = (subjective.physicalIndex + objective.physiologicalIndex) / 2,
                averageIndex = (subjective.averageIndex + objective.averageIndex) / 2
            )
        )
    }
}