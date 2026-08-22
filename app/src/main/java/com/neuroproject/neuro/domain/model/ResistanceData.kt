package com.neuroproject.neuro.domain.model

/**
 * Данные сопротивления электродов ЭЭГ.
 *
 * @property o1 сопротивление на канале O1 (Ом)
 * @property o2 сопротивление на канале O2 (Ом)
 * @property t3 сопротивление на канале T3 (Ом)
 * @property t4 сопротивление на канале T4 (Ом)
 */
data class ResistanceData(
    val o1: Double,  // сопротивление на канале O1 (Ом)
    val o2: Double,  // сопротивление на канале O2 (Ом)
    val t3: Double,  // сопротивление на канале T3 (Ом)
    val t4: Double   // сопротивление на канале T4 (Ом)
) {
    fun isChannelOk(value: Double): Boolean = value <= 1000

    fun isAllOk(): Boolean =
        isChannelOk(o1) && isChannelOk(o2) && isChannelOk(t3) && isChannelOk(t4)
}