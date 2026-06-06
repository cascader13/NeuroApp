package com.neuroproject.neuro

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

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
class MainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}