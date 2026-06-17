package com.neuroproject.neuro.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalAuthDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SAVED_USER_ID = "saved_user_id"
        private const val KEY_SAVED_MOBILE_ID = "saved_mobile_id"
        private const val KEY_EXPEDITION_ID = "saved_expedition_id"
        private const val KEY_USER_ID_HISTORY = "user_id_history"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val MAX_HISTORY_SIZE = 10
    }

    fun getSavedUserId(): String? = sharedPreferences.getString(KEY_SAVED_USER_ID, null)

    fun saveUserId(userId: String) {
        // Сохраняем в оба ключа: saved_user_id — основной, saved_mobile_id — legacy-ключ старого кода.
        sharedPreferences.edit()
            .putString(KEY_SAVED_USER_ID, userId)
            .putString(KEY_SAVED_MOBILE_ID, userId)
            .apply()
        addToHistory(userId)
    }

    fun clearSavedData() {
        sharedPreferences.edit().remove(KEY_SAVED_USER_ID).apply()
    }

    fun hasSavedData(): Boolean = sharedPreferences.contains(KEY_SAVED_USER_ID)


    fun getUserIdHistory(): List<String> {
        val json = sharedPreferences.getString(KEY_USER_ID_HISTORY, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addToHistory(userId: String) {
        val currentHistory = getUserIdHistory().toMutableList()

        // Удаляем если уже есть
        currentHistory.remove(userId)

        // Добавляем в начало
        currentHistory.add(0, userId)

        // Ограничиваем размер
        while (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.size - 1)
        }

        val json = gson.toJson(currentHistory)
        sharedPreferences.edit().putString(KEY_USER_ID_HISTORY, json).apply()
    }

    fun removeFromHistory(userId: String) {
        val currentHistory = getUserIdHistory().toMutableList()
        currentHistory.remove(userId)
        val json = gson.toJson(currentHistory)
        sharedPreferences.edit().putString(KEY_USER_ID_HISTORY, json).apply()
    }

    fun getExpeditionId(): String = sharedPreferences.getString(KEY_EXPEDITION_ID, "") ?: ""

    fun saveExpeditionId(expeditionId: String) = sharedPreferences.edit().putString(KEY_EXPEDITION_ID, expeditionId).apply()

    fun clearHistory() {
        sharedPreferences.edit().remove(KEY_USER_ID_HISTORY).apply()
    }

    fun saveDeviceName(name: String) {
        sharedPreferences.edit().putString(KEY_DEVICE_NAME, name).apply()
    }

    fun getDeviceName(): String = sharedPreferences.getString(KEY_DEVICE_NAME, "") ?: ""
}