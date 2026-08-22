package com.neuroproject.neuro.data.repository

import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.data.datasource.LocalAuthDataSource
import com.neuroproject.neuro.domain.repository.AuthRepository
import javax.inject.Inject


/**
 * Реализация репозитория авторизации на основе локального хранилища.
 */
class AuthRepositoryImpl @Inject constructor(
    private val localDataSource: LocalAuthDataSource
) : AuthRepository {

    override fun login(userId: String): Result<Unit> {
        return try {
            if (userId.isNotEmpty() && userId.length >= 2) {
                localDataSource.saveUserId(userId)
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Неверный ID пользователя"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getSavedUserId(): String? = localDataSource.getSavedUserId()

    override fun saveUserId(userId: String) = localDataSource.saveUserId(userId)

    override fun clearSavedData() = localDataSource.clearSavedData()

    override fun hasSavedData(): Boolean = localDataSource.hasSavedData()

    override fun getUserIdHistory(): List<String> = localDataSource.getUserIdHistory()

    override fun removeFromHistory(userId: String) = localDataSource.removeFromHistory(userId)

    override fun clearHistory() = localDataSource.clearHistory()

    override suspend fun getUserId(): String{
        return localDataSource.getSavedUserId() ?: ""
    }

    override suspend fun getExpeditionId(): String = localDataSource.getExpeditionId()

    override suspend fun saveExpeditionId(expeditionId: String) {
        localDataSource.saveExpeditionId(expeditionId)
    }

    override fun saveDeviceName(name: String) {
        localDataSource.saveDeviceName(name)
    }

    override fun getDeviceName(): String = localDataSource.getDeviceName()
}