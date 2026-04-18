package com.neuroproject.neuro.di


import com.neuroproject.neuro.data.datasources.LocalAuthDataSource
import com.neuroproject.neuro.data.repositories.AuthRepositoryImpl
import com.neuroproject.neuro.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        localAuthDataSource: LocalAuthDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(localAuthDataSource)
    }
}