
package com.neuroproject.neuro.presentation.screens.sensorchecking

import com.neuroproject.neuro.domain.model.ResistanceData

/**
 * Состояние сопротивления электрода
 */
enum class ElectrodeState {
    BAD,  // Плохой контакт (сопротивление > 1000)
    OK    // Хороший контакт (сопротивление <= 1000)
}

/**
 * Состояние всех электродов
 */
data class ElectrodeStates(
    val o1: ElectrodeState = ElectrodeState.BAD,
    val o2: ElectrodeState = ElectrodeState.BAD,
    val t3: ElectrodeState = ElectrodeState.BAD,
    val t4: ElectrodeState = ElectrodeState.BAD
) {
    fun isAllOk(): Boolean =
        o1 == ElectrodeState.OK &&
                o2 == ElectrodeState.OK &&
                t3 == ElectrodeState.OK &&
                t4 == ElectrodeState.OK
}

/**
 * UI состояние экрана проверки датчиков
 */
data class SensorCheckingUiState(
    val electrodeStates: ElectrodeStates = ElectrodeStates(),
    val batteryCharge: Float = 0f,
    val isChecking: Boolean = false,
    val errorMessage: String? = null
)

// Extension function для преобразования ResistanceData -> ElectrodeStates
fun ResistanceData.toElectrodeStates(): ElectrodeStates {
    fun toState(value: Double): ElectrodeState =
        if (value <= 1000) ElectrodeState.OK else ElectrodeState.BAD

    return ElectrodeStates(
        o1 = toState(o1),
        o2 = toState(o2),
        t3 = toState(t3),
        t4 = toState(t4)
    )
}