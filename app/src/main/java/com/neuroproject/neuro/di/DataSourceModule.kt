package com.neuroproject.neuro.di

import android.content.Context
import com.neuroproject.neuro.data.datasource.LocalAuthDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideLocalAuthDataSource(
        @ApplicationContext context: Context
    ): LocalAuthDataSource {
        return LocalAuthDataSource(context)
    }
}