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

@Database(
    entities = [
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
        EEGRawMetricEntity::class,
        EEGRawMetricCompressedEntity::class,
        EEGProceedMetricEntity::class,
        EEGProceedMetricCompressedEntity::class,
        EEGArtifactsMetricEntity::class,
        EEGArtifactsMetricCompressedEntity::class,
        CardioMetricEntity::class,
        CardioMetricCompressedEntity::class,
        SubjectiveQuestionEntity::class,
        CalibrationHistoryEntity::class,
        SubjectiveAnswerEntity::class,
        SessionEntity::class
    ],
    version = 2,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class MetricsDatabase : RoomDatabase() {
    abstract fun metricsDao(): MetricsDao
    abstract fun subjectiveAnswerDao(): SubjectiveAnswerDao
    abstract fun subjectiveQuestionDao(): SubjectiveQuestionDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: MetricsDatabase? = null

        fun getInstance(context: Context): MetricsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MetricsDatabase::class.java,
                    "metrics_database_v4"
                )
                .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // При создании БД заполняем вопросами
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