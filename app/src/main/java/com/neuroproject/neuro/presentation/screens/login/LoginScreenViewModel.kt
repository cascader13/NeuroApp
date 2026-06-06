package com.neuroproject.neuro.presentation.screens.login

import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.presentation.BaseViewModel
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.usecase.LoginUseCase
import com.neuroproject.neuro.domain.usecase.ValidateUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.usecase.SaveMobileIdUseCase
import kotlinx.coroutines.launch

/**
 * Состояние авторизации
 *
 * @property Disconnected Не авторизован
 * @property Connecting Процесс авторизации
 * @property Connected Авторизован успешно
 * @property Error Ошибка авторизации
 */
data class LoginUIState (
    val userId: String = "",
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val hasSavedData: Boolean = false,
    val isValid: Boolean = false,
    val userIdHistory: List<String> = emptyList(),
    val showDropdown: Boolean = false
    )

/**
 * Модель представления для экрана входа
 *
 * Управляет процессом авторизации пользователя: валидацией ID,
 * сохранением данных в SharedPreferences и восстановлением сохраненной сессии.
 *
 * ## Правила валидации ID:
 * - Не может быть пустым
 * - Минимум 2 символа
 * - Только буквы, цифры, дефис и подчеркивание
 *
 * @property context Контекст приложения
 */
@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateUserIdUseCase: ValidateUserIdUseCase,
    private val authRepository: AuthRepository,
    private val saveIdUseCase: SaveMobileIdUseCase
) : BaseViewModel<LoginUIState>() {

    init {
        initializeState()
    }

    /** Создание начального состояния для поля ввода*/
    override fun createInitialState(): LoginUIState {
        val savedUserId = authRepository.getSavedUserId() ?: ""
        val history = authRepository.getUserIdHistory()
        return LoginUIState(
            userId = savedUserId,
            hasSavedData = authRepository.hasSavedData(),
            isValid = validateUserIdUseCase(savedUserId) is ValidateUserIdUseCase.ValidationResult.Success,
            userIdHistory = history,
            showDropdown = false
        )
    }

    /**
     * Обновление ID пользователя
     *
     * @param value Новое значение ID
     */
    fun onUserIdChange(value: String) {
        val validationResult = validateUserIdUseCase(value)
        val isValid = validationResult is ValidateUserIdUseCase.ValidationResult.Success
        val showDropdown = currentState.userIdHistory.isNotEmpty() &&
                value.isNotEmpty() &&
                currentState.userIdHistory.any { it.contains(value, ignoreCase = true) }

        setState {
            copy(
                userId = value,
                isValid = isValid,
                showDropdown = showDropdown,
                errorMessage = if (validationResult is ValidateUserIdUseCase.ValidationResult.Error)
                    validationResult.message else null
            )
        }
    }

    fun showDropdown() {
        if (currentState.userIdHistory.isNotEmpty()) {
            setState { copy(showDropdown = true) }
        }
    }

    fun onTextFieldFocusChange(focused: Boolean) {
        if (focused && currentState.userId.isNotEmpty() && currentState.userIdHistory.isNotEmpty()) {
            setState { copy(showDropdown = true) }
        } else if (!focused) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(200)
                if (currentState.showDropdown) {
                    setState { copy(showDropdown = false) }
                }
            }
        }
    }

    fun selectUserIdFromHistory(userId: String) {
        val validationResult = validateUserIdUseCase(userId)
        val isValid = validationResult is ValidateUserIdUseCase.ValidationResult.Success

        setState {
            copy(
                userId = userId,
                isValid = isValid,
                showDropdown = false,
                errorMessage = if (validationResult is ValidateUserIdUseCase.ValidationResult.Error)
                    validationResult.message else null
            )
        }
    }

    fun removeFromHistory(userId: String) {
        authRepository.removeFromHistory(userId)
        val updatedHistory = authRepository.getUserIdHistory()

        setState {
            copy(
                userIdHistory = updatedHistory,
                // Если удалили текущий ID, очищаем поле
                userId = if (currentState.userId == userId) "" else currentState.userId,
                isValid = if (currentState.userId == userId) false else currentState.isValid,
                showDropdown = updatedHistory.isNotEmpty() && currentState.userId.isNotEmpty()
            )
        }
    }

    fun login() {
        val currentState = currentState

        if (!currentState.isValid) {
            return
        }

        val result = loginUseCase(currentState.userId)

        when (result) {
            is Result.Success -> {
                saveIdUseCase.invoke(currentState.userId)
                setState {
                    copy(
                        isLoggedIn = true,
                        hasSavedData = true,
                        errorMessage = null,
                        userIdHistory = authRepository.getUserIdHistory()
                    )
                }
            }
            is Result.Error -> {
                setState {
                    copy(
                        isLoggedIn = false,
                        errorMessage = result.message ?: "Ошибка входа"
                    )
                }
            }
            is Result.Loading -> {
                // Не используется в синхронном коде, но нужно для полноты when
                // Можно игнорировать или не обрабатывать
            }
        }
    }

    fun clearSavedData() {
        authRepository.clearSavedData()
        setState {
            copy(
                userId = "",
                hasSavedData = false,
                errorMessage = null,
                isValid = false,
                userIdHistory = emptyList(),
                showDropdown = false
            )
        }
    }

    fun clearError() {
        setState { copy(errorMessage = null) }
    }

    fun dismissDropdown() {
        setState { copy(showDropdown = false) }
    }
}