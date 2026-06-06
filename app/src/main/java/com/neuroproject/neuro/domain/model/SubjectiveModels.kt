package com.neuroproject.neuro.domain.model

data class SubjectiveQuestion(
    val id: Int,
    val text: String,
    val blockType: com.neuroproject.neuro.domain.model.BlockType,
    val displayOrder: Int,
    val isReversed: Boolean
)

enum class BlockType {
    COGNITIVE, EMOTIONAL, PHYSICAL
}

data class SubjectiveAnswer(
    val questionId: Int,
    val value: Int  // 1-10
)

data class SubjectiveResult(
    val cognitiveIndex: Int,    // 0-100
    val emotionalIndex: Int,    // 0-100
    val physicalIndex: Int,     // 0-100
    val averageIndex: Int       // среднее трёх
)