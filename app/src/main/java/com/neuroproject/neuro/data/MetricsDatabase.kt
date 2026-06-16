package com.neuroproject.neuro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.neuroproject.neuro.data.dao.*
import com.neuroproject.neuro.data.entity.*
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

private const val DATABASE_NAME = "metrics_database_v7"

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
 * - **version = 5** - текущая версия схемы
 * - **exportSchema = true** - схемы экспортируются в app/schemas/
 * - **fallbackToDestructiveMigration()** - только в DEBUG сборках
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
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MetricsDatabase : RoomDatabase() {

    /** Legacy DAO для работы с метриками (постепенно заменяется на доменные DAO) */
    abstract fun metricsDao(): MetricsDao

    /** DAO для работы с результатами усталости */
    abstract fun fatigueDao(): FatigueDao

    /** DAO для работы с ответами на субъективные вопросы */
    abstract fun subjectiveAnswerDao(): SubjectiveAnswerDao

    /** DAO для работы с вопросами субъективного тестирования */
    abstract fun subjectiveQuestionDao(): SubjectiveQuestionDao

    /** DAO для работы с сессиями */
    abstract fun sessionDao(): SessionDao

    // === Доменные DAO ===
    abstract fun calibrationDao(): CalibrationDao
    abstract fun nfbDao(): NfbDao
    abstract fun eegDao(): EegDao
    abstract fun physiologicalDao(): PhysiologicalDao
    abstract fun memsDao(): MemsDao
    abstract fun productivityDao(): ProductivityDao
    abstract fun emotionalDao(): EmotionalDao
    abstract fun cardioDao(): CardioDao

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
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    MetricsDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(object : Callback() {
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

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN id TEXT")
                db.execSQL("ALTER TABLE sessions ADD COLUMN expedition_id TEXT")
                db.execSQL("""
                    UPDATE sessions 
                    SET 
                    id = (SELECT id FROM nfb_metrics WHERE nfb_metrics.sessionId = sessions.sessionId),
                    expedition_id = (SELECT expedition_id FROM nfb_metrics WHERE nfb_metrics.sessionId = sessions.sessionId)
                    WHERE EXISTS (SELECT 1 FROM nfb_metrics WHERE nfb_metrics.sessionId = sessions.sessionId)
                """)
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS fatigue_results (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sessionId INTEGER NOT NULL,
                        minuteIndex INTEGER NOT NULL,
                        cognitiveResult REAL NOT NULL,
                        physiologicalResult REAL NOT NULL,
                        psychologicalResult REAL NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS subjective_answers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sessionId INTEGER NOT NULL,
                        questionId INTEGER NOT NULL,
                        value INTEGER NOT NULL,
                        FOREIGN KEY (sessionId) REFERENCES sessions(sessionId) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subjective_answers_sessionId ON subjective_answers(sessionId)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_calibration_history_user_id ON Calibration_History(user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_id ON sessions(id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_expedition_id ON sessions(expedition_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fatigue_results_sessionId ON fatigue_results(sessionId)")
                db.execSQL("ALTER TABLE fatigue_results RENAME COLUMN physioligicalResult TO physiologicalResult")
                db.execSQL("ALTER TABLE fatigue_results RENAME COLUMN psychologicalResultval TO psychologicalResult")
            }
        }
    }
}