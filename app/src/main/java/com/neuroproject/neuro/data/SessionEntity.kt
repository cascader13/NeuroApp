package com.neuroproject.neuro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(

    @PrimaryKey
    val sessionId: Long,  // timestamp старта

    // Объективные индексы
    val objectiveCognitive: Int? = null,
    val objectiveEmotional: Int? = null,
    val objectivePhysical: Int? = null,

    // Субъективные индексы
    val subjectiveCognitive: Int? = null,
    val subjectiveEmotional: Int? = null,
    val subjectivePhysical: Int? = null,

    // Конечный результат
    val totalIndex: Int? = null,


    // Комментарий с субъективного тестирования
    val comment: String? = null
)