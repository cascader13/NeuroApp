package com.neuroproject.neuro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MetricsDao {


    @Insert
    suspend fun insertCalibrationData(history: CalibrationHistoryEntity)
    @Insert
    suspend fun insertNFBMetric(metric: NFBMetricEntity)

    @Insert
    suspend fun insertNFBCompressedMetric(metric: NFBMetricCompressedEntity)

    @Insert
    suspend fun insertEEGRAWMetric(metric: EEGRawMetricEntity)

    @Insert
    suspend fun insertEEGRAWCompressedMetric(metric: EEGRawMetricCompressedEntity)

    @Insert
    suspend fun insertEEGPROCEEDMetric(metric: EEGProceedMetricEntity)

    @Insert
    suspend fun insertEEGPROCEEDCompressedMetric(metric: EEGProceedMetricCompressedEntity)

    @Insert
    suspend fun insertEEGArtifactsMetric(metric: EEGArtifactsMetricEntity)

    @Insert
    suspend fun insertEEGArtifactsCompressedMetric(metric: EEGArtifactsMetricCompressedEntity)
    @Insert
    suspend fun insertPhysiologicalMetric(metric: PhysiologicalMetricEntity)

    @Insert
    suspend fun insertPhysiologicalCompressedMetric(metric: PhysiologicalMetricCompressedEntity)

    @Insert
    suspend fun insertMEMSMetric(metric: MEMSMetricEntity)

    @Insert
    suspend fun insertMEMSCompressedMetric(metric: MEMSMetricCompressedEntity)

    @Insert
    suspend fun insertProductivityMetric(metric: ProductivityMetricEntity)

    @Insert
    suspend fun insertProductivityCompressedMetric(metric: ProductivityMetricCompressedEntity)

    @Insert
    suspend fun insertEmotionalMetric(metric: EmotionalMetricEntity)

    @Insert
    suspend fun insertEmotionalCompressedMetric(metric: EmotionalMetricCompressedEntity)

    @Insert
    suspend fun insertCardioMetric(metric: CardioMetricEntity)

    @Insert
    suspend fun insertCardioCompressedMetric(metric: CardioMetricCompressedEntity)


    @Query("SELECT COUNT(*) FROM nfb_metrics")
    suspend fun getNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM nfb_metrics_compressed")
    suspend fun getNFBMetricsCompressedCount(): Int


    @Query("SELECT COUNT(*) FROM EEG_Raw_metrics")
    suspend fun getEEGRAWMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM EEG_Raw_metrics_compressed")
    suspend fun getEEGRAWMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM EEG_Proceed_metrics")
    suspend fun getEEGPROCEEDMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM EEG_Proceed_metrics_compressed")
    suspend fun getEEGPROCEEDMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM EEG_Artifacts_metrics")
    suspend fun getEEGArtifactMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM EEG_Artifacts_metrics_compressed")
    suspend fun getEEGArtifactMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics")
    suspend fun getPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics_compressed")
    suspend fun getPhysiologicalMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics")
    suspend fun getMEMSMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics_compressed")
    suspend fun getMEMSMetricsCompressedCount(): Int


    @Query("SELECT *  FROM calibration_history WHERE user_id = :user_id ORDER BY id DESC LIMIT 1")
    suspend fun getCalibration(user_id: String): List<CalibrationHistoryEntity>



    @Query("DELETE FROM EEG_Raw_metrics")
    suspend fun clearEEGRAW()

    @Query("DELETE FROM EEG_Raw_metrics_compressed")
    suspend fun clearEEGRAWCompressed()

    @Query("DELETE FROM EEG_Proceed_metrics")
    suspend fun clearEEGPROCEED()

    @Query("DELETE FROM EEG_Proceed_metrics_compressed")
    suspend fun clearEEGPROCEEDCompressed()

    @Query("DELETE FROM EEG_Artifacts_metrics")
    suspend fun clearEEGArtifacts()

    @Query("DELETE FROM EEG_Artifacts_metrics_compressed")
    suspend fun clearEEGArtifactsCompressed()
    @Query("DELETE FROM nfb_metrics")
    suspend fun clearNFBMetrics()

    @Query("DELETE FROM nfb_metrics_compressed")
    suspend fun clearNFBMetricsCompressed()

    @Query("DELETE FROM physiological_metrics")
    suspend fun clearPhysiologicalMetrics()

    @Query("DELETE FROM physiological_metrics_compressed")
    suspend fun clearPhysiologicalMetricsCompressed()

    @Query("DELETE FROM mems_metrics")
    suspend fun clearMEMSMetrics()

    @Query("DELETE FROM mems_metrics_compressed")
    suspend fun clearMEMSMetricsCompressed()

    @Query("DELETE FROM productivity_metrics")
    suspend fun clearProductivityMetrics()

    @Query("DELETE FROM productivity_metrics_compressed")
    suspend fun clearProductivityMetricsCompressed()

    @Query("DELETE FROM emotional_metrics")
    suspend fun clearEmotionalMetrics()

    @Query("DELETE FROM emotional_metrics_compressed")
    suspend fun clearEmotionalMetricsCompressed()

    @Query("DELETE FROM cardio_metrics")
    suspend fun clearCardioMetrics()

    @Query("DELETE FROM cardio_metrics_compressed")
    suspend fun clearCardioMetricsCompressed()


    // Получение непомеченных данных
    @Query("SELECT * FROM nfb_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetrics(): List<NFBMetricEntity>

    @Query("SELECT * FROM nfb_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetricsCompressed(): List<NFBMetricCompressedEntity>

    @Query("SELECT * FROM EEG_Raw_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEEGRAWMetrics(): List<EEGRawMetricEntity>

    @Query("SELECT * FROM EEG_Raw_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEEGRAWMetricsCompressed(): List<EEGRawMetricCompressedEntity>

    @Query("SELECT * FROM EEG_Proceed_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEEGPROCEEDMetrics(): List<EEGProceedMetricEntity>

    @Query("SELECT * FROM EEG_Proceed_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEEGPROCEEDMetricsCompressed(): List<EEGProceedMetricCompressedEntity>

    @Query("SELECT * FROM EEG_Artifacts_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEEGArtifactsMetrics(): List<EEGArtifactsMetricEntity>

    @Query("SELECT * FROM EEG_Artifacts_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEEGArtifactsMetricsCompressed(): List<EEGArtifactsMetricCompressedEntity>

    @Query("SELECT * FROM physiological_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetrics(): List<PhysiologicalMetricEntity>

    @Query("SELECT * FROM physiological_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetricsCompressed(): List<PhysiologicalMetricCompressedEntity>

    @Query("SELECT * FROM mems_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetrics(): List<MEMSMetricEntity>

    @Query("SELECT * FROM mems_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetricsCompressed(): List<MEMSMetricCompressedEntity>

    @Query("SELECT * FROM productivity_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetrics(): List<ProductivityMetricEntity>

    @Query("SELECT * FROM productivity_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetricsCompressed(): List<ProductivityMetricCompressedEntity>

    @Query("SELECT * FROM emotional_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetrics(): List<EmotionalMetricEntity>

    @Query("SELECT * FROM emotional_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetricsCompressed(): List<EmotionalMetricCompressedEntity>

    @Query("SELECT * FROM cardio_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetrics(): List<CardioMetricEntity>

    @Query("SELECT * FROM cardio_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetricsCompressed(): List<CardioMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM nfb_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM nfb_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM eeg_raw_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEEGRAWMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM eeg_raw_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEEGRAWMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM eeg_proceed_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEEGPROCEEDMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM eeg_proceed_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEEGPROCEEDMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM eeg_artifacts_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEEGArtifactMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM eeg_artifacts_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEEGArtifactMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetricsCountCompressed(): Int

    @Query("UPDATE nfb_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markNFBMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE nfb_metrics_compressed SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markNFBMetricsCompressedAsSynced(timestamps: List<Long>)


    @Query("UPDATE physiological_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markPhysiologicalMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE physiological_metrics_compressed SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markPhysiologicalMetricsCompressedAsSynced(timestamps: List<Long>)

    @Query("UPDATE mems_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markMEMSMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE mems_metrics_compressed SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markMEMSMetricsCompressedAsSynced(timestamps: List<Long>)

    @Query("UPDATE EEG_Raw_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markEEGRAWMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE EEG_Raw_metrics_compressed SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markEEGRAWMetricsCompressedAsSynced(timestamps: List<Long>)

    @Query("UPDATE EEG_Proceed_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markEEGProceedMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE EEG_Proceed_metrics_compressed SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markEEGProceedMetricsCompressedAsSynced(timestamps: List<Long>)

    @Query("UPDATE eeg_artifacts_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markEEGArtifactsMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE eeg_artifacts_metrics_compressed SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markEEGArtifactsMetricsCompressedAsSynced(timestamps: List<Long>)

    @Query("UPDATE productivity_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markProductivityMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE productivity_metrics_compressed SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markProductivityMetricsCompressedAsSynced(timestamps: List<Long>)
    @Query("UPDATE emotional_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markEmotionalMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE emotional_metrics_compressed SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markEmotionalMetricsCompressedAsSynced(timestamps: List<Long>)

    @Query("UPDATE cardio_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markCardioMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE cardio_metrics_compressed SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markCardioMetricsCompressedAsSynced(timestamps: List<Long>)

    @Query("SELECT * FROM nfb_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedNFBMetricsBatch(limit: Int): List<NFBMetricEntity>

    @Query("SELECT * FROM nfb_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedNFBMetricsBatchCompressed(limit: Int): List<NFBMetricCompressedEntity>

    @Query("SELECT * FROM EEG_Raw_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEEGRAWMetricsBatch(limit: Int): List<EEGRawMetricEntity>

    @Query("SELECT * FROM EEG_Raw_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEEGRAWMetricsBatchCompressed(limit: Int): List<EEGRawMetricCompressedEntity>

    @Query("SELECT * FROM EEG_Proceed_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEEGPROCEEDMetricsBatch(limit: Int): List<EEGProceedMetricEntity>

    @Query("SELECT * FROM EEG_Proceed_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEEGPROCEEDMetricsBatchCompressed(limit: Int): List<EEGProceedMetricCompressedEntity>

    @Query("SELECT * FROM EEG_Artifacts_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEEGArtifactsMetricsBatch(limit: Int): List<EEGArtifactsMetricEntity>

    @Query("SELECT * FROM EEG_Artifacts_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEEGArtifactsMetricsBatchCompressed(limit: Int): List<EEGArtifactsMetricCompressedEntity>

    @Query("SELECT * FROM physiological_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedPhysiologicalMetricsBatch(limit: Int): List<PhysiologicalMetricEntity>

    @Query("SELECT * FROM physiological_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedPhysiologicalMetricsBatchCompressed(limit: Int): List<PhysiologicalMetricCompressedEntity>

    @Query("SELECT * FROM mems_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedMEMSMetricsBatch(limit: Int): List<MEMSMetricEntity>

    @Query("SELECT * FROM mems_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedMEMSMetricsBatchCompressed(limit: Int): List<MEMSMetricCompressedEntity>

    @Query("SELECT * FROM productivity_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedProductivityMetricsBatch(limit: Int): List<ProductivityMetricEntity>

    @Query("SELECT * FROM productivity_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedProductivityMetricsBatchCompressed(limit: Int): List<ProductivityMetricCompressedEntity>

    @Query("SELECT * FROM emotional_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEmotionalMetricsBatch(limit: Int): List<EmotionalMetricEntity>

    @Query("SELECT * FROM emotional_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEmotionalMetricsBatchCompressed(limit: Int): List<EmotionalMetricCompressedEntity>

    @Query("SELECT * FROM cardio_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedCardioMetricsBatch(limit: Int): List<CardioMetricEntity>

    @Query("SELECT * FROM cardio_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedCardioMetricsBatchCompressed(limit: Int): List<CardioMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM nfb_metrics")
    suspend fun getAllNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM nfb_metrics_compressed")
    suspend fun getAllNFBMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM EEG_Raw_metrics")
    suspend fun getAllEEGRAWMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM EEG_Raw_metrics_compressed")
    suspend fun getAllEEGRAWMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM EEG_Proceed_metrics")
    suspend fun getAllEEGPROCEEDMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM EEG_Proceed_metrics_compressed")
    suspend fun getAllEEGPROCEEDMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM EEG_Artifacts_metrics")
    suspend fun getAllEEGArtifactsMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM EEG_Artifacts_metrics_compressed")
    suspend fun getAllEEGArtifactsMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics")
    suspend fun getAllPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics_compressed")
    suspend fun getAllPhysiologicalMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics")
    suspend fun getAllMEMSMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics_compressed")
    suspend fun getAllMEMSMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics")
    suspend fun getAllProductivityMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics_compressed")
    suspend fun getAllProductivityMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics")
    suspend fun getAllEmotionalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics_compressed")
    suspend fun getAllEmotionalMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics")
    suspend fun getAllCardioMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics_compressed")
    suspend fun getAllCardioMetricsCountCompressed(): Int

    // Получить временные метки последних данных
    @Query("SELECT MAX(timestamp) FROM nfb_metrics")
    suspend fun getLastNFBMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM nfb_metrics_compressed")
    suspend fun getLastNFBMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM EEG_Raw_metrics")
    suspend fun getLastEEGRAWMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM EEG_Raw_metrics_compressed")
    suspend fun getLastEEGRAWMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM EEG_Proceed_metrics")
    suspend fun getLastEEGPROCEEDMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM EEG_Proceed_metrics_compressed")
    suspend fun getLastEEGPROCEEDMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM EEG_Artifacts_metrics")
    suspend fun getLastEEGArtifactMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM EEG_Artifacts_metrics_compressed")
    suspend fun getLastEEGArtifactMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM physiological_metrics")
    suspend fun getLastPhysiologicalMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM physiological_metrics_compressed")
    suspend fun getLastPhysiologicalMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM mems_metrics")
    suspend fun getLastMEMSMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM mems_metrics_compressed")
    suspend fun getLastMEMSMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM productivity_metrics")
    suspend fun getLastProductivityMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM productivity_metrics_compressed")
    suspend fun getLastProductivityMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM emotional_metrics")
    suspend fun getLastEmotionalMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM emotional_metrics_compressed")
    suspend fun getLastEmotionalMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM cardio_metrics")
    suspend fun getLastCardioMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM cardio_metrics_compressed")
    suspend fun getLastCardioMetricTimestampCompressed(): Long?


    @Transaction
    suspend fun safeMarkNFBMetricsAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markNFBMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkNFBMetricsCompressedAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markNFBMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkPhysiologicalMetricsAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markPhysiologicalMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkPhysiologicalMetricsCompressedAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markPhysiologicalMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkMEMSMetricsAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markMEMSMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkMEMSMetricsCompressedAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markMEMSMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGRAWMetricsAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markEEGRAWMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGRAWMetricsCompressedAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markEEGRAWMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGProceedMetricsAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markEEGProceedMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGProceedMetricsCompressedAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markEEGProceedMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGArtifactsMetricsAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markEEGArtifactsMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGArtifactsMetricsCompressedAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markEEGArtifactsMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkProductivityMetricsAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markProductivityMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkProductivityMetricsCompressedAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markProductivityMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEmotionalMetricsAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markEmotionalMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEmotionalMetricsCompressedAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markEmotionalMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkCardioMetricsAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markCardioMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkCardioMetricsCompressedAsSynced(timestamps: List<Long>) {
        val BATCH_SIZE = 500
        timestamps.chunked(BATCH_SIZE).forEach { batch ->
            markCardioMetricsCompressedAsSynced(batch)
        }
    }


}