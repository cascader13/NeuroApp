package com.neuroproject.neuro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Главный класс приложения Neuro Project
 *
 * Аннотация @HiltAndroidApp включает Hilt для внедрения зависимостей
 * на уровне всего приложения. Это базовый класс, который инициализирует
 * Dagger Hilt компоненты.
 *
 * Hilt позволяет:
 * - Инжектировать зависимости в Activity, Fragment, ViewModel, Service
 * - Использовать синглтоны на уровне приложения
 * - Управлять жизненным циклом компонентов
 */
@HiltAndroidApp
class MainApplication : Application() {
    // Класс не требует дополнительной реализации,
    // вся логика инициализации выполняется через Hilt
}