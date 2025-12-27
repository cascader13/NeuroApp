package com.neuroproject.neuro.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.neuroproject.neuro.data.subtest.SubTestDao
import com.neuroproject.neuro.data.subtest.SubTestResultEntity
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionDao
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity

@Database(
    entities = [
        NFBMetricEntity::class,
        PhysiologicalMetricEntity::class,
        MEMSMetricEntity::class,
        ProductivityMetricEntity::class,
        EmotionalMetricEntity::class,
        EEGRawMetricEntity::class,
        EEGProceedMetricEntity::class,
        EEGArtifactsMetricEntity::class,
        CardioMetricEntity::class,
        UsersEntity::class,
        SubTestResultEntity::class,
        SubjectiveQuestionEntity::class,
    ],
    version = 2,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class MetricsDatabase : RoomDatabase() {
    abstract fun metricsDao(): MetricsDao
    abstract fun subTestDao(): SubTestDao
    abstract fun subjectiveQuestionDao(): SubjectiveQuestionDao

    companion object {
        @Volatile
        private var INSTANCE: MetricsDatabase? = null

        fun getInstance(context: Context): MetricsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MetricsDatabase::class.java,
                    "metrics_database_v2"
                ).fallbackToDestructiveMigrationOnDowngrade().build()
                INSTANCE = instance
                instance
            }
        }
    }
}