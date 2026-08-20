package com.neuroproject.neuro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.neuroproject.neuro.data.entity.*

/**
 * DAO для доступа к данным нейрофидбека.
 */
@Dao
interface NfbDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNFBMetric(metric: NFBMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNFBCompressedMetric(metric: NFBMetricCompressedEntity)

    @Query("DELETE FROM nfb_metrics")
    suspend fun clearNFBMetrics()

    @Query("DELETE FROM nfb_metrics WHERE sessionId = :sessionId")
    suspend fun clearNFBMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM nfb_metrics_compressed")
    suspend fun clearNFBMetricsCompressed()

    @Query("DELETE FROM nfb_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearNFBMetricsCompressedBySessionId(sessionId: Long)

    @Query("SELECT * FROM nfb_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetrics(): List<NFBMetricEntity>

    @Query("SELECT * FROM nfb_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetricsCompressed(): List<NFBMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM nfb_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM nfb_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetricsCountCompressed(): Int

    @Query("UPDATE nfb_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markNFBMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE nfb_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markNFBMetricsCompressedAsSynced(rowIds: List<Long>)

    @Transaction
    suspend fun safeMarkNFBMetricsAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markNFBMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkNFBMetricsCompressedAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markNFBMetricsCompressedAsSynced(batch)
        }
    }

    @Query("SELECT * FROM nfb_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedNFBMetricsBatch(limit: Int): List<NFBMetricEntity>

    @Query("SELECT * FROM nfb_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedNFBMetricsBatchCompressed(limit: Int): List<NFBMetricCompressedEntity>

    @Query("SELECT COUNT(*) FROM nfb_metrics")
    suspend fun getNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM nfb_metrics_compressed")
    suspend fun getNFBMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM nfb_metrics")
    suspend fun getAllNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM nfb_metrics_compressed")
    suspend fun getAllNFBMetricsCountCompressed(): Int

    @Query("SELECT MAX(timestamp) FROM nfb_metrics")
    suspend fun getLastNFBMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM nfb_metrics_compressed")
    suspend fun getLastNFBMetricTimestampCompressed(): Long?
}
