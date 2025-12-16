// [file name]: MetricsDao.kt (расширенная версия)
package com.neuroproject.neuro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MetricsDao {

    // Существующие методы
    @Insert
    suspend fun insertUsers(session: UsersEntity)
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

    @Query("SELECT COUNT(*) FROM Users")
    suspend fun getUsersCount(): Int

    @Query("SELECT COUNT(*) FROM nfb_metrics")
    suspend fun getNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics")
    suspend fun getPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics")
    suspend fun getMEMSMetricsCount(): Int

    @Query("DELETE FROM Users")
    suspend fun clearUsers()

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

    // НОВЫЕ МЕТОДЫ ДЛЯ ВЫГРУЗКИ

    // Получение непомеченных данных
    @Query("SELECT * FROM nfb_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetrics(): List<NFBMetricEntity>

    @Query("SELECT * FROM physiological_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetrics(): List<PhysiologicalMetricEntity>

    @Query("SELECT * FROM mems_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetrics(): List<MEMSMetricEntity>

    @Query("SELECT * FROM productivity_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetrics(): List<ProductivityMetricEntity>

    @Query("SELECT * FROM emotional_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetrics(): List<EmotionalMetricEntity>

    @Query("SELECT * FROM cardio_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetrics(): List<CardioMetricEntity>

    // Подсчет непомеченных данных
    @Query("SELECT COUNT(*) FROM nfb_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedMEMSMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedEmotionalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics WHERE isMarked = 0")
    suspend fun getUnmarkedCardioMetricsCount(): Int

    // Пометить данные как отправленные
    @Query("UPDATE nfb_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markNFBMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE physiological_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markPhysiologicalMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE mems_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markMEMSMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE productivity_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markProductivityMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE emotional_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markEmotionalMetricsAsSynced(timestamps: List<Long>)

    @Query("UPDATE cardio_metrics SET isMarked = 1 WHERE timestamp IN (:timestamps)")
    suspend fun markCardioMetricsAsSynced(timestamps: List<Long>)

    // Получить данные с лимитом (для пакетной отправки)
    @Query("SELECT * FROM nfb_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedNFBMetricsBatch(limit: Int): List<NFBMetricEntity>

    @Query("SELECT * FROM physiological_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedPhysiologicalMetricsBatch(limit: Int): List<PhysiologicalMetricEntity>

    @Query("SELECT * FROM mems_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedMEMSMetricsBatch(limit: Int): List<MEMSMetricEntity>

    @Query("SELECT * FROM productivity_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedProductivityMetricsBatch(limit: Int): List<ProductivityMetricEntity>

    @Query("SELECT * FROM emotional_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedEmotionalMetricsBatch(limit: Int): List<EmotionalMetricEntity>

    @Query("SELECT * FROM cardio_metrics WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedCardioMetricsBatch(limit: Int): List<CardioMetricEntity>

    // Статистика по всем данным
    @Query("SELECT COUNT(*) FROM nfb_metrics")
    suspend fun getAllNFBMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_metrics")
    suspend fun getAllPhysiologicalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM mems_metrics")
    suspend fun getAllMEMSMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_metrics")
    suspend fun getAllProductivityMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM emotional_metrics")
    suspend fun getAllEmotionalMetricsCount(): Int

    @Query("SELECT COUNT(*) FROM cardio_metrics")
    suspend fun getAllCardioMetricsCount(): Int

    // Получить временные метки последних данных
    @Query("SELECT MAX(timestamp) FROM nfb_metrics")
    suspend fun getLastNFBMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM physiological_metrics")
    suspend fun getLastPhysiologicalMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM mems_metrics")
    suspend fun getLastMEMSMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM productivity_metrics")
    suspend fun getLastProductivityMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM emotional_metrics")
    suspend fun getLastEmotionalMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM cardio_metrics")
    suspend fun getLastCardioMetricTimestamp(): Long?
}