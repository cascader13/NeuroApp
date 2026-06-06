// domain/model/Session.kt
package com.neuroproject.neuro.domain.model

import java.text.SimpleDateFormat
import java.util.*

data class Session(
    val sessionId: Long,  // основной идентификатор (timestamp)
    val userId: String? = null,
    val expeditionId: String? = null,
    val startTime: Long = sessionId,
    val endTime: Long? = null,
    val durationMinutes: Int = 0,
    val category: SessionCategory = SessionCategory.TECHNICAL,
    val comment: String? = null,
    val passedPrematurely: Boolean = false,
    val isSynced: Boolean = false,

    // Результаты (вложенные объекты)
    val subjectiveResult: SubjectiveResult? = null,
    val objectiveResult: ObjectiveFatigueResult? = null,
    val totalResult: TotalFatigueResult? = null
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(startTime))

    val formattedTime: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(startTime))

    val formattedDuration: String
        get() = if (durationMinutes > 0) "$durationMinutes мин" else "—"

    // Компьютед проперти для обратной совместимости
    val objectiveCognitive: Int? get() = objectiveResult?.cognitiveIndex
    val objectivePsychological: Int? get() = objectiveResult?.psychologicalIndex
    val objectivePhysiological: Int? get() = objectiveResult?.physiologicalIndex
    val averageObjective: Int? get() = objectiveResult?.averageIndex
    val objectiveFatigue: String? get() = objectiveResult?.fatigueLevel
    val objectiveStress: String? get() = objectiveResult?.stressLevel

    val subjectiveCognitive: Int? get() = subjectiveResult?.cognitiveIndex
    val subjectivePsychological: Int? get() = subjectiveResult?.emotionalIndex
    val subjectivePhysiological: Int? get() = subjectiveResult?.physicalIndex
    val averageSubjective: Int? get() = subjectiveResult?.averageIndex

    val totalCognitive: Int? get() = totalResult?.cognitiveIndex
    val totalPsychological: Int? get() = totalResult?.psychologicalIndex
    val totalPhysiological: Int? get() = totalResult?.physiologicalIndex
    val totalIndex: Int? get() = totalResult?.averageIndex
}