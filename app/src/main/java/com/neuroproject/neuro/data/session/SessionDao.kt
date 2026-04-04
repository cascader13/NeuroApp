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

    // ========== Базовые CRUD ==========

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: Long): SessionEntity?

    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    // ========== Специализированные методы для обновления ==========

    /**
     * Полное обновление результатов сессии.
     * Вызывается после получения всех данных (субъективных и объективных).
     * Включает объективные и субъективные индексы, средние и общие показатели.
     */
    @Query(
        """
        UPDATE sessions SET
        objectiveFatigue = :objFatigue,
        objectiveStress = :objStress,
        objectiveCognitive = :objCog,
        objectivePsychological = :objPsy,
        objectivePhysiological = :objPhys,
        subjectiveCognitive = :subCog,
        subjectivePsychological = :subPsy,
        subjectivePhysiological = :subPhys,
        totalIndex = :total,
        averageObjective = :avgObj,
        averageSubjective = :avgSub,
        totalCognitive = :totalCog,
        totalPhysiological = :totalPhys,
        totalPsychological = :totalPsy,
        comment = :comment,
        endTime = :endTime
        WHERE sessionId = :sessionId
        """
    )
    suspend fun updateSessionResults(
        sessionId: Long,
        objFatigue: String?,
        objStress: String?,
        objCog: Int?,
        objPsy: Int?,
        objPhys: Int?,
        subCog: Int?,
        subPsy: Int?,
        subPhys: Int?,
        total: Int?,
        avgObj: Int?,
        avgSub: Int?,
        totalCog: Int?,
        totalPhys: Int?,
        totalPsy: Int?,
        comment: String?,
        endTime: Long?
    )
}