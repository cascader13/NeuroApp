package com.neuroproject.neuro.screens.history

import android.util.Log
import com.neuroproject.neuro.domain.BaseViewModel
import com.neuroproject.neuro.domain.usecase.GetSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Модель представления для экрана истории
 * 
 * Использует:
 * - GetSessionsUseCase для загрузки сессий (вместо прямого доступа к DAO)
 * - BaseViewModel для уменьшения boilerplate кода
 * - HistoryState для типизированного состояния
 * 
 * ## Преимущества нового подхода:
 * - Меньше кода (нет ручного управления StateFlow)
 * - Единая обработка ошибок
 * - Легче тестировать (зависит от UseCase а не от DAO)
 * - Понятнее структура состояния
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getSessions: GetSessionsUseCase
) : BaseViewModel<HistoryState>() {

    init {
        initializeState()
        loadSessions()
    }

    override fun createInitialState(): HistoryState {
        return HistoryState(isLoading = true)
    }

    override fun handleError(error: Throwable) {
        setState { 
            copy(
                isLoading = false,
                error = error.message ?: "Неизвестная ошибка"
            )
        }
        Log.e("HistoryViewModel", "Failed to load sessions", error)
    }

    /**
     * Загрузка списка сессий
     */
    fun loadSessions() {
        safeLaunchWithResult(
            block = { getSessions() },
            onSuccess = { sessions ->
                setState { 
                    copy(
                        sessions = sessions,
                        isLoading = false,
                        error = null
                    )
                }
            },
            onError = { error ->
                setState {
                    copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка загрузки сессий"
                    )
                }
            }
        )
    }

    /**
     * Очистка сообщения об ошибке
     */
    fun clearError() {
        setState { copy(error = null) }
    }
}
