package com.neuroproject.neuro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.neuroproject.neuro.data.entity.*

/**
 * DAO для доступа к физиологическим метрикам и базовым значениям.
 */
@Dao
interface PhysiologicalDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhysiologicalMetric(metric: PhysiologicalMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhysiologicalCompressedMetric(metric: PhysiologicalMetricCompressedEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhysiologicalBaselines(baseline: PhysiologicalBaselinesEntity)

    @Query("DELETE FROM physiological_metrics")
    suspend fun clearPhysiologicalMetrics()

    @Query("DELETE FROM physiological_metrics WHERE sessionId = :sessionId")
    suspend fun clearPhysiologicalMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM physiological_metrics_compressed")
    suspend fun clearPhysiologicalMetricsCompressed()

    @Query("DELETE FROM physiological_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearPhysiologicalMetricsCompressedBySessionId(sessionId: Long)

    @Query("DELETE FROM physiological_baselines")
    suspend fun clearPhysiologicalBaseline()

    @Query("DELETE FROM physiological_baselines WHERE sessionId = :sessionId")
    suspend fun clearPhysiologicalBaselinesBySessionId(sessionId: Long)

    @Query("SELECT * FROM physiological_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetrics(): List<PhysiologicalMetricEntity>

    @Query("SELECT * FROM physiological_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetricsCompressed(): List<PhysiologicalMetricCompressedEntity>

    @Query("SELECT * FROM physiological_baselines WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalBaseline(): List<PhysiologicalBaselinesEntity>

    @Query("SELECT COUNT(*) FROM physiological_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetricsCountCompressed(): Int

    @Query("UPDATE physiological_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markPhysiologicalMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE physiological_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markPhysiologicalMetricsCompressedAsSynced(rowIds: List<Long>)

    @Query("UPDATE physiological_baselines SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markPhysiologicalBaselineAsSynced(rowIds: List<Long>)

    @Transaction
    suspend fun safeMarkPhysiologicalMetricsAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markPhysiologicalMetricsAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkPhysiologicalMetricsCompressedAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markPhysiologicalMetricsCompressedAsSynced(batch)
        }
    }

    @Transaction
    suspend fun safeMarkPhysiologicalBaselineAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markPhysiologicalBaselineAsSynced(batch)
        }
    }

    @Query("SELECT * FROM physiological_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedPhysiologicalMetricsBatch(limit: Int): List<PhysiologicalMetricEntity>

    @Query("SELECT * FROM physiological_metrics_compressed WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedPhysiologicalMetricsBatchCompressed(limit: Int): List<PhysiologicalMetricCompressedEntity>

    @Query("SELECT * FROM physiological_baselines WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedPhysiologicalBaselineBatch(limit: Int): List<PhysiologicalBaselinesEntity>

    @Query("SELECT COUNT(*) FROM physiological_metrics")
    suspend fun getPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics_compressed")
    suspend fun getPhysiologicalMetricsCompressedCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics")
    suspend fun getAllPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics_compressed")
    suspend fun getAllPhysiologicalMetricsCountCompressed(): Int

    @Query("SELECT COUNT(*) FROM physiological_baselines")
    suspend fun getAllPhysiologicalBaselineCount(): Int

    @Query("SELECT MAX(timestamp) FROM physiological_metrics")
    suspend fun getLastPhysiologicalMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM physiological_metrics_compressed")
    suspend fun getLastPhysiologicalMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM physiological_baselines")
    suspend fun getLastPhysiologicalBaselineTimestamp(): Long?

    @Query("""
        SELECT * FROM physiological_metrics 
        WHERE sessionId = :sessionId 
        AND timestamp >= :minuteStart 
        AND timestamp <= :minuteEnd
        ORDER BY timestamp ASC
    """)
    suspend fun getPhysiologicalMetricsForMinute(
        sessionId: Long,
        minuteStart: Long,
        minuteEnd: Long
    ): List<PhysiologicalMetricEntity>

    @Query("SELECT * FROM physiological_metrics_compressed WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPhysiologicalCompressedMetrics(sessionId: Long) : List<PhysiologicalMetricCompressedEntity>

    @Query("SELECT timestamp FROM physiological_metrics  WHERE sessionId = :sessionId  ORDER BY timestamp ASC LIMIT 1")
    suspend fun getPhysiologicalStartTimestamp(sessionId: Long) : Long
}
