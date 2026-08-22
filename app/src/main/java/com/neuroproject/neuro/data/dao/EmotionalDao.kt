package com.neuroproject.neuro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.neuroproject.neuro.data.entity.*

/**
 * DAO для доступа к эмоциональным метрикам пользователя.
 */
@Dao
interface EmotionalDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmotionalMetric(metric: EmotionalMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmotionalCompressedMetric(metric: EmotionalMetricCompressedEntity)

    @Query("DELETE FROM emotional_metrics")
    suspend fun clearEmotionalMetrics()

    @Query("DELETE FROM emotional_metrics WHERE sessionId = :sessionId")
    suspend fun clearEmotionalMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM emotional_metrics_compressed")
    suspend fun clearEmotionalMetricsCompressed()

    @Query("DELETE FROM emotional_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearEmotionalMetricsCompressedBySessionId(sessionId: Long)

    @Query("SELECT * FROM emotional_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetrics(): List<EmotionalMetricEntity>

    @Query("SELECT * FROM emotional_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetricsCompressed(): List<EmotionalMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM emotional_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetricsCountCompressed(): Int

    @Query("UPDATE emotional_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEmotionalMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE emotional_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEmotionalMetricsCompressedAsSynced(rowIds: List<Long>)

    @Transaction
    suspend fun safeMarkEmotionalMetricsAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markEmotionalMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkEmotionalMetricsCompressedAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markEmotionalMetricsCompressedAsSynced(batch)
        }
    }

    @Query("SELECT * FROM emotional_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEmotionalMetricsBatch(limit: Int): List<EmotionalMetricEntity>

    @Query("SELECT * FROM emotional_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEmotionalMetricsBatchCompressed(limit: Int): List<EmotionalMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM emotional_metrics")
    suspend fun getEmotionalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics_compressed")
    suspend fun getEmotionalMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics")
    suspend fun getAllEmotionalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics_compressed")
    suspend fun getAllEmotionalMetricsCountCompressed(): Int

    @Query("SELECT MAX(timestamp) FROM emotional_metrics")
    suspend fun getLastEmotionalMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM emotional_metrics_compressed")
    suspend fun getLastEmotionalMetricTimestampCompressed(): Long?

    @Query("""
        SELECT * FROM emotional_metrics 
        WHERE sessionId = :sessionId 
        AND timestamp >= :minuteStart 
        AND timestamp <= :minuteEnd
        ORDER BY timestamp ASC
    """)
    suspend fun getEmotionalMetricsForMinute(
        sessionId: Long,
        minuteStart: Long,
        minuteEnd: Long
    ): List<EmotionalMetricEntity>

    @Query("SELECT * FROM emotional_metrics_compressed WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getEmotionalCompressedMetrics(sessionId: Long) : List<EmotionalMetricCompressedEntity>

    @Query("SELECT timestamp FROM emotional_metrics  WHERE sessionId = :sessionId  ORDER BY timestamp ASC LIMIT 1")
    suspend fun getEmotionalStartTimestamp(sessionId: Long) : Long
}
