package com.neuroproject.neuro.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainScreenState(
    val appVersion: String = "0.6.3",
    val lastSessionDate: String? = null
)

@HiltViewModel
class MainScreenViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    init {
        loadAppInfo()
    }

    private fun loadAppInfo() {
        viewModelScope.launch {
            // Здесь можно загружать информацию о приложении
            // Например, дату последней сессии из БД
        }
    }

    fun getAppInfo(): String {
        return "neuroAssestment v${_state.value.appVersion}"
    }
}