
package com.neuroproject.neuro.presentation.screens.devicesearch

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.presentation.BaseViewModel
import com.neuroproject.neuro.domain.model.DeviceInfo
import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.usecase.device.ConnectDeviceUseCase
import com.neuroproject.neuro.domain.usecase.device.DisconnectDeviceUseCase
import com.neuroproject.neuro.domain.usecase.device.InitDeviceUseCase
import com.neuroproject.neuro.domain.usecase.device.SearchDevicesUseCase
import com.neuroproject.neuro.domain.usecase.device.ObserveConnectionStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Состояние экрана поиска устройств
 */
data class DeviceSearchScreenState(
    val isSearching: Boolean = false,
    val isSearchTimeout: Boolean = false,
    val foundDevices: List<DeviceInfo> = emptyList(),
    val connectionState: DeviceConnectionState = DeviceConnectionState.disconnected,
    val connectingDeviceId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
open class DeviceSearchScreenViewModel @Inject constructor(
    private val initDeviceUseCase: InitDeviceUseCase,
    private val searchDevicesUseCase: SearchDevicesUseCase,
    private val connectDeviceUseCase: ConnectDeviceUseCase,
    private val disconnectDeviceUseCase: DisconnectDeviceUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase
) : BaseViewModel<DeviceSearchScreenState>() {

    private var searchJob: Job? = null

    init {
        setupConnectionStateObserver()
        initializeCapsule()
    }

    override fun createInitialState(): DeviceSearchScreenState = DeviceSearchScreenState()

    override fun handleError(error: Throwable) {
        setState {
            copy(
                errorMessage = "Ошибка: ${error.message ?: "Неизвестная ошибка"}",
                isSearching = false
            )
        }
        Log.e("DeviceSearch", "ViewModel error", error)
    }

    /**
     * Наблюдение за состоянием подключения
     */
    private fun setupConnectionStateObserver() {
        safeLaunch {
            observeConnectionStateUseCase()
                .catch { error ->
                    Log.e("DeviceSearch", "Error observing connection state", error)
                    setState { copy(errorMessage = error.message) }
                }
                .collect { state ->
                    setState { copy(connectionState = state) }

                    when (state) {
                        DeviceConnectionState.connected,
                        DeviceConnectionState.disconnected,
                        DeviceConnectionState.error -> {
                            setState { copy(connectingDeviceId = null) }
                        }
                        else -> { /* Ignore */ }
                    }
                }
        }
    }

    /**
     * Инициализация устройства
     */
    private fun initializeCapsule() {
        safeLaunch {
            try {
                initDeviceUseCase()
                // После инициализации автоматически начинаем поиск
                startSearch()
            } catch (e: Exception) {
                Log.e("DeviceSearch", "Failed to initialize capsule", e)
                setState { copy(errorMessage = "Ошибка инициализации: ${e.message}") }
            }
        }
    }

    /**
     * Начать поиск устройств
     */
    fun startSearch() {
        if (currentState.isSearching) {
            Log.d("DeviceSearch", "Поиск уже выполняется")
            return
        }

        // Отменяем предыдущий поиск
        searchJob?.cancel()

        safeLaunch {
            setState {
                copy(
                    isSearching = true,
                    isSearchTimeout = false,
                    foundDevices = emptyList(),
                    errorMessage = null
                )
            }

            // Небольшая задержка перед началом поиска
            delay(500)

            // Запускаем поиск через Use Case
            searchJob = viewModelScope.launch {
                searchDevicesUseCase()
                    .catch { error ->
                        Log.e("DeviceSearch", "Search error", error)
                        val errorMessage = when (error) {
                            is SecurityException -> "Нет разрешения на Bluetooth. Проверьте настройки приложения."
                            is IllegalStateException -> if (error.message?.contains("Bluetooth") == true) {
                                "Bluetooth выключен. Включите Bluetooth."
                            } else {
                                "Ошибка поиска: ${error.message}"
                            }
                            else -> "Ошибка поиска: ${error.message}"
                        }
                        setState {
                            copy(
                                isSearching = false,
                                errorMessage = errorMessage
                            )
                        }
                    }
                    .collect { devices ->
                        setState {
                            copy(
                                foundDevices = devices,
                                isSearchTimeout = if (devices.isNotEmpty()) false else currentState.isSearchTimeout
                            )
                        }
                    }
            }

            // Таймаут поиска 30 секунд
            delay(30000)

            if (currentState.isSearching && currentState.connectionState != DeviceConnectionState.connected) {
                setState {
                    copy(
                        isSearchTimeout = true,
                        isSearching = false
                    )
                }
                stopSearch()
            }
        }
    }

    /**
     * Повторить поиск
     */
    fun retrySearch() {
        safeLaunch {
            stopSearch()
            delay(500)
            startSearch()
        }
    }

    /**
     * Подключиться к устройству
     */
    fun connect(deviceId: String) {
        if (currentState.connectionState != DeviceConnectionState.disconnected) {
            setState {
                copy(errorMessage = "Нельзя подключиться в текущем состоянии: ${currentState.connectionState}")
            }
            return
        }

        if (currentState.isSearching) {
            stopSearch()
        }

        setState {
            copy(
                connectingDeviceId = deviceId,
                errorMessage = null
            )
        }

        safeLaunch {
            try {
                connectDeviceUseCase(deviceId)
            } catch (e: SecurityException) {
                Log.e("DeviceSearch", "Permission error", e)
                setState {
                    copy(
                        connectingDeviceId = null,
                        errorMessage = "Нет разрешения на Bluetooth. Проверьте настройки приложения."
                    )
                }
            } catch (e: IllegalStateException) {
                Log.e("DeviceSearch", "State error", e)
                setState {
                    copy(
                        connectingDeviceId = null,
                        errorMessage = when {
                            e.message?.contains("Bluetooth") == true -> "Bluetooth выключен. Включите Bluetooth."
                            e.message?.contains("timed out") == true -> "Превышено время подключения. Попробуйте снова."
                            else -> "Ошибка: ${e.message}"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("DeviceSearch", "Connection error", e)
                setState {
                    copy(
                        connectingDeviceId = null,
                        errorMessage = "Ошибка подключения: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Отключиться от устройства
     */
    fun disconnect() {
        safeLaunch {
            try {
                disconnectDeviceUseCase()
            } catch (e: Exception) {
                Log.e("DeviceSearch", "Disconnect error", e)
                setState { copy(errorMessage = "Ошибка отключения: ${e.message}") }
            }
        }
    }

    /**
     * Остановить поиск
     */
    fun stopSearch() {
        searchJob?.cancel()
        searchJob = null
        setState { copy(isSearching = false) }
    }

    /**
     * Очистить сообщение об ошибке
     */
    fun clearError() {
        setState { copy(errorMessage = null) }
    }

    /**
     * Сбросить состояние
     */
    fun resetState() {
        stopSearch()
        setState {
            copy(
                isSearchTimeout = false,
                foundDevices = emptyList(),
                connectingDeviceId = null,
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSearch()
    }
}