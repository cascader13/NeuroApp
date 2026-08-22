package com.neuroproject.neuro.data.repository

import com.neuroproject.neuro.data.FatigueDao
import com.neuroproject.neuro.data.MetricsDao
import com.neuroproject.neuro.domain.model.CognitiveFatigueMetrics
import com.neuroproject.neuro.domain.model.PhysiologicalFatigueMetrics
import com.neuroproject.neuro.domain.model.PsychologicalFatigueMetrics
import com.neuroproject.neuro.utils.Normalization
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.neuroproject.neuro.data.entity.FatigueResultEntity
import com.neuroproject.neuro.domain.model.FatigueResult
import com.neuroproject.neuro.domain.model.SessionFatigueResult


@Singleton
/**
 * Репозиторий агрегации метрик усталости по минутам сессии.
 */
class MetricsAggregationRepository @Inject constructor(
    private val metricsDao: MetricsDao,
    private val fatigueDao: FatigueDao
){
    suspend fun getCognitiveMetricsForMinute(
        sessionId: Long,
        minute: Int
    ): CognitiveFatigueMetrics? {


        val productivityBatch = metricsDao.getProductivityCompressedMetrics(sessionId)
        var emotionalBatch = metricsDao.getEmotionalCompressedMetrics(sessionId)
        var productivityBaseline = metricsDao.getProductivityIndexes(sessionId)
        Log.d("Aggregation_repository", "size of batch productivity ${productivityBatch.size}, emotional ${emotionalBatch.size}")
        if((minute) > productivityBatch.size || (minute) > emotionalBatch.size || productivityBaseline == null){
            return null
        }
        var fatigue = productivityBatch[minute-1].fatigue
        var concentration = productivityBatch[minute-1].concentration
        var productivity = productivityBatch[minute-1].productivity
        var cognitiveLoad = emotionalBatch[minute-1].cognitiveLoad
        var norm_fatigue = Normalization.normalizeProdFatique(fatigue, productivityBaseline.fatigueBaseline)
        var norm_concentration = Normalization.normalizeConcentration(concentration, productivityBaseline.concentrationBaseline)
        var norm_productivity = Normalization.normalizeProductivity(productivity, productivityBaseline.productivityBaseline)
        var norm_cognitiveLoad = Normalization.normalizeCognitiveLoad(cognitiveLoad)
        return CognitiveFatigueMetrics(norm_fatigue, norm_concentration, norm_productivity, norm_cognitiveLoad)
    }

    suspend fun getPhysiologicalMetricsForMinute(
        sessionId: Long,
        minute: Int
    ): PhysiologicalFatigueMetrics? {
        var physiologicalBatch = metricsDao.getPhysiologicalCompressedMetrics(sessionId)
        Log.d("Aggregation_repository", "size of batch physiological ${physiologicalBatch.size}")
            if((minute) > physiologicalBatch.size){
            if(physiologicalBatch.size == 0) {
                return null
            }
                var fatigue = Normalization.normalizePhysFatique(physiologicalBatch.last().fatigue)
                var stress = Normalization.normalizeStress(physiologicalBatch.last().stress)
                var relax = Normalization.normalizeRelax(physiologicalBatch.last().relax)
                var involment = Normalization.normalizeInvolvement(physiologicalBatch.last() .involvement)
                return PhysiologicalFatigueMetrics(fatigue, stress, relax, involment)
        }
        var fatigue = Normalization.normalizePhysFatique(physiologicalBatch[minute-1].fatigue)
        var stress = Normalization.normalizeStress(physiologicalBatch[minute-1].stress)
        var relax = Normalization.normalizeRelax(physiologicalBatch[minute-1].relax)
        var involment = Normalization.normalizeInvolvement(physiologicalBatch[minute-1].involvement)
        return PhysiologicalFatigueMetrics(fatigue, stress, relax, involment)

    }

    suspend fun getPsychologicalMetricsForMinute(
        sessionId: Long,
        minute: Int
    ): PsychologicalFatigueMetrics? {
        var emotionalBatch = metricsDao.getEmotionalCompressedMetrics(sessionId)
        Log.d("Aggregation_repository", "size of batch emotional ${emotionalBatch.size}")
            if((minute) > emotionalBatch.size){
            return null
        }
        var cognitiveLoad = Normalization.normalizeCognitiveLoad(emotionalBatch[minute-1].cognitiveLoad)
        var relaxation = Normalization.normalizeRelaxation(emotionalBatch[minute-1].relaxation)
        var selfControl = Normalization.normalizeSelfControl(emotionalBatch[minute-1].selfControl)
        var cognitiveControl = Normalization.normalizeCognitiveLoad(emotionalBatch[minute-1].cognitiveControl)

        return PsychologicalFatigueMetrics(cognitiveLoad, relaxation, selfControl, cognitiveControl)
    }

    suspend fun writeResultForMinute(fatigueResult: FatigueResult){
        fatigueDao.insertFatiqueResult(FatigueResultEntity(sessionId = fatigueResult.sessionId, minuteIndex = fatigueResult.minuteIndex, cognitiveResult = fatigueResult.cognitive, physiologicalResult = fatigueResult.physiological, psychologicalResult = fatigueResult.psychological))
    }
}