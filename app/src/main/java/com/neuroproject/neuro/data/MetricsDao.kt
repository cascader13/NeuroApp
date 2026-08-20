package com.neuroproject.neuro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.neuroproject.neuro.data.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы со всеми типами метрик.
 *
 * Содержит методы для:
 * - Вставки новых записей (все типы метрик)
 * - Получения данных по сессиям и временным интервалам
 * - Подсчёта записей и получения статистики
 * - Пометки данных как синхронизированных (isMarked)
 * - Очистки данных по сессиям или полностью
 *
 * Все методы являются потокобезопасными (suspend).
 */
@Dao
interface MetricsDao {


    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCalibrationData(history: CalibrationHistoryEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNFBMetric(metric: NFBMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNFBCompressedMetric(metric: NFBMetricCompressedEntity)

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
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhysiologicalMetric(metric: PhysiologicalMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhysiologicalCompressedMetric(metric: PhysiologicalMetricCompressedEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMEMSMetric(metric: MEMSMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMEMSCompressedMetric(metric: MEMSMetricCompressedEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProductivityIndex(index: ProductivityIndexesEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProductivityBaselines(baseline: ProductivityBaselinesEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhysiologicalBaselines(baseline: PhysiologicalBaselinesEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProductivityMetric(metric: ProductivityMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProductivityCompressedMetric(metric: ProductivityMetricCompressedEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmotionalMetric(metric: EmotionalMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmotionalCompressedMetric(metric: EmotionalMetricCompressedEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCardioMetric(metric: CardioMetricEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
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



    @Query("SELECT *  FROM calibration_history WHERE user_id = :user_id" +
            " AND productivityConcentration IS NOT NULL AND physiologicalAlpha IS NOT NULL " +
            "  ORDER BY id DESC LIMIT 1") // Нужно проверить хотя бы одной метрики каждой категории,для того чтобы удостовериться что есть все остальные
    suspend fun getCalibration(user_id: String): List<CalibrationHistoryEntity>

    @Query("UPDATE calibration_history SET productivityGravity = :gravity," +
            " productivityProductivity = :productivity," +
            " productivityFatigue = :fatigue," +
            " productivityReverseFatigue = :reverseFatigue," +
            " productivityRelaxation = :relaxation," +
            " productivityConcentration = :concentration WHERE user_id = :userId AND id = (SELECT MAX(id) FROM calibration_history)")
    suspend fun insertProductivityCalibration(userId: String, gravity: Float, productivity: Float, fatigue: Float, reverseFatigue: Float, relaxation: Float, concentration: Float)

    @Query("UPDATE calibration_history SET physiologicalAlpha = :alpha," +
            " physiologicalBeta = :beta, " +
            "physiologicalAlphaGravity = :alphaGravity, " +
            "physiologicalBetaGravity = :betaGravity, " +
            "physiologicalConcentration = :concentration WHERE user_id = :userId AND id = (SELECT MAX(id) FROM calibration_history)")
    suspend fun insertPhysiologicalCalibration(userId: String, alpha: Float, beta: Float, alphaGravity: Float, betaGravity: Float, concentration: Float)

    @Query("SELECT relaxation FROM productivity_indexes WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun getRelaxationValuesBySession(sessionId: Long): List<String>

    @Query("SELECT stress FROM productivity_indexes WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun getStressValuesBySession(sessionId: Long): List<String>


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

    @Query("DELETE FROM nfb_metrics")
    suspend fun clearNFBMetrics()

    @Query("DELETE FROM nfb_metrics WHERE sessionId = :sessionId")
    suspend fun clearNFBMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM nfb_metrics_compressed")
    suspend fun clearNFBMetricsCompressed()

    @Query("DELETE FROM nfb_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearNFBMetricsCompressedBySessionId(sessionId: Long)

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

    @Query("DELETE FROM mems_metrics")
    suspend fun clearMEMSMetrics()

    @Query("DELETE FROM mems_metrics WHERE sessionId = :sessionId")
    suspend fun clearMEMSMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM mems_metrics_compressed")
    suspend fun clearMEMSMetricsCompressed()

    @Query("DELETE FROM mems_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearMEMSMetricsCompressedBySessionId(sessionId: Long)

    @Query("DELETE FROM productivity_baselines")
    suspend fun clearProductivityBaseline()

    @Query("DELETE FROM productivity_baselines WHERE sessionId = :sessionId")
    suspend fun clearProductivityBaselinesBySessionId(sessionId: Long)

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

    @Query("DELETE FROM emotional_metrics")
    suspend fun clearEmotionalMetrics()

    @Query("DELETE FROM emotional_metrics WHERE sessionId = :sessionId")
    suspend fun clearEmotionalMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM emotional_metrics_compressed")
    suspend fun clearEmotionalMetricsCompressed()

    @Query("DELETE FROM emotional_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearEmotionalMetricsCompressedBySessionId(sessionId: Long)

    @Query("DELETE FROM cardio_metrics")
    suspend fun clearCardioMetrics()

    @Query("DELETE FROM cardio_metrics WHERE sessionId = :sessionId")
    suspend fun clearCardioMetricsBySessionId(sessionId: Long)

    @Query("DELETE FROM cardio_metrics_compressed")
    suspend fun clearCardioMetricsCompressed()

    @Query("DELETE FROM cardio_metrics_compressed WHERE sessionId = :sessionId")
    suspend fun clearCardioMetricsCompressedBySessionId(sessionId: Long)




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

    @Query("SELECT * FROM productivity_baselines WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityBaseline(): List<ProductivityBaselinesEntity>

    @Query("SELECT * FROM physiological_baselines WHERE isMarked = 0")
    suspend fun getUnmarkedPhysiologicalBaseline(): List<PhysiologicalBaselinesEntity>

    @Query("SELECT * FROM productivity_metrics_compressed WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityMetricsCompressed(): List<ProductivityMetricCompressedEntity>

    @Query("SELECT * FROM productivity_indexes WHERE isMarked = 0")
    suspend fun getUnmarkedProductivityIndexes(): List<ProductivityIndexesEntity>


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

    @Query("UPDATE nfb_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markNFBMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE nfb_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markNFBMetricsCompressedAsSynced(rowIds: List<Long>)


    @Query("UPDATE physiological_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markPhysiologicalMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE physiological_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markPhysiologicalMetricsCompressedAsSynced(rowIds: List<Long>)

    @Query("UPDATE mems_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markMEMSMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE mems_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markMEMSMetricsCompressedAsSynced(rowIds: List<Long>)

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

    @Query("UPDATE productivity_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markProductivityMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE productivity_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markProductivityMetricsCompressedAsSynced(rowIds: List<Long>)

    @Query("UPDATE productivity_indexes SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markProductivityIndexAsSynced(rowIds: List<Long>)

    @Query("UPDATE productivity_baselines SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markProductivityBaselineAsSynced(rowIds: List<Long>)

    @Query("UPDATE physiological_baselines SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markPhysiologicalBaselineAsSynced(rowIds: List<Long>)

    @Query("UPDATE emotional_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEmotionalMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE emotional_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markEmotionalMetricsCompressedAsSynced(rowIds: List<Long>)

    @Query("UPDATE cardio_metrics SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markCardioMetricsAsSynced(rowIds: List<Long>)

    @Query("UPDATE cardio_metrics_compressed SET isMarked = 1 WHERE rowId IN (:rowIds)")
    suspend fun markCardioMetricsCompressedAsSynced(rowIds: List<Long>)

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

    @Query("SELECT * FROM productivity_indexes WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedProductivityIndexesBatch(limit: Int): List<ProductivityIndexesEntity>

    @Query("SELECT * FROM productivity_baselines WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedProductivityBaselineBatch(limit: Int): List<ProductivityBaselinesEntity>

    @Query("SELECT * FROM physiological_baselines WHERE isMarked = 0 LIMIT :limit")
    suspend fun getUnmarkedPhysiologicalBaselineBatch(limit: Int): List<PhysiologicalBaselinesEntity>

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

    @Query("SELECT COUNT(*) FROM productivity_indexes")
    suspend fun getAllProductivityIndexesCount(): Int

    @Query("SELECT COUNT(*) FROM productivity_baselines")
    suspend fun getAllProductivityBaselineCount(): Int

    @Query("SELECT COUNT(*) FROM physiological_baselines")
    suspend fun getAllPhysiologicalBaselineCount(): Int

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

    @Query("SELECT MAX(timestamp) FROM productivity_indexes")
    suspend fun getLastProductivityIndexTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM productivity_baselines")
    suspend fun getLastProductivityBaselineTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM physiological_baselines")
    suspend fun getLastPhysiologicalBaselineTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM emotional_metrics")
    suspend fun getLastEmotionalMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM emotional_metrics_compressed")
    suspend fun getLastEmotionalMetricTimestampCompressed(): Long?

    @Query("SELECT MAX(timestamp) FROM cardio_metrics")
    suspend fun getLastCardioMetricTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM cardio_metrics_compressed")
    suspend fun getLastCardioMetricTimestampCompressed(): Long?


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

    @Transaction
    suspend fun safeMarkPhysiologicalBaselineAsSynced(rowIds: List<Long>) {
        val BATCH_SIZE = 500
        rowIds.chunked(BATCH_SIZE).forEach { batch ->
            markPhysiologicalBaselineAsSynced(batch)
        }
    }


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

   @Query("SELECT * FROM productivity_metrics WHERE sessionId = :sessionId AND timestamp >= :minuteStart AND timestamp <= :minuteEnd ORDER BY timestamp ASC")
   suspend fun getProductivityMetricsForMinute(sessionId: Long, minuteStart: Long, minuteEnd: Long) : List<ProductivityMetricEntity>

    @Query("SELECT * FROM productivity_baselines WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getProductivityBaselines(sessionId: Long) : ProductivityBaselinesEntity

    @Query("SELECT * FROM productivity_indexes WHERE id = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastProductivityIndexForUser(userId: String) : ProductivityIndexesEntity

    @Query("SELECT * FROM productivity_indexes WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getProductivityIndexes(sessionId: Long) : ProductivityIndexesEntity

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


    @Query("SELECT timestamp FROM productivity_metrics  WHERE sessionId = :sessionId  ORDER BY timestamp ASC LIMIT 1")
    suspend fun getProductivityStartTimestamp(sessionId: Long) : Long

    @Query("SELECT timestamp FROM emotional_metrics  WHERE sessionId = :sessionId  ORDER BY timestamp ASC LIMIT 1")
    suspend fun getEmotionalStartTimestamp(sessionId: Long) : Long

    @Query("SELECT timestamp FROM physiological_metrics  WHERE sessionId = :sessionId  ORDER BY timestamp ASC LIMIT 1")
    suspend fun getPhysiologicalStartTimestamp(sessionId: Long) : Long

    @Query("SELECT * FROM productivity_metrics_compressed WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getProductivityCompressedMetrics(sessionId: Long) : List<ProductivityMetricCompressedEntity>

    @Query("SELECT * FROM physiological_metrics_compressed WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPhysiologicalCompressedMetrics(sessionId: Long) : List<PhysiologicalMetricCompressedEntity>

    @Query("SELECT * FROM emotional_metrics_compressed WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getEmotionalCompressedMetrics(sessionId: Long) : List<EmotionalMetricCompressedEntity>


}