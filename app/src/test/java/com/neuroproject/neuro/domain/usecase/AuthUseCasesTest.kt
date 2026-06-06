package com.neuroproject.neuro.domain.usecase

import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {

    @Test
    fun loginUseCase_delegatesToRepository() {
        val repository = FakeAuthRepository()
        val useCase = LoginUseCase(repository)

        val result = useCase("ivan")

        assertTrue(result is Result.Success)
        assertEquals("ivan", repository.lastLogin)
    }

    @Test
    fun saveMobileIdUseCase_savesThroughRepositoryWithoutAndroidContext() {
        val repository = FakeAuthRepository()
        val useCase = SaveMobileIdUseCase(repository)

        useCase("crew_7")

        assertEquals("crew_7", repository.getSavedUserId())  // Используем метод вместо свойства
    }

    private class FakeAuthRepository : AuthRepository {
        var lastLogin: String? = null
        private var _savedUserId: String? = null

        private val history = mutableListOf<String>()

        override fun login(userId: String): Result<Unit> {
            lastLogin = userId
            saveUserId(userId)
            return Result.Success(Unit)
        }

        override fun getSavedUserId(): String? = _savedUserId
        override fun saveUserId(userId: String) {
            _savedUserId = userId
            history.remove(userId)
            history.add(0, userId)
        }

        override fun clearSavedData() {
            _savedUserId = null
        }

        override fun hasSavedData(): Boolean = _savedUserId != null
        override fun getUserIdHistory(): List<String> = history
        override fun removeFromHistory(userId: String) {
            history.remove(userId)
        }

        override fun clearHistory() {
            history.clear()
        }

        override suspend fun getUserId(): String = _savedUserId.orEmpty()
        override suspend fun getExpeditionId(): String = ""
    }
}