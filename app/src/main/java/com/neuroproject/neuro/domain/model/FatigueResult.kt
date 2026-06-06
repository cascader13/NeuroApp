package com.neuroproject.neuro.domain.model

data class FatigueSummary(
    val subjective: SubjectiveResult,
    val objective: ObjectiveFatigueResult,
    val total: TotalFatigueResult
)

data class TotalFatigueResult(
    val cognitiveIndex: Int,
    val psychologicalIndex: Int,
    val physiologicalIndex: Int,
    val averageIndex: Int
)