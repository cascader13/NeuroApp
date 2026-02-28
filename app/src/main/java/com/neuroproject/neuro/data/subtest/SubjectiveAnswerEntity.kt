package com.neuroproject.neuro.data.subtest

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.neuroproject.neuro.data.SessionEntity

@Entity(
    tableName = "subjective_answers",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SubjectiveAnswerEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sessionId: java.sql.Timestamp,   // К какой сессии относится
    val questionId: Int,   // Какой вопрос
    val value: Int,        // Ответ 1-10
)