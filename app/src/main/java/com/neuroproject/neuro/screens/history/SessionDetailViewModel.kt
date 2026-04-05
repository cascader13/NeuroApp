package com.neuroproject.neuro.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.data.session.SessionDao
import com.neuroproject.neuro.data.session.SessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionDao: SessionDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: 0L
    private val _session = MutableStateFlow<SessionEntity?>(null)
    val session: StateFlow<SessionEntity?> = _session.asStateFlow()

    init {
        viewModelScope.launch {
            _session.value = sessionDao.getSession(sessionId)
        }
    }
}