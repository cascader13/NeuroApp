package com.neuroproject.neuro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.neuroproject.neuro.data.entity.*

/**
 * DAO для доступа к метрикам продуктивности, индексам и базовым значениям.
 */
@Dao
interface ProductivityDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProductivityMetric(metric: ProductivityMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProductivityCompressedMetric(metric: ProductivityMetricCompressedEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProductivityIndex(index: ProductivityIndexesEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProductivityBaselines(baseline: ProductivityBaselinesEntity)

    @Query("DELETE FROM productivity_metrics")
    suspend fun clearProductivityMetrics()

    @Query("DELETE FROM productivity_metrics WHERE sessionId = :sessionId")
    suspend fun clearProductivityMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM productivity_metrics_compressed")
    suspend fun clearProductivityMetricsCompressed()

    @Query("DELETE FROM productivity_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearProductivityMetricsCompressedBySessionId(sessionId: Long)

    @Query("DELETE FROM productivity_indexes")
    suspend fun clearProductivityIndexes()

    @Query("DELETE FROM productivity_indexes WHERE sessionId = :sessionId")
    suspend fun clearProductivityIndexesBySessionId(sessionId: Long)

    @Query("DELETE FROM productivity_baselines")
    suspend fun clearProductivityBaseline()

    @Query("DELETE FROM productivity_baselines WHERE sessionId = :sessionId")
    suspend fun clearProductivityBaselinesBySessionId(sessionId: Long)

    @Query("SELECT * FROM productivity_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetrics(): List<ProductivityMetricEntity>

    @Query("SELECT * FROM productivity_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetricsCompressed(): List<ProductivityMetricCompressedEntity>

    @Query("SELECT * FROM productivity_indexes WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityIndexes(): List<ProductivityIndexesEntity>

    @Query("SELECT * FROM productivity_baselines WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityBaseline(): List<ProductivityBaselinesEntity>

    @Query("SELECT COUNT(*) FROM productivity_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetricsCountCompressed(): Int

    @Query("UPDATE productivity_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markProductivityMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE productivity_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markProductivityMetricsCompressedAsSynced(rowIds: List<Long>)

    @Query("UPDATE productivity_indexes SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markProductivityIndexAsSynced(rowIds: List<Long>)

    @Query("UPDATE productivity_baselines SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markProductivityBaselineAsSynced(rowIds: List<Long>)

    @Transaction
    suspend fun safeMarkProductivityMetricsAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markProductivityMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkProductivityMetricsCompressedAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markProductivityMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkProductivityIndexesAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markProductivityIndexAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkProductivityBaselineAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markProductivityBaselineAsSynced(batch)
        }
    }

    @Query("SELECT * FROM productivity_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedProductivityMetricsBatch(limit: Int): List<ProductivityMetricEntity>

    @Query("SELECT * FROM productivity_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedProductivityMetricsBatchCompressed(limit: Int): List<ProductivityMetricCompressedEntity>

    @Query("SELECT * FROM productivity_indexes WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedProductivityIndexesBatch(limit: Int): List<ProductivityIndexesEntity>

    @Query("SELECT * FROM productivity_baselines WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedProductivityBaselineBatch(limit: Int): List<ProductivityBaselinesEntity>

    @Query("SELECT COUNT(*) FROM productivity_metrics")
    suspend fun getProductivityMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics_compressed")
    suspend fun getProductivityMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics")
    suspend fun getAllProductivityMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics_compressed")
    suspend fun getAllProductivityMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM productivity_indexes")
    suspend fun getAllProductivityIndexesCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_baselines")
    suspend fun getAllProductivityBaselineCount(): Int

    @Query("SELECT MAX(timestamp) FROM productivity_metrics")
    suspend fun getLastProductivityMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM productivity_metrics_compressed")
    suspend fun getLastProductivityMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM productivity_indexes")
    suspend fun getLastProductivityIndexTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM productivity_baselines")
    suspend fun getLastProductivityBaselineTimestamp(): Long?

    @Query("SELECT * FROM productivity_metrics WHERE sessionId = :sessionId AND timestamp >= :minuteStart AND timestamp <= :minuteEnd ORDER BY timestamp ASC")
    suspend fun getProductivityMetricsForMinute(sessionId: Long, minuteStart: Long, minuteEnd: Long) : List<ProductivityMetricEntity>

    @Query("SELECT * FROM productivity_baselines WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getProductivityBaselines(sessionId: Long) : ProductivityBaselinesEntity

    @Query("SELECT * FROM productivity_indexes WHERE id = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastProductivityIndexForUser(userId: String) : ProductivityIndexesEntity

    @Query("SELECT * FROM productivity_indexes WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getProductivityIndexes(sessionId: Long) : ProductivityIndexesEntity

    @Query("SELECT timestamp FROM productivity_metrics  WHERE sessionId = :sessionId  ORDER BY timestamp ASC LIMIT 1")
    suspend fun getProductivityStartTimestamp(sessionId: Long) : Long

    @Query("SELECT * FROM productivity_metrics_compressed WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getProductivityCompressedMetrics(sessionId: Long) : List<ProductivityMetricCompressedEntity>
}
