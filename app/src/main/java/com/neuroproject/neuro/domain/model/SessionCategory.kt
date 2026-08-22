package com.neuroproject.neuro.domain.model

/**
 * Категория сессии в терминах домена.
 *
 * Enum живет в domain-слое, чтобы UI и use case не зависели от Room/data-моделей.
 */
enum class SessionCategory {
    H24_3,
    H3_6,
    H6_9,
    H9_12,
    H12_15,
    H15_18,
    H18_21,
    H21_24,
    TECHNICAL
}
