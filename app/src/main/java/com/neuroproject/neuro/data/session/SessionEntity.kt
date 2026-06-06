package com.neuroproject.neuro.data.session

import androidx.room.Entity
import androidx.room.PrimaryKey



/**
 * Сущность сессии для хранения в базе данных Room
 *
 * Представляет одну сессию записи данных с нейро-гарнитуры. Содержит как
 * объективные показатели (рассчитанные алгоритмами на основе данных устройства),
 * так и субъективные оценки пользователя (введенные после сессии).
 *
 * ## Жизненный цикл сессии:
 * 1. **Создание** - при начале записи создается запись с [sessionId] (timestamp)
 * 2. **Обновление** - после завершения сессии заполняются индексы и комментарий
 * 3. **Синхронизация** - данные отправляются на сервер
 * 4. **Удаление** - после успешной синхронизации запись удаляется
 *
 * ## Связи с другими таблицами:
 * - Связана с метриками через [sessionId] (внешний ключ в таблицах метрик)
 * - Используется для группировки данных по сессиям
 *
 * @property sessionId Уникальный идентификатор сессии (timestamp начала сессии в миллисекундах)
 * @property objectiveFatigue Объективный уровень утомления (рассчитанный алгоритмом)
 * @property objectiveStress Объективный уровень стресса (рассчитанный алгоритмом)
 * @property subjectiveCognitive Субъективная оценка когнитивного состояния (шкала 1-10)
 * @property subjectiveEmotional Субъективная оценка эмоционального состояния (шкала 1-10)
 * @property subjectivePhysical Субъективная оценка физического состояния (шкала 1-10)
 * @property totalIndex Итоговый интегральный показатель состояния
 * @property comment Комментарий пользователя о сессии
 *
 * @author Neuro Project Team
 * @since 1.0
 * @see SessionDao
 */
@Entity(tableName = "sessions")
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