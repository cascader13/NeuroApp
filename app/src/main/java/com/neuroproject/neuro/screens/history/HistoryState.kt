package com.neuroproject.neuro.screens.history

import com.neuroproject.neuro.domain.model.Session

/**
 * Состояние экрана истории
 * 
 * @property sessions Список сессий для отображения
 * @property isLoading Флаг загрузки
 * @property error Сообщение об ошибке (null если ошибки нет)
 */
data class HistoryState(
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
