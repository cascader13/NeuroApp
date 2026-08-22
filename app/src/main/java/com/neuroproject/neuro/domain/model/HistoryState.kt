// domain/model/HistoryState.kt
package com.neuroproject.neuro.domain.model

/**
 * Состояние экрана истории сессий.
 *
 * @property sessions список сохранённых сессий
 * @property isLoading идёт ли загрузка данных
 * @property isDeleting идёт ли удаление сессии
 * @property error сообщение об ошибке
 * @property deleteSuccess успешно ли удалена сессия
 */
data class HistoryState(
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val deleteSuccess: Boolean = false
)