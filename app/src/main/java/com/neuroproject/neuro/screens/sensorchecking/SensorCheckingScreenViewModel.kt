package com.neuroproject.neuro.screens.sensorchecking

import android.util.Log
import androidx.lifecycle.ViewModel
import com.neuroproject.neuro.services.CapsuleDeviceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Состояние сопротивления электрода
 *
 * @property BAD Плохой контакт (сопротивление > 1000)
 * @property OK Хороший контакт (сопротивление <= 1000)
 */
enum class ResistState(val value: Int) {
    BAD(0),
    OK(1)
}

/**
 * Запись состояния сопротивления всех электродов
 *
 * @property o1 Состояние электрода O1
 * @property o2 Состояние электрода O2
 * @property t3 Состояние электрода T3
 * @property t4 Состояние электрода T4
 */
data class ResistStateRecord(
    val o1: ResistState = ResistState.BAD,
    val o2: ResistState = ResistState.BAD,
    val t3: ResistState = ResistState.BAD,
    val t4: ResistState = ResistState.BAD
) {
    /**
     * Проверка качества контакта всех электродов
     *
     * @return true если все электроды имеют хороший контакт
     */
    fun isAllOk(): Boolean {
        return o1 == ResistState.OK &&
                o2 == ResistState.OK &&
                t3 == ResistState.OK &&
                t4 == ResistState.OK
    }
}

/**
 * Модель представления для экрана проверки датчиков
 *
 * Отвечает за проверку качества контакта электродов нейро-гарнитуры.
 * Отображает состояние сопротивления для каждого электрода в реальном времени.
 *
 * ## Электроды:
 * - **O1** - затылочная область (левая)
 * - **O2** - затылочная область (правая)
 * - **T3** - височная область (левая)
 * - **T4** - височная область (правая)
 *
 * ## Пороговые значения:
 * - **OK** - сопротивление ≤ 1000 (хороший контакт)
 * - **BAD** - сопротивление > 1000 (плохой контакт)
 *
 * @param dm Менеджер устройства
 * @see CapsuleDeviceManager
 * @see ResistState
 */
@HiltViewModel
class SensorCheckingScreenViewModel @Inject constructor(dm: CapsuleDeviceManager) : ViewModel() {

    /** Менеджер устройства */
    val capsuleDM: CapsuleDeviceManager = dm

    private val _scope = CoroutineScope(EmptyCoroutineContext)

    /** Поток состояния сопротивления электродов */
    private val _resistState = MutableStateFlow(ResistStateRecord())
    val resistState = _resistState.asStateFlow()

    private val _batteryCharge = MutableStateFlow(0f)

    val batteryCharge = _batteryCharge.asStateFlow()


    init {

        _scope.launch {
            capsuleDM.BatteryChargeValue.collect { batteryChargeData ->
                _batteryCharge.emit(batteryChargeData.value)
                Log.d("SensorCheckingScreenViewModel", "Battery charge: ${batteryChargeData.value}%")
            }
        }
        capsuleDM.resistanceReceived = { o1: Double, o2: Double, t3: Double, t4: Double ->
            Log.d("SensorCheckingScreenViewModel", "o1 = $o1, o2 = $o2, t3 = $t3, t4 = $t4")
            _scope.launch {
                _resistState.emit(
                    ResistStateRecord(
                        setResistColor(o1),
                        setResistColor(o2),
                        setResistColor(t3),
                        setResistColor(t4)
                    )
                )
            }
        }
    }

    /**
     * Начать проверку датчиков
     *
     * Запускает измерение сопротивления электродов.
     */
    suspend fun start() {
        Log.d("TAG", "TRY START RESIST")
        delay(2000)
        capsuleDM.startResistance()
    }

    /**
     * Завершить проверку датчиков
     */
    fun finish() {
        capsuleDM.stopResistance()
    }

    /**
     * Определение состояния по значению сопротивления
     *
     * @param value Значение сопротивления в килоомах
     * @return Состояние контакта
     */
    private fun setResistColor(value: Double): ResistState {
        return if (value > 1000) {
            ResistState.BAD
        } else {
            ResistState.OK
        }
    }
}