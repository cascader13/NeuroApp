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

@HiltViewModel
class DeviceSearchScreenViewModel @Inject constructor(
    private val dm: CapsuleDeviceManager
) : ViewModel() {

    private val _foundDevices = MutableStateFlow<Array<DeviceInfo>>(emptyArray())
    private val _initState = MutableStateFlow<CapsuleInitializedState>(CapsuleInitializedState.NonInitialized)
    private val _isSearchTimeout = MutableStateFlow(false)
    private val _isSearching = MutableStateFlow(false)
    private var isInitialized = false // Флаг для отслеживания инициализации

    val foundDevices = _foundDevices.asStateFlow()
    val deviceState = dm.connectionState
    val isSearchTimeout = _isSearchTimeout.asStateFlow()
    val isSearching = _isSearching.asStateFlow()

    val capsuleDM: CapsuleDeviceManager = dm

    init {
        setupCallbacks()

        // Инициализируем капсулу только один раз при создании ViewModel
        if (!isInitialized && deviceState.value != DeviceConnectionState.connected) {
            isInitialized = true
            viewModelScope.launch {
                capsuleDM.initCapsule()
                startSearch()
            }
        }
    }

    private fun setupCallbacks() {
        capsuleDM.initializeStateChanged = { state ->
            viewModelScope.launch {
                _initState.emit(state)
                when (state) {
                    CapsuleInitializedState.Initialized -> {
                        // Капсула инициализирована, начинаем поиск
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
                // Если нашли устройства, сбрасываем таймаут
                if (devices.isNotEmpty()) {
                    _isSearchTimeout.update { false }
                }
            }
        }
    }

    fun startSearch() {
        viewModelScope.launch {
            // Если уже ищем или капсула не инициализирована, не начинаем новый поиск
            if (_isSearching.value) {
                Log.d("Search", "Поиск уже выполняется")
                return@launch
            }

            _isSearching.update { true }
            _isSearchTimeout.update { false }
            // Очищаем список устройств перед новым поиском
            _foundDevices.update { emptyArray() }
            delay(2000)
            // Запускаем поиск через CapsuleDeviceManager
            capsuleDM.startSearch()

            // Запускаем таймер на 30 секунд
            delay(30000)

            // Если после 30 секунд все еще ищем и нет подключенных устройств
            if (_isSearching.value && deviceState.value != DeviceConnectionState.connected) {
                _isSearchTimeout.update { true }
                _isSearching.update { false }
                stopSearch()
            }
        }
    }

    fun retrySearch() {
        viewModelScope.launch {
            stopSearch()
            delay(500) // Небольшая задержка перед повторным поиском
            startSearch()
        }
    }

    fun connect(id: String) {
        viewModelScope.launch {
            capsuleDM.connect(id)
        }
    }

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