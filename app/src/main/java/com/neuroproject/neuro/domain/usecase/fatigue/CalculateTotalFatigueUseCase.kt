package com.neuroproject.neuro.domain.usecase.fatigue

import com.neuroproject.neuro.domain.model.FatigueSummary
import com.neuroproject.neuro.domain.model.ObjectiveFatigueResult
import com.neuroproject.neuro.domain.model.SubjectiveResult
import com.neuroproject.neuro.domain.model.TotalFatigueResult
import javax.inject.Inject

class CalculateTotalFatigueUseCase @Inject constructor() {

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