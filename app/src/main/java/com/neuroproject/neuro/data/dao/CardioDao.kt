package com.neuroproject.neuro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.neuroproject.neuro.data.entity.*

@Dao
interface CardioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCardioMetric(metric: CardioMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCardioCompressedMetric(metric: CardioMetricCompressedEntity)

    @Query("DELETE FROM cardio_metrics")
    suspend fun clearCardioMetrics()

    @Query("DELETE FROM cardio_metrics WHERE sessionId = :sessionId")
    suspend fun clearCardioMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM cardio_metrics_compressed")
    suspend fun clearCardioMetricsCompressed()

    @Query("DELETE FROM cardio_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearCardioMetricsCompressedBySessionId(sessionId: Long)

    @Query("SELECT * FROM cardio_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetrics(): List<CardioMetricEntity>

    @Query("SELECT * FROM cardio_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetricsCompressed(): List<CardioMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM cardio_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetricsCountCompressed(): Int

    @Query("UPDATE cardio_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markCardioMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE cardio_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markCardioMetricsCompressedAsSynced(rowIds: List<Long>)

    @Transaction
    suspend fun safeMarkCardioMetricsAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markCardioMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkCardioMetricsCompressedAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markCardioMetricsCompressedAsSynced(batch)
        }
    }

    @Query("SELECT * FROM cardio_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedCardioMetricsBatch(limit: Int): List<CardioMetricEntity>

    @Query("SELECT * FROM cardio_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedCardioMetricsBatchCompressed(limit: Int): List<CardioMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM cardio_metrics")
    suspend fun getCardioMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics_compressed")
    suspend fun getCardioMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics")
    suspend fun getAllCardioMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics_compressed")
    suspend fun getAllCardioMetricsCountCompressed(): Int

    @Query("SELECT MAX(timestamp) FROM cardio_metrics")
    suspend fun getLastCardioMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM cardio_metrics_compressed")
    suspend fun getLastCardioMetricTimestampCompressed(): Long?
}
