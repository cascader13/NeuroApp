// presentation/screens/history/HistoryViewModel.kt
package com.neuroproject.neuro.presentation.screens.history

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.presentation.BaseViewModel
import com.neuroproject.neuro.domain.model.HistoryState
import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.usecase.DeleteSessionUseCase
import com.neuroproject.neuro.domain.usecase.session.GetSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getSessions: GetSessionsUseCase,
    private val deleteSessionBySessionId: DeleteSessionUseCase
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

    fun loadSessions() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            try {
                val result = getSessions()
                when (result) {
                    is Result.Success<List<Session>> -> {
                        setState {
                            copy(
                                sessions = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = result.message ?: "Ошибка загрузки сессий"
                            )
                        }
                        Log.e("HistoryViewModel", "Failed to load sessions", result.exception)
                    }
                    is Result.Loading -> {
                        // Already loading, do nothing
                    }
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
                Log.e("HistoryViewModel", "Failed to load sessions", e)
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            setState { copy(isDeleting = true) }

            try {
                val result = deleteSessionBySessionId(sessionId)
                when (result) {
                    is Result.Success -> {
                        // Обновляем список после удаления
                        loadSessions()
                        setState {
                            copy(
                                isDeleting = false,
                                deleteSuccess = true
                            )
                        }
                        // Сбрасываем флаг успеха через некоторое время
                        delay(2000)
                        setState { copy(deleteSuccess = false) }
                    }
                    is Result.Error -> {
                        setState {
                            copy(
                                isDeleting = false,
                                error = result.message ?: "Ошибка удаления сессии"
                            )
                        }
                        Log.e("HistoryViewModel", "Failed to delete session $sessionId", result.exception)
                    }
                    is Result.Loading -> {
                        // Loading state, do nothing
                        setState { copy(isDeleting = false) }
                    }
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isDeleting = false,
                        error = e.message ?: "Ошибка удаления сессии"
                    )
                }
                Log.e("HistoryViewModel", "Failed to delete session $sessionId", e)
            }
        }
    }

    fun clearError() {
        setState { copy(error = null) }
    }

    fun clearDeleteSuccess() {
        setState { copy(deleteSuccess = false) }
    }
}