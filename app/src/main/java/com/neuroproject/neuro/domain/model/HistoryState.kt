// domain/model/HistoryState.kt
package com.neuroproject.neuro.domain.model

data class HistoryState(
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val deleteSuccess: Boolean = false
)