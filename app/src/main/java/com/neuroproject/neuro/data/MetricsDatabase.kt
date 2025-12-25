package com.neuroproject.neuro.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context

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
        UsersEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MetricsDatabase : RoomDatabase() {
    abstract fun metricsDao(): MetricsDao

    companion object {
        @Volatile
        private var INSTANCE: MetricsDatabase? = null

        fun getInstance(context: Context): MetricsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MetricsDatabase::class.java,
                    "metrics_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}