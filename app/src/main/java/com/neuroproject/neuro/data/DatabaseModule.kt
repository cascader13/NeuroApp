package com.neuroproject.neuro.di

import android.content.Context
import com.neuroproject.neuro.data.MetricsDatabase
import com.neuroproject.neuro.data.MetricsDao
import com.neuroproject.neuro.data.MetricsRepository
import com.neuroproject.neuro.data.subtest.SubTestDao
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideMetricsRepository(metricsDao: MetricsDao): MetricsRepository {
        return MetricsRepository(metricsDao)
    }

    @Provides
    @Singleton
    fun provideSubTestDao(database: MetricsDatabase): SubTestDao {
        return database.subTestDao()
    }

    @Provides
    @Singleton
    fun provideSubjectiveQuestionDao(database: MetricsDatabase): SubjectiveQuestionDao {
        return database.subjectiveQuestionDao()
    }
}