package com.neuroproject.neuro.screens.devicesearch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.models.CapsuleInitializedState
import com.neuroproject.neuro.services.DeviceConnectionState
import com.neuroproject.neuro.models.DeviceInfo
import com.neuroproject.neuro.services.CapsuleDeviceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Модель представления для экрана поиска устройств
 *
 * Управляет процессом поиска и подключения к нейро-гарнитуре через Bluetooth.
 *
 * ## Процесс подключения:
 * 1. Инициализация CapsuleDeviceManager
 * 2. Автоматический старт поиска после инициализации
 * 3. Поиск длится 30 секунд, после чего автоматически останавливается
 * 4. Отображение найденных устройств в списке
 * 5. Подключение к выбранному устройству
 *
 * ## Состояния:
 * - **NonInitialized** - капсула не инициализирована
 * - **Initialized** - капсула готова к поиску
 * - **Searching** - активный поиск устройств
 * - **SearchTimeout** - таймаут поиска (30 секунд без результатов)
 * - **Connected** - устройство подключено
 *
 * @property dm Менеджер устройства
 * @see CapsuleDeviceManager
 * @see DeviceInfo
 */
@HiltViewModel
class DeviceSearchScreenViewModel @Inject constructor(
    private val dm: CapsuleDeviceManager
) : ViewModel() {

    /** Список найденных устройств */
    private val _foundDevices = MutableStateFlow<Array<DeviceInfo>>(emptyArray())
    val foundDevices = _foundDevices.asStateFlow()

    /** Состояние инициализации капсулы */
    private val _initState = MutableStateFlow<CapsuleInitializedState>(CapsuleInitializedState.NonInitialized)

    /** Флаг таймаута поиска */
    private val _isSearchTimeout = MutableStateFlow(false)
    val isSearchTimeout = _isSearchTimeout.asStateFlow()

    /** Флаг активного поиска */
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    /** Состояние подключения устройства */
    val deviceState = dm.connectionState

    /** Ссылка на менеджер устройства для доступа в UI */
    val capsuleDM: CapsuleDeviceManager = dm

    private var isInitialized = false // Флаг отслеживания инициализации

    init {
        setupCallbacks()

        // Инициализация капсулы только один раз
        if (!isInitialized && deviceState.value != DeviceConnectionState.connected) {
            isInitialized = true
            viewModelScope.launch {
                capsuleDM.initCapsule()
                startSearch()
            }
        }
    }

    /**
     * Настройка callback-ов от менеджера устройства
     */
    private fun setupCallbacks() {
        capsuleDM.initializeStateChanged = { state ->
            viewModelScope.launch {
                _initState.emit(state)
                when (state) {
                    CapsuleInitializedState.Initialized -> {
                        startSearch()
                    }
                    CapsuleInitializedState.NonInitialized -> {
                        Log.d("Search", "Capsule is not initialized")
                        _isSearching.update { false }
                    }
                }
            }
        }

        capsuleDM.devicesFound = { devices ->
            viewModelScope.launch {
                _foundDevices.emit(devices)
                if (devices.isNotEmpty()) {
                    _isSearchTimeout.update { false }
                }
            }
        }
    }

    /**
     * Начать поиск устройств
     *
     * Поиск длится 30 секунд, после чего автоматически останавливается.
     */
    fun startSearch() {
        viewModelScope.launch {
            if (_isSearching.value) {
                Log.d("Search", "Поиск уже выполняется")
                return@launch
            }

            _isSearching.update { true }
            _isSearchTimeout.update { false }
            _foundDevices.update { emptyArray() }

            delay(2000)
            capsuleDM.startSearch()

            // Таймер на 30 секунд
            delay(30000)

            if (_isSearching.value && deviceState.value != DeviceConnectionState.connected) {
                _isSearchTimeout.update { true }
                _isSearching.update { false }
                stopSearch()
            }
        }
    }

    /**
     * Повторить поиск
     *
     * Останавливает текущий поиск и начинает новый.
     */
    fun retrySearch() {
        viewModelScope.launch {
            stopSearch()
            delay(500)
            startSearch()
        }
    }

    /**
     * Подключиться к устройству
     *
     * @param id Идентификатор устройства (MAC-адрес)
     */
    fun connect(id: String) {
        viewModelScope.launch {
            capsuleDM.connect(id)
        }
    }

    /**
     * Остановить поиск
     */
    fun stopSearch() {
        viewModelScope.launch {
            _isSearching.update { false }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSearch()
    }
}