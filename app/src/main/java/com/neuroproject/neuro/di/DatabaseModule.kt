package com.neuroproject.neuro.di

import android.content.Context
import com.neuroproject.neuro.ApplicationScope
import com.neuroproject.neuro.IoDispatcher
import com.neuroproject.neuro.data.FatigueDao
import com.neuroproject.neuro.data.MetricsDatabase
import com.neuroproject.neuro.data.MetricsDao
import com.neuroproject.neuro.data.MetricsRepository
import com.neuroproject.neuro.data.dao.*
import com.neuroproject.neuro.data.session.SessionDao
import com.neuroproject.neuro.data.subtest.SubjectiveAnswerDao
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMetricsDatabase(@ApplicationContext context: Context): MetricsDatabase {
        return MetricsDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideMetricsDao(database: MetricsDatabase): MetricsDao {
        return database.metricsDao()
    }

    @Provides
    @Singleton
    fun provideFatigueDao(database: MetricsDatabase): FatigueDao {
        return database.fatigueDao()
    }

    @Provides
    @Singleton
    fun provideMetricsRepository(
        metricsDao: MetricsDao,
        @ApplicationScope applicationScope: CoroutineScope,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): MetricsRepository {
        return MetricsRepository(metricsDao, applicationScope, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideSubjectiveAnswerDao(database: MetricsDatabase): SubjectiveAnswerDao {
        return database.subjectiveAnswerDao()
    }

    @Provides
    @Singleton
    fun provideSubjectiveQuestionDao(database: MetricsDatabase): SubjectiveQuestionDao {
        return database.subjectiveQuestionDao()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: MetricsDatabase): SessionDao {
        return database.sessionDao()
    }

    // === Доменные DAO ===

    @Provides
    @Singleton
    fun provideCalibrationDao(database: MetricsDatabase): CalibrationDao {
        return database.calibrationDao()
    }

    @Provides
    @Singleton
    fun provideNfbDao(database: MetricsDatabase): NfbDao {
        return database.nfbDao()
    }

    @Provides
    @Singleton
    fun provideEegDao(database: MetricsDatabase): EegDao {
        return database.eegDao()
    }

    @Provides
    @Singleton
    fun providePhysiologicalDao(database: MetricsDatabase): PhysiologicalDao {
        return database.physiologicalDao()
    }

    @Provides
    @Singleton
    fun provideMemsDao(database: MetricsDatabase): MemsDao {
        return database.memsDao()
    }

    @Provides
    @Singleton
    fun provideProductivityDao(database: MetricsDatabase): ProductivityDao {
        return database.productivityDao()
    }

    @Provides
    @Singleton
    fun provideEmotionalDao(database: MetricsDatabase): EmotionalDao {
        return database.emotionalDao()
    }

    @Provides
    @Singleton
    fun provideCardioDao(database: MetricsDatabase): CardioDao {
        return database.cardioDao()
    }
}