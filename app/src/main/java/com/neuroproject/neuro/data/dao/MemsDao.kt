package com.neuroproject.neuro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.neuroproject.neuro.data.entity.*

@Dao
interface MemsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMEMSMetric(metric: MEMSMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMEMSCompressedMetric(metric: MEMSMetricCompressedEntity)

    @Query("DELETE FROM mems_metrics")
    suspend fun clearMEMSMetrics()

    @Query("DELETE FROM mems_metrics WHERE sessionId = :sessionId")
    suspend fun clearMEMSMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM mems_metrics_compressed")
    suspend fun clearMEMSMetricsCompressed()

    @Query("DELETE FROM mems_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearMEMSMetricsCompressedBySessionId(sessionId: Long)

    @Query("SELECT * FROM mems_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetrics(): List<MEMSMetricEntity>

    @Query("SELECT * FROM mems_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetricsCompressed(): List<MEMSMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM mems_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetricsCountCompressed(): Int

    @Query("UPDATE mems_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markMEMSMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE mems_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markMEMSMetricsCompressedAsSynced(rowIds: List<Long>)

    @Transaction
    suspend fun safeMarkMEMSMetricsAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markMEMSMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkMEMSMetricsCompressedAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markMEMSMetricsCompressedAsSynced(batch)
        }
    }

    @Query("SELECT * FROM mems_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedMEMSMetricsBatch(limit: Int): List<MEMSMetricEntity>

    @Query("SELECT * FROM mems_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedMEMSMetricsBatchCompressed(limit: Int): List<MEMSMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM mems_metrics")
    suspend fun getMEMSMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics_compressed")
    suspend fun getMEMSMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics")
    suspend fun getAllMEMSMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics_compressed")
    suspend fun getAllMEMSMetricsCountCompressed(): Int

    @Query("SELECT MAX(timestamp) FROM mems_metrics")
    suspend fun getLastMEMSMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM mems_metrics_compressed")
    suspend fun getLastMEMSMetricTimestampCompressed(): Long?
}
