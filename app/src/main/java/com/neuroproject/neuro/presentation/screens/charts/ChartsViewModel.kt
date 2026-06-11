package com.neuroproject.neuro.presentation.screens.charts

import android.util.Log
import com.neuroproject.neuro.presentation.BaseViewModel
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.usecase.session.GetSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Состояние экрана графиков
 */
data class ChartsState(
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Модель представления для экрана графиков
 * 
 * Использует GetSessionsUseCase вместо прямого доступа к SessionDao
 */
@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val getSessions: GetSessionsUseCase
) : BaseViewModel<ChartsState>() {

    init {
        loadSessions()
    }

    override fun createInitialState(): ChartsState {
        return ChartsState(isLoading = true)
    }

    override fun handleError(error: Throwable) {
        setState { 
            copy(
                isLoading = false,
                error = error.message ?: "Неизвестная ошибка"
            )
        }
        Log.e("ChartsViewModel", "Failed to load sessions", error)
    }

    /**
     * Загрузка списка сессий для графиков
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
            }
        )
    }
}
