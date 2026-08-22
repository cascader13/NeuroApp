package com.neuroproject.neuro.presentation.screens.main

import com.neuroproject.neuro.presentation.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Состояние главного экрана
 */
data class MainScreenState(
    var init: Boolean = true
    // Пустое состояние, так как на экран ничего не передается
    // Можно добавить флаги для анимаций или других UI эффектов при необходимости
)

/**
 * Модель представления для главного экрана
 *
 * Отвечает за отображение основной информации о приложении
 */
@HiltViewModel
class MainScreenViewModel @Inject constructor() : BaseViewModel<MainScreenState>() {

    override fun createInitialState(): MainScreenState = MainScreenState()
}