package com.neuroproject.neuro.screens.history

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
open class ChartsViewModel @Inject constructor(
    private val sessionDao: SessionDao
) : ViewModel() {
    private val _sessions = MutableStateFlow<List<SessionEntity>>(emptyList())
    open val sessions: StateFlow<List<SessionEntity>> = _sessions.asStateFlow()

    init {
        viewModelScope.launch {
            _sessions.value = sessionDao.getAllSessions()
        }
    }
}