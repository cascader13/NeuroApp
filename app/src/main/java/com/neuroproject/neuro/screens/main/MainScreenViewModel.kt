package com.neuroproject.neuro.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Состояние главного экрана
 *
 * @property appVersion Версия приложения
 * @property lastSessionDate Дата последней сессии
 */
data class MainScreenState(
    val appVersion: String = "0.6.3",
    val lastSessionDate: String? = null
)

/**
 * Модель представления для главного экрана
 *
 * Отвечает за отображение основной информации о приложении
 * и загрузку данных о последней сессии пользователя.
 */
@HiltViewModel
class MainScreenViewModel @Inject constructor() : ViewModel() {

    /** Состояние главного экрана */
    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    init {
        loadAppInfo()
    }

    /**
     * Загрузка информации о приложении
     *
     * Здесь можно загружать дату последней сессии из базы данных,
     * информацию о пользователе и другие данные.
     */
    private fun loadAppInfo() {
        viewModelScope.launch {
            // TODO: Загрузка даты последней сессии из БД
            // val lastSession = sessionDao.getLastSession()
            // _state.update { it.copy(lastSessionDate = lastSession?.timestamp?.let { formatDate(it) }) }
        }
    }

    /**
     * Получение информации о приложении в виде строки
     *
     * @return Строка с версией приложения
     */
    fun getAppInfo(): String {
        return "neuroAssestment v${_state.value.appVersion}"
    }
}