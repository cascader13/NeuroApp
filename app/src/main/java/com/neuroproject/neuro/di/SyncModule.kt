package com.neuroproject.neuro.di

import com.neuroproject.neuro.domain.repository.DatabaseExportRepository
import com.neuroproject.neuro.domain.repository.SyncRepository
import com.neuroproject.neuro.domain.usecase.sync.EnablePeriodicSyncUseCase
import com.neuroproject.neuro.domain.usecase.sync.ExportDatabaseUseCase
import com.neuroproject.neuro.domain.usecase.sync.ObserveSyncStateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideObserveSyncStateUseCase(
        syncRepository: SyncRepository
    ): ObserveSyncStateUseCase = ObserveSyncStateUseCase(syncRepository)

    @Provides
    @Singleton
    fun provideEnablePeriodicSyncUseCase(
        syncRepository: SyncRepository
    ): EnablePeriodicSyncUseCase = EnablePeriodicSyncUseCase(syncRepository)

    @Provides
    @Singleton
    fun provideExportDatabaseUseCase(
        databaseExportRepository: DatabaseExportRepository
    ): ExportDatabaseUseCase = ExportDatabaseUseCase(databaseExportRepository)
}
