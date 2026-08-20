package com.neuroproject.neuro.di

import com.neuroproject.neuro.data.repository.ObjectiveMetricsRepositoryImpl
import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Модуль Hilt для привязки ObjectiveMetricsRepository.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ObjectiveModule {

    @Binds
    @Singleton
    abstract fun bindObjectiveMetricsRepository(
        impl: ObjectiveMetricsRepositoryImpl
    ): ObjectiveMetricsRepository
}