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
// буду работать здесь
@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Disconnected)
    val loginState = _loginState.asStateFlow()

    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val showPassword = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    init {
        loadSavedData()
    }

    fun onUsernameChange(value: String) {
        username.value = value
        errorMessage.value = null
    }

    fun onPasswordChange(value: String) {
        password.value = value
        errorMessage.value = null
    }

    fun togglePasswordVisibility() {
        showPassword.value = !showPassword.value
    }

    fun login() {
        val user = username.value.trim()
        val pass = password.value.trim()

        if (!isFormValid(user, pass)) {
            return
        }

        _loginState.value = LoginState.Connecting
        errorMessage.value = null

        viewModelScope.launch {
            try {
                // Имитация сетевого запроса
                delay(1500)

                // В реальном приложении здесь будет вызов API
                val success = performLogin(user, pass)

                if (success) {
                    saveUserData(user, pass)
                    _loginState.value = LoginState.Connected
                } else {
                    _loginState.value = LoginState.Error
                    errorMessage.value = "Неверный логин или пароль"
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

    private fun isFormValid(username: String, password: String): Boolean {
        if (username.isBlank()) {
            errorMessage.value = "Введите логин"
            return false
        }

        if (password.isBlank()) {
            errorMessage.value = "Введите пароль"
            return false
        }

        if (username.length < 3) {
            errorMessage.value = "Логин должен содержать минимум 3 символа"
            return false
        }

        if (password.length != 8) {
            errorMessage.value = "Пароль должен содержать ровно 8 символов"
            return false
        }

        if (!username.matches(Regex("^[a-zA-Z0-9]+$"))) {
            errorMessage.value = "Логин может содержать только буквы и цифры"
            return false
        }

        return true
    }

    private suspend fun performLogin(username: String, password: String): Boolean {
        // В реальном приложении здесь будет аутентификация через API
        // Пока что просто имитируем успешный вход
        return username.isNotEmpty() && password.length == 8
    }

    private fun saveUserData(username: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("saved_username", username)
        // В реальном приложении пароль должен быть зашифрован!
        editor.putString("saved_password", password)
        editor.apply()
    }

    private fun loadSavedData() {
        val savedUsername = sharedPreferences.getString("saved_username", "")
        val savedPassword = sharedPreferences.getString("saved_password", "")

        if (!savedUsername.isNullOrEmpty()) {
            username.value = savedUsername
        }

        if (!savedPassword.isNullOrEmpty()) {
            password.value = savedPassword
        }
    }

    fun clearSavedData() {
        sharedPreferences.edit().apply {
            remove("saved_username")
            remove("saved_password")
        }.apply()

        username.value = ""
        password.value = ""
        showPassword.value = false
        _loginState.value = LoginState.Disconnected
    }

    fun hasSavedData(): Boolean {
        return sharedPreferences.contains("saved_username")
    }
}