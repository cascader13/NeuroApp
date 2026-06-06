package com.neuroproject.neuro.di

import com.neuroproject.neuro.ApplicationScope
import com.neuroproject.neuro.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Общие coroutine-зависимости приложения.
 *
 * Singleton-сервисы и репозитории не должны создавать неуправляемые
 * CoroutineScope внутри себя. Scope на уровне приложения позволяет централизованно
 * задавать SupervisorJob и dispatcher, а также упрощает тестирование.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
