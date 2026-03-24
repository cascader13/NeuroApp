package com.neuroproject.neuro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import com.neuroproject.neuro.ui.theme.ThemeViewModel

/**
 * Корневой Composable функции приложения
 *
 * Точка входа в Compose UI. Отвечает за настройку темы приложения
 * и инициализацию навигации.
 *
 * ## Композиция:
 * 1. Получение ViewModel для управления темой через Hilt
 * 2. Подписка на изменения темы через StateFlow
 * 3. Применение темы к приложению
 * 4. Запуск графа навигации
 *
 * @param modifier Модификатор для применения к корневому контейнеру
 * @see NeuroApplicationTheme
 * @see NeuroNavGraph
 */
@Composable
fun NeuroApplication(modifier: Modifier = Modifier) {
    // Получаем ViewModel для управления темой (светлая/темная/системная)
    val themeViewModel: ThemeViewModel = hiltViewModel()

    // Подписываемся на изменения темы
    val themeMode by themeViewModel.themeMode.collectAsState()

    // Применяем тему к приложению
    NeuroApplicationTheme(
        themeMode = themeMode,
        dynamicColor = false  // Отключаем динамические цвета Material You
    ) {
        // Запускаем граф навигации
        NeuroNavGraph(modifier = modifier)
    }
}