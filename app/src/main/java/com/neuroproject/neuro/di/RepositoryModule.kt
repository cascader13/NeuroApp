package com.neuroproject.neuro.di

import com.neuroproject.neuro.data.datasource.LocalAuthDataSource
import com.neuroproject.neuro.data.device.CapsuleDeviceAdapter
import com.neuroproject.neuro.data.device.CapsuleSensorStreamAdapter
import com.neuroproject.neuro.data.local.RoomCalibrationRepository
import com.neuroproject.neuro.data.local.RoomMetricsRepositoryAdapter
import com.neuroproject.neuro.data.local.RoomSessionRepository
import com.neuroproject.neuro.data.local.RoomSubjectiveTestRepository
import com.neuroproject.neuro.data.repository.AuthRepositoryImpl
import com.neuroproject.neuro.domain.repository.AuthRepository
import com.neuroproject.neuro.domain.repository.CalibrationRepository
import com.neuroproject.neuro.domain.repository.DeviceGateway
import com.neuroproject.neuro.domain.repository.MetricsRepository
import com.neuroproject.neuro.domain.repository.SensorStreamGateway
import com.neuroproject.neuro.domain.repository.SessionRepository
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI-модуль, который связывает domain-контракты с data-реализациями.
 *
 * ViewModel и use case зависят только от интерфейсов domain-слоя. Конкретные Room/JNI
 * реализации подключаются здесь, на внешнем слое приложения.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        localAuthDataSource: LocalAuthDataSource
    ): AuthRepository = AuthRepositoryImpl(localAuthDataSource)

    @Provides
    @Singleton
    fun provideSessionRepository(repository: RoomSessionRepository): SessionRepository = repository

    @Provides
    @Singleton
    fun provideSubjectiveTestRepository(repository: RoomSubjectiveTestRepository): SubjectiveTestRepository = repository

    @Provides
    @Singleton
    fun provideCalibrationRepository(repository: RoomCalibrationRepository): CalibrationRepository = repository

    @Provides
    @Singleton
    fun provideMetricsRepositoryAdapter(repository: RoomMetricsRepositoryAdapter): MetricsRepository = repository

    @Provides
    @Singleton
    fun provideDeviceGateway(adapter: CapsuleDeviceAdapter): DeviceGateway = adapter

    @Provides
    @Singleton
    fun provideSensorStreamGateway(adapter: CapsuleSensorStreamAdapter): SensorStreamGateway = adapter
}
