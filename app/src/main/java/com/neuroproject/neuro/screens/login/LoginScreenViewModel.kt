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

/**
 * Состояние авторизации
 *
 * @property Disconnected Не авторизован
 * @property Connecting Процесс авторизации
 * @property Connected Авторизован успешно
 * @property Error Ошибка авторизации
 */
sealed class LoginState {
    object Disconnected : LoginState()
    object Connecting : LoginState()
    object Connected : LoginState()
    object Error : LoginState()
}

/**
 * Модель представления для экрана входа
 *
 * Управляет процессом авторизации пользователя: валидацией ID,
 * сохранением данных в SharedPreferences и восстановлением сохраненной сессии.
 *
 * ## Правила валидации ID:
 * - Не может быть пустым
 * - Минимум 3 символа
 * - Только буквы, цифры, дефис и подчеркивание
 *
 * @property context Контекст приложения
 */
@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** Состояние авторизации */
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Disconnected)
    val loginState = _loginState.asStateFlow()

    /** Введенный ID пользователя */
    val userId = mutableStateOf("")

    /** Текст ошибки валидации */
    val errorMessage = mutableStateOf<String?>(null)

    /** SharedPreferences для хранения данных пользователя */
    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    init {
        loadSavedData()
    }

    /**
     * Обновление ID пользователя
     *
     * @param value Новое значение ID
     */
    fun onUserIdChange(value: String) {
        userId.value = value
        errorMessage.value = null
    }

    /**
     * Выполнение входа
     *
     * Валидирует ID, имитирует сетевой запрос и сохраняет данные
     */
    fun login() {
        val id = userId.value.trim()

        if (!isFormValid(id)) {
            return
        }

        _loginState.value = LoginState.Connecting
        errorMessage.value = null

        viewModelScope.launch {
            try {
                delay(1500) // Имитация сетевого запроса
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

    /**
     * Очистка сообщения об ошибке
     */
    fun clearError() {
        errorMessage.value = null
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Disconnected
        }
    }

    /**
     * Валидация формы входа
     *
     * @param userId ID пользователя
     * @return true если ID прошел валидацию
     */
    private fun isFormValid(userId: String): Boolean {
        if (userId.isBlank()) {
            errorMessage.value = "Введите ID пользователя"
            return false
        }

        if (userId.length < 2) {
            errorMessage.value = "ID должен содержать минимум 2 символа"
            return false
        }

        if (!userId.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            errorMessage.value = "ID может содержать только буквы, цифры, дефис и подчеркивание"
            return false
        }

        return true
    }

    /**
     * Выполнение логина (имитация API запроса)
     *
     * @param userId ID пользователя
     * @return true если авторизация успешна
     */
    private suspend fun performLogin(userId: String): Boolean {
        return userId.isNotEmpty() && userId.length >= 2
    }

    /**
     * Сохранение данных пользователя
     *
     * @param userId ID пользователя
     */
    private fun saveUserData(userId: String) {
        val editor = sharedPreferences.edit()
        editor.putString("saved_user_id", userId)
        editor.apply()
    }

    /**
     * Загрузка сохраненных данных
     */
    private fun loadSavedData() {
        val savedUserId = sharedPreferences.getString("saved_user_id", "")
        if (!savedUserId.isNullOrEmpty()) {
            userId.value = savedUserId
        }
    }

    /**
     * Очистка сохраненных данных (выход)
     */
    fun clearSavedData() {
        sharedPreferences.edit().apply {
            remove("saved_user_id")
        }.apply()
        userId.value = ""
        _loginState.value = LoginState.Disconnected
    }

    /**
     * Проверка наличия сохраненных данных
     *
     * @return true если есть сохраненный пользователь
     */
    fun hasSavedData(): Boolean {
        return sharedPreferences.contains("saved_user_id")
    }
}