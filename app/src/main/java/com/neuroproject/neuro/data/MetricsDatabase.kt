package com.neuroproject.neuro.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.neuroproject.neuro.data.session.SessionDao
import com.neuroproject.neuro.data.session.SessionEntity
import com.neuroproject.neuro.data.subtest.SubjectiveAnswerDao
import com.neuroproject.neuro.data.subtest.SubjectiveAnswerEntity
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionDao
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Основная база данных Room для хранения всех метрик Neuro Project
 *
 * Содержит все таблицы для хранения данных, полученных от нейро-гарнитуры,
 * а также данные субъективного тестирования и информации о сессиях.
 *
 * ## Структура базы данных:
 * - **Метрики в реальном времени**: NFB, физиологические, ЭЭГ, MEMS, продуктивность, эмоциональные, кардио
 * - **Сжатые метрики**: Агрегированные данные для уменьшения объема
 * - **Базовые значения**: Индивидуальные нормы пользователя
 * - **Сессии**: Информация о сессиях записи
 * - **Субъективное тестирование**: Вопросы и ответы пользователя
 * - **Калибровка**: Индивидуальная калибровка для пользователя
 *
 * ## Версионирование:
 * - **version = 2** - текущая версия схемы
 * - **fallbackToDestructiveMigration()** - при несовместимости версий БД пересоздается
 *
 * ## Инициализация:
 * При первом создании базы данных автоматически заполняются вопросы субъективного тестирования.
 *
 * @see Converters
 * @see MetricsDao
 */
@Database(
    entities = [
        // Основные метрики
        NFBMetricEntity::class,
        NFBMetricCompressedEntity::class,
        PhysiologicalMetricEntity::class,
        PhysiologicalMetricCompressedEntity::class,
        PhysiologicalBaselinesEntity::class,
        MEMSMetricEntity::class,
        MEMSMetricCompressedEntity::class,
        ProductivityMetricEntity::class,
        ProductivityMetricCompressedEntity::class,
        ProductivityIndexesEntity::class,
        ProductivityBaselinesEntity::class,
        EmotionalMetricEntity::class,
        EmotionalMetricCompressedEntity::class,

        // ЭЭГ метрики
        EEGRawMetricEntity::class,
        EEGRawMetricCompressedEntity::class,
        EEGProceedMetricEntity::class,
        EEGProceedMetricCompressedEntity::class,
        EEGArtifactsMetricEntity::class,
        EEGArtifactsMetricCompressedEntity::class,

        // Кардио метрики
        CardioMetricEntity::class,
        CardioMetricCompressedEntity::class,

        // Субъективное тестирование и сессии
        SubjectiveQuestionEntity::class,
        CalibrationHistoryEntity::class,
        SubjectiveAnswerEntity::class,
        SessionEntity::class,

        FatigueResultEntity::class,

    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MetricsDatabase : RoomDatabase() {

    /** DAO для работы с метриками */
    abstract fun metricsDao(): MetricsDao

    /** DAO для работы с результатами */
    abstract fun fatigueDao(): FatigueDao

    /** DAO для работы с ответами на субъективные вопросы */
    abstract fun subjectiveAnswerDao(): SubjectiveAnswerDao

    /** DAO для работы с вопросами субъективного тестирования */
    abstract fun subjectiveQuestionDao(): SubjectiveQuestionDao

    /** DAO для работы с сессиями */
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: MetricsDatabase? = null

        /**
         * Получение экземпляра базы данных (синглтон)
         *
         * Использует паттерн Double-Checked Locking для потокобезопасности.
         *
         * @param context Контекст приложения
         * @return Экземпляр MetricsDatabase
         */
        fun getInstance(context: Context): MetricsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MetricsDatabase::class.java,
                    "metrics_database_v6"
                )
                    .fallbackToDestructiveMigration()  // При миграции пересоздаем БД
                    .addCallback(object : Callback() {
                        /**
                         * Заполнение базы данных вопросами при первом создании
                         */
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.subjectiveQuestionDao().insertAll(
                                        SubjectiveQuestionsProvider.getQuestions()
                                    )
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}