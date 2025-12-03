package com.neuroproject.neuro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MetricsDao {

    @Insert
    suspend fun insertNFBMetric(metric: NFBMetricEntity)

    @Insert
    suspend fun insertPhysiologicalMetric(metric: PhysiologicalMetricEntity)

    @Insert
    suspend fun insertMEMSMetric(metric: MEMSMetricEntity)

    @Insert
    suspend fun insertProductivityMetric(metric: ProductivityMetricEntity)

    @Insert
    suspend fun insertEmotionalMetric(metric: EmotionalMetricEntity)

    @Insert
    suspend fun insertCardioMetric(metric: CardioMetricEntity)

    @Query("SELECT COUNT(*) FROM nfb_metrics")
    suspend fun getNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics")
    suspend fun getPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics")
    suspend fun getMEMSMetricsCount(): Int

    @Query("DELETE FROM nfb_metrics")
    suspend fun clearNFBMetrics()

    @Query("DELETE FROM physiological_metrics")
    suspend fun clearPhysiologicalMetrics()

    @Query("DELETE FROM mems_metrics")
    suspend fun clearMEMSMetrics()

    @Query("DELETE FROM productivity_metrics")
    suspend fun clearProductivityMetrics()

    @Query("DELETE FROM emotional_metrics")
    suspend fun clearEmotionalMetrics()

    @Query("DELETE FROM cardio_metrics")
    suspend fun clearCardioMetrics()
}