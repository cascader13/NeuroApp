package com.neuroproject.neuro.screens.login

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Disconnected)
    val loginState = _loginState.asStateFlow()

    val userId = mutableStateOf("")
    val errorMessage = mutableStateOf<String?>(null)

    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    init {
        loadSavedData()
    }

    fun onUserIdChange(value: String) {
        userId.value = value
        errorMessage.value = null
    }

    fun login() {
        val id = userId.value.trim()

        if (!isFormValid(id)) {
            return
        }

        _loginState.value = LoginState.Connecting
        errorMessage.value = null

        viewModelScope.launch {
            try {
                // Имитация сетевого запроса
                delay(1500)

                // В реальном приложении здесь будет вызов API
                val success = performLogin(id)

                if (success) {
                    saveUserData(id)
                    _loginState.value = LoginState.Connected
                } else {
                    _loginState.value = LoginState.Error
                    errorMessage.value = "Неверный ID пользователя"
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error
                errorMessage.value = "Ошибка подключения: ${e.message}"
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Disconnected
        }
    }

    private fun isFormValid(userId: String): Boolean {
        if (userId.isBlank()) {
            errorMessage.value = "Введите ID пользователя"
            return false
        }

        if (userId.length < 3) {
            errorMessage.value = "ID должен содержать минимум 3 символа"
            return false
        }

        if (!userId.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            errorMessage.value = "ID может содержать только буквы, цифры, дефис и подчеркивание"
            return false
        }

        return true
    }

    private suspend fun performLogin(userId: String): Boolean {
        // В реальном приложении здесь будет аутентификация через API
        // Пока что просто имитируем успешный вход
        return userId.isNotEmpty() && userId.length >= 3
    }

    private fun saveUserData(userId: String) {
        val editor = sharedPreferences.edit()
        editor.putString("saved_user_id", userId)
        editor.apply()
    }

    private fun loadSavedData() {
        val savedUserId = sharedPreferences.getString("saved_user_id", "")

        if (!savedUserId.isNullOrEmpty()) {
            userId.value = savedUserId
        }
    }

    fun clearSavedData() {
        sharedPreferences.edit().apply {
            remove("saved_user_id")
        }.apply()

        userId.value = ""
        _loginState.value = LoginState.Disconnected
    }

    fun hasSavedData(): Boolean {
        return sharedPreferences.contains("saved_user_id")
    }
}