package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.Result

interface AuthRepository {
    fun login(userId: String): Result<Unit>
    fun getSavedUserId(): String?
    fun saveUserId(userId: String)
    fun clearSavedData()
    fun hasSavedData(): Boolean
    fun getUserIdHistory(): List<String>
    fun removeFromHistory(userId: String)
    fun clearHistory()
    suspend fun getUserId(): String
    suspend fun getExpeditionId(): String

    suspend fun saveExpeditionId(expeditionId: String)
}