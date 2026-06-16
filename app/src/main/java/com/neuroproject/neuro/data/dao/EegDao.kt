package com.neuroproject.neuro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.neuroproject.neuro.data.entity.*

@Dao
interface EegDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEEGRAWMetric(metric: EEGRawMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEEGRAWCompressedMetric(metric: EEGRawMetricCompressedEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEEGPROCEEDMetric(metric: EEGProceedMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEEGPROCEEDCompressedMetric(metric: EEGProceedMetricCompressedEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEEGArtifactsMetric(metric: EEGArtifactsMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEEGArtifactsCompressedMetric(metric: EEGArtifactsMetricCompressedEntity)

    @Query("DELETE FROM EEG_Raw_metrics")
    suspend fun clearEEGRAW()

    @Query("DELETE FROM EEG_Raw_metrics WHERE sessionId = :sessionId")
    suspend fun clearEEGRAWBySessionId(sessionId: Long)

    @Query("DELETE FROM EEG_Raw_metrics_compressed")
    suspend fun clearEEGRAWCompressed()

    @Query("DELETE FROM EEG_Raw_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearEEGRAWCompressedBySessionId(sessionId: Long)

    @Query("DELETE FROM EEG_Proceed_metrics")
    suspend fun clearEEGPROCEED()

    @Query("DELETE FROM EEG_Proceed_metrics WHERE sessionId = :sessionId")
    suspend fun clearEEGPROCEEDBySessionId(sessionId: Long)

    @Query("DELETE FROM EEG_Proceed_metrics_compressed")
    suspend fun clearEEGPROCEEDCompressed()

    @Query("DELETE FROM EEG_Proceed_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearEEGPROCEEDCompressedBySessionId(sessionId: Long)

    @Query("DELETE FROM EEG_Artifacts_metrics")
    suspend fun clearEEGArtifacts()

    @Query("DELETE FROM EEG_Artifacts_metrics WHERE sessionId = :sessionId")
    suspend fun clearEEGArtifactsBySessionId(sessionId: Long)

    @Query("DELETE FROM EEG_Artifacts_metrics_compressed")
    suspend fun clearEEGArtifactsCompressed()

    @Query("DELETE FROM EEG_Artifacts_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearEEGArtifactsCompressedBySessionId(sessionId: Long)

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

    @Query("UPDATE EEG_Raw_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEEGRAWMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE EEG_Raw_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEEGRAWMetricsCompressedAsSynced(rowIds: List<Long>)

    @Query("UPDATE EEG_Proceed_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEEGProceedMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE EEG_Proceed_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEEGProceedMetricsCompressedAsSynced(rowIds: List<Long>)

    @Query("UPDATE eeg_artifacts_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEEGArtifactsMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE eeg_artifacts_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEEGArtifactsMetricsCompressedAsSynced(rowIds: List<Long>)

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

    @Transaction
    suspend fun safeMarkEEGRAWMetricsAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markEEGRAWMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGRAWMetricsCompressedAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markEEGRAWMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGProceedMetricsAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markEEGProceedMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGProceedMetricsCompressedAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markEEGProceedMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGArtifactsMetricsAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markEEGArtifactsMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEEGArtifactsMetricsCompressedAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markEEGArtifactsMetricsCompressedAsSynced(batch)
        }
    }
}
