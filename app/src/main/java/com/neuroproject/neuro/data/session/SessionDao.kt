package com.neuroproject.neuro.data.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO (Data Access Object) для работы с сессиями в базе данных Room
 *
 * Предоставляет методы для выполнения CRUD операций над таблицей sessions.
 * Используется для управления сессиями записи данных с нейро-гарнитуры.
 *
 * ## Основные операции:
 * - **Создание** - [insert] при начале сессии
 * - **Обновление** - [update] и [updateIndexes] для заполнения данных после сессии
 * - **Чтение** - [getSession] для получения информации о сессии
 * - **Удаление** - [deleteSession] после синхронизации с сервером
 *
 * ## Транзакции:
 * Все методы являются suspend функциями и должны вызываться из корутины.
 * Room автоматически выполняет операции в транзакции при необходимости.
 *
 * ## Пример использования:
 * ```kotlin
 * class SessionRepository @Inject constructor(
 *     private val sessionDao: SessionDao
 * ) {
 *     suspend fun createSession(): Long {
 *         val sessionId = System.currentTimeMillis()
 *         sessionDao.insert(SessionEntity(sessionId))
 *         return sessionId
 *     }
 *
 *     suspend fun completeSession(
 *         sessionId: Long,
 *         fatigue: String,
 *         stress: String,
 *         cognitive: Int,
 *         emotional: Int,
 *         physical: Int
 *     ) {
 *         val total = (cognitive + emotional + physical) / 3
 *         sessionDao.updateIndexes(
 *             sessionId, fatigue, stress,
 *             cognitive, emotional, physical, total
 *         )
 *     }
 * }
 * ```
 * @see SessionEntity
 */
@Dao
interface SessionDao {

    /**
     * Вставка новой сессии в базу данных
     *
     * Вызывается в момент начала записи данных с устройства.
     * Использует [OnConflictStrategy.ABORT] - если сессия с таким ID уже существует,
     * операция прерывается и выбрасывается исключение.
     *
     * @param session Объект сессии для вставки
     *
     * @throws androidx.room.ForeignKeyViolationException при нарушении внешних ключей
     * @throws androidx.room.ConflictResolutionException при конфликте первичного ключа
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: SessionEntity)

    /**
     * Обновление существующей сессии
     *
     * Обновляет все поля сессии по ее первичному ключу.
     * Используется для:
     * - Добавления комментария после сессии
     * - Корректировки сохраненных данных
     * - Обновления индексов в процессе обработки
     *
     * @param session Объект сессии с обновленными полями
     *
     * @throws IllegalArgumentException если сессия с таким ID не найдена
     */
    @Update
    suspend fun update(session: SessionEntity)

    /**
     * Получение сессии по ID
     *
     * Извлекает полную информацию о сессии для:
     * - Отображения в UI истории сессий
     * - Подготовки данных для отправки на сервер
     * - Валидации наличия сессии перед операциями
     *
     * @param sessionId Уникальный идентификатор сессии (timestamp начала)
     * @return Объект сессии или null если сессия не найдена
     */
    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: Long): SessionEntity?

    /**
     * Обновление индексов сессии
     *
     * Специализированный метод для обновления только оценочных показателей.
     * Используется после завершения сессии для заполнения:
     * - Объективных индексов (утомление, стресс)
     * - Субъективных оценок (когнитивные, эмоциональные, физические)
     * - Итогового интегрального показателя
     *
     * ## SQL запрос:
     * ```sql
     * UPDATE sessions SET
     * objectiveFatigue = :objFatigue,
     * objectiveStress = :objStress,
     * subjectiveCognitive = :subCog,
     * subjectiveEmotional = :subEmo,
     * subjectivePhysical = :subPhys,
     * totalIndex = :total
     * WHERE sessionId = :sessionId
     * ```
     *
     * @param sessionId ID сессии для обновления
     * @param objFatigue Объективный уровень утомления
     * @param objStress Объективный уровень стресса
     * @param subCog Субъективная оценка когнитивного состояния (1-10)
     * @param subEmo Субъективная оценка эмоционального состояния (1-10)
     * @param subPhys Субъективная оценка физического состояния (1-10)
     * @param total Итоговый интегральный показатель (1-10)
     *
     * @return количество обновленных строк (0 или 1)
     */
    @Query(
        """
        UPDATE sessions SET 
        objectiveFatigue = :objFatigue,
        objectiveStress = :objStress,
        subjectiveCognitive = :subCog,
        subjectiveEmotional = :subEmo,
        subjectivePhysical = :subPhys,
        totalIndex = :total
        WHERE sessionId = :sessionId
    """
    )
    suspend fun updateIndexes(
        sessionId: Long,
        objFatigue: String?,
        objStress: String?,
        subCog: Int?,
        subEmo: Int?,
        subPhys: Int?,
        total: Int?
    )

    /**
     * Обновление комментария сессии
     *
     * Специализированный метод для добавления или изменения комментария пользователя.
     * Комментарий вводится после сессии и содержит субъективные заметки.
     *
     * @param sessionId ID сессии для обновления
     * @param comment Текст комментария (может быть null для удаления)
     *
     * @return количество обновленных строк (0 или 1)
     */
    @Query(
        """
        UPDATE sessions SET 
        comment = :comment
        WHERE sessionId = :sessionId
    """
    )
    suspend fun updateComment(sessionId: Long, comment: String?)

    /**
     * Удаление сессии
     *
     * Удаляет запись о сессии и все связанные с ней метрики
     * (при наличии каскадного удаления в связях таблиц).
     *
     * Используется для:
     * - Очистки локальной БД после успешной синхронизации с сервером
     * - Удаления тестовых или ошибочных сессий
     * - Освобождения места в хранилище
     *
     * ## Внимание:
     * Операция необратима. Рекомендуется предварительно синхронизировать
     * данные с сервером перед удалением.
     *
     * @param sessionId ID сессии для удаления
     *
     * @return количество удаленных строк (0 или 1)
     */
    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}