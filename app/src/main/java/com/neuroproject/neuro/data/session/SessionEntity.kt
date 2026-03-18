package com.neuroproject.neuro.data.session

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(

    @PrimaryKey
    val sessionId: Long,  // timestamp старта

    // Объективные индексы
    val objectiveFatigue: String? = null,
    val objectiveStress: String? = null,

    // Субъективные индексы
    val subjectiveCognitive: Int? = null,
    val subjectiveEmotional: Int? = null,
    val subjectivePhysical: Int? = null,

    // Конечный результат
    val totalIndex: Int? = null,


    // Комментарий с субъективного тестирования
    val comment: String? = null
)