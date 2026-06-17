package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.Result

/**
 * Репозиторий аутентификации и управления пользовательскими данными.
 *
 * Отвечает за:
 * - вход/выход пользователя
 * - хранение и извлечение userId
 * - управление историей пользователей
 * - хранение expedition ID (идентификатор экспедиции)
 */
interface AuthRepository {
    /**
     * Выполняет вход пользователя по ID.
     *
     * @param userId идентификатор пользователя.
     * @return [Result.Success] при успешном входе, [Result.Error] при ошибке.
     */
    fun login(userId: String): Result<Unit>

    /** Возвращает сохранённый ID пользователя или null, если вход не выполнялся. */
    fun getSavedUserId(): String?

    /** Сохраняет ID пользователя. */
    fun saveUserId(userId: String)

    /** Очищает все сохранённые данные пользователя. */
    fun clearSavedData()

    /** Проверяет, есть ли сохранённые данные пользователя. */
    fun hasSavedData(): Boolean

    /** Возвращает список ранее входивших пользователей. */
    fun getUserIdHistory(): List<String>

    /** Удаляет пользователя из истории входов. */
    fun removeFromHistory(userId: String)

    /** Очищает всю историю входов. */
    fun clearHistory()

    /**
     * Возвращает ID текущего пользователя.
     *
     * @throws IllegalStateException если пользователь не вошёл в систему.
     */
    suspend fun getUserId(): String

    /**
     * Возвращает ID текущей экспедиции.
     *
     * @return expedition ID или пустую строку, если не задан.
     */
    suspend fun getExpeditionId(): String

    /**
     * Сохраняет ID экспедиции.
     *
     * @param expeditionId идентификатор экспедиции.
     */
    suspend fun saveExpeditionId(expeditionId: String)

    /** Сохраняет имя подключённого устройства. */
    fun saveDeviceName(name: String)

    /** Возвращает имя подключённого устройства. */
    fun getDeviceName(): String
}