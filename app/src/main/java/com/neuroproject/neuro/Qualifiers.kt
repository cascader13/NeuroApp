package com.neuroproject.neuro

import javax.inject.Qualifier

/**
 * Квалификатор Dagger Hilt для различения экземпляров зависимостей
 *
 * Используется для предоставления разных экземпляров одного типа
 * для разных серверов или целей.
 *
 * ## Применение:
 * В приложении используются два сервера:
 * - **Основной сервер** (без квалификатора) - для загрузки метрик
 * - **Сервер мониторинга** (с @SecondServer) - для отправки строк в реальном времени
 *
 * ## Пример использования:
 * ```kotlin
 * @Provides
 * @Singleton
 * @SecondServer
 * fun provideSecondGson(): Gson = GsonBuilder().create()
 *
 * @Provides
 * @Singleton
 * fun provideMainGson(): Gson = GsonBuilder().create()
 * ```
 *
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SecondServer