package com.neuroproject.neuro.di

import android.content.Context
import com.neuroproject.neuro.ApplicationScope
import com.neuroproject.neuro.IoDispatcher
import com.neuroproject.neuro.data.FatigueDao
import com.neuroproject.neuro.data.MetricsDatabase
import com.neuroproject.neuro.data.MetricsDao
import com.neuroproject.neuro.data.MetricsRepository
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

/**
 * Dagger Hilt модуль для предоставления зависимостей базы данных
 *
 * Обеспечивает инъекцию всех DAO и репозиториев, связанных с базой данных Room.
 * Все зависимости предоставляются как синглтоны на уровне приложения.
 *
 * ## Иерархия зависимостей:
 * ```
 * MetricsDatabase
 *     ├── MetricsDao
 *     ├── SubjectiveAnswerDao
 *     ├── SubjectiveQuestionDao
 *     └── SessionDao
 *
 * MetricsRepository (обертка над MetricsDao)
 * ```
 *
 * @see MetricsDatabase
 * @see MetricsRepository
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Предоставление экземпляра базы данных
     *
     * @param context Контекст приложения для создания базы данных
     * @return Синглтон MetricsDatabase
     */
    @Provides
    @Singleton
    fun provideMetricsDatabase(@ApplicationContext context: Context): MetricsDatabase {
        return MetricsDatabase.getInstance(context)
    }

    /**
     * Предоставление DAO для метрик
     *
     * @param database Экземпляр базы данных
     * @return DAO для работы с метриками
     */
    @Provides
    @Singleton
    fun provideMetricsDao(database: MetricsDatabase): MetricsDao {
        return database.metricsDao()
    }

    /**
     * Предоставление DAO для анализа результата
     *
     * @param database Экземпляр базы данных
     * @return DAO для работы с конечным результатом
     */
    @Provides
    @Singleton
    fun provideFatigueDao(database: MetricsDatabase): FatigueDao {
        return database.fatigueDao()
    }

    /**
     * Предоставление репозитория метрик
     *
     * Оборачивает MetricsDao и предоставляет бизнес-логику для работы с метриками.
     *
     * @param metricsDao DAO для доступа к данным
     * @return Репозиторий метрик
     */
    @Provides
    @Singleton
    fun provideMetricsRepository(
        metricsDao: MetricsDao,
        @ApplicationScope applicationScope: CoroutineScope,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): MetricsRepository {
        return MetricsRepository(metricsDao, applicationScope, ioDispatcher)
    }

    /**
     * Предоставление DAO для ответов на субъективные вопросы
     *
     * @param database Экземпляр базы данных
     * @return DAO для работы с ответами
     */
    @Provides
    @Singleton
    fun provideSubTestDao(database: MetricsDatabase): SubjectiveAnswerDao {
        return database.subjectiveAnswerDao()
    }

    /**
     * Предоставление DAO для вопросов субъективного тестирования
     *
     * @param database Экземпляр базы данных
     * @return DAO для работы с вопросами
     */
    @Provides
    @Singleton
    fun provideSubjectiveQuestionDao(database: MetricsDatabase): SubjectiveQuestionDao {
        return database.subjectiveQuestionDao()
    }

    /**
     * Предоставление DAO для сессий
     *
     * @param database Экземпляр базы данных
     * @return DAO для работы с сессиями
     */
    @Provides
    @Singleton
    fun provideSessionDao(database: MetricsDatabase): SessionDao {
        return database.sessionDao()
    }
}