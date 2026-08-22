package com.neuroproject.neuro.data.subtest

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.neuroproject.neuro.data.session.SessionEntity

/**
 * Сущность ответа на вопрос субъективного тестирования
 *
 * Хранит ответы пользователя на вопросы теста, которые заполняются после завершения сессии.
 * Каждый ответ привязан к конкретной сессии и конкретному вопросу.
 *
 * ## Связи с другими таблицами:
 * - **Внешний ключ**: [sessionId] ссылается на [SessionEntity]
 * - **Каскадное удаление**: при удалении сессии все связанные ответы удаляются автоматически
 * - **Индекс**: по [sessionId] для быстрого поиска ответов сессии
 *
 * ## Жизненный цикл:
 * 1. Пользователь завершает сессию записи данных
 * 2. Отображается форма с вопросами (из [SubjectiveQuestionEntity])
 * 3. Пользователь выбирает ответы (1-10) на каждый вопрос
 * 4. Ответы сохраняются в эту таблицу с привязкой к [sessionId]
 * 5. После синхронизации с сервером ответы удаляются вместе с сессией
 *
 * @property id Уникальный идентификатор ответа (автогенерация)
 * @property sessionId ID сессии, к которой относится ответ
 * @property questionId ID вопроса из [SubjectiveQuestionEntity]
 * @property value Ответ пользователя (1-10)
 *
 * @see SubjectiveQuestionEntity
 * @see SessionEntity
 */
@Entity(
    tableName = "subjective_answers",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE  // При удалении сессии удаляются и ответы
        )
    ],
    indices = [Index("sessionId")]  // Индекс для быстрого поиска по сессии
)
data class SubjectiveAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,      // К какой сессии относится
    val questionId: Int,      // Какой вопрос
    val value: Int            // Ответ (1-10)
)