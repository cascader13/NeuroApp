package com.neuroproject.neuro.domain.model

/**
 * Категория сессии в терминах домена.
 *
 * Enum живет в domain-слое, чтобы UI и use case не зависели от Room/data-моделей.
 */
enum class SessionCategory {
    MORNING,
    DAY,
    EVENING,
    TECHNICAL
}
