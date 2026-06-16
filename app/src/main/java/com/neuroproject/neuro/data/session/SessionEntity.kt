package com.neuroproject.neuro.data.session

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    indices = [
        Index(value = ["id"]),
        Index(value = ["expedition_id"])
    ]
)
data class SessionEntity(
    @PrimaryKey
    val sessionId: Long,                     // timestamp начала в миллисекундах
    val id: String? = null,
    val expedition_id: String? = null,
    // Объективные числовые по категориям
    val objectiveCognitive: Int? = null,
    val objectivePsychological: Int? = null,
    val objectivePhysiological: Int? = null,

    // Субъективные оценки
    val subjectiveCognitive: Int? = null,
    val subjectivePsychological: Int? = null,
    val subjectivePhysiological: Int? = null,

    // Итоговые и средние
    val totalIndex: Int? = null,             // общий итог
    val averageObjective: Int? = null,       // среднее по трём объективным
    val averageSubjective: Int? = null,      // среднее по трём субъективным
    val totalCognitive: Int? = null,         // комбинированный когнитивный
    val totalPhysiological: Int? = null,
    val totalPsychological: Int? = null,

    // Метаданные сессии
    val durationMinutes: Int? = null,
    val endTime: Long? = null,               // timestamp окончания
    val sessionCategory: SessionCategory? = null,
    val comment: String? = null,

    val passingPrematurely: Boolean = false,


    // Объективные метрики (строковые)
    val objectiveFatigue: String? = null,
    val objectiveStress: String? = null,




    val isMarked: Boolean = false
)