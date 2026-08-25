package com.neuroproject.neuro.presentation.screens.sessiondetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel экрана деталей сессии.
 *
 * Загружает и отображает информацию о конкретной сессии по её идентификатору.
 */
@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: 0L
    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val _isMarked = MutableStateFlow<Boolean?>(null)
    val isMarked: StateFlow<Boolean?> = _isMarked.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            _session.value = sessionRepository.getSession(sessionId)
            val marked = sessionRepository.isMarkedSession(sessionId)
            _isMarked.value = marked
        }
    }

    fun updateComment(comment: String) {
        viewModelScope.launch {
            val currentSession = _session.value
            if (currentSession != null) {
                val updatedSession = currentSession.copy(comment = comment)
                sessionRepository.updateSession(updatedSession)
                _session.value = updatedSession
            }
        }
    }
    fun refreshSession() {
        loadSession()
    }


}
