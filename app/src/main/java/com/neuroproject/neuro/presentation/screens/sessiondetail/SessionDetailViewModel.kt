package com.neuroproject.neuro.presentation.screens.sessiondetail

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

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: 0L
    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    init {
        viewModelScope.launch {
            _session.value = sessionRepository.getSession(sessionId)
        }
    }
}
