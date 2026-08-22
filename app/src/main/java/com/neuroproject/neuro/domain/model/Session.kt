// domain/model/Session.kt
package com.neuroproject.neuro.domain.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Сессия записи данных — основная единица работы приложения.
 *
 * Содержит метаданные сессии (время, длительность, категорию),
 * а также результаты расчёта показателей утомления.
 *
 * Результаты делятся на три типа:
 * - [subjectiveResult] — по ответам пользователя на опросник
 * - [objectiveResult] — по данным сенсоров (ЭЭГ, физиология)
 * - [totalResult] — интегральный показатель (среднее субъективного и объективного)
 *
 * @property sessionId внешний идентификатор (timestamp создания)
 * @property userId ID пользователя
 * @property expeditionId ID экспедиции
 * @property startTime время начала (мс)
 * @property endTime время окончания (мс), null если сессия активна
 * @property durationMinutes планируемая длительность в минутах
 * @property category категория (утро, день, вечер, техническая)
 * @property comment комментарий пользователя
 * @property passedPrematurely true, если завершена досрочно
 * @property isSynced синхронизирована ли сессия с сервером
 * @property subjectiveResult результат субъективного теста
 * @property objectiveResult результат объективного анализа
 * @property totalResult интегральный результат
 */
data class Session(
    val sessionId: Long,
    val userId: String? = null,
    val expeditionId: String? = null,
    val startTime: Long = sessionId,
    val endTime: Long? = null,
    val durationMinutes: Int = 0,
    val category: SessionCategory = SessionCategory.TECHNICAL,
    val comment: String? = null,
    val passedPrematurely: Boolean = false,
    val isSynced: Boolean = false,
    val subjectiveResult: SubjectiveResult? = null,
    val objectiveResult: ObjectiveFatigueResult? = null,
    val totalResult: TotalFatigueResult? = null
) {
    /** Дата начала сессии (dd MMM yyyy). */
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(startTime))

    /** Время начала сессии (HH:mm). */
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(startTime))

    /** Форматированная длительность ("10 мин" или "—"). */
    val formattedDuration: String
        get() = if (durationMinutes > 0) "$durationMinutes мин" else "—"

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