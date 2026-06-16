package com.neuroproject.neuro.data.repository

import com.neuroproject.neuro.data.MetricsDao
import com.neuroproject.neuro.data.FatigueDao
import com.neuroproject.neuro.data.entity.FatigueResultEntity
import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.repository.ObjectiveMetricsRepository
import com.neuroproject.neuro.utils.Normalization
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObjectiveMetricsRepositoryImpl @Inject constructor(
    private val metricsDao: MetricsDao,
    private val fatigueDao: FatigueDao
) : ObjectiveMetricsRepository {

    override suspend fun getMinuteMetrics(sessionId: Long, minuteIndex: Int): MinuteFatigueData? {
        val productivityBatch = metricsDao.getProductivityCompressedMetrics(sessionId)
        val emotionalBatch = metricsDao.getEmotionalCompressedMetrics(sessionId)
        val physiologicalBatch = metricsDao.getPhysiologicalCompressedMetrics(sessionId)
        val productivityBaseline = metricsDao.getProductivityIndexes(sessionId)

        if (minuteIndex > productivityBatch.size ||
            minuteIndex > emotionalBatch.size ||
            minuteIndex > physiologicalBatch.size ||
            productivityBaseline == null) {
            return null
        }

        val productivity = productivityBatch[minuteIndex - 1]
        val emotional = emotionalBatch[minuteIndex - 1]
        val physiological = physiologicalBatch[minuteIndex - 1]

        return MinuteFatigueData(
            minuteIndex = minuteIndex,
            cognitive = CognitiveMetrics(
                fatigue = Normalization.normalizeProdFatique(productivity.fatigue, productivityBaseline.fatigueBaseline),
                concentration = Normalization.normalizeConcentration(productivity.concentration, productivityBaseline.concentrationBaseline),
                productivity = Normalization.normalizeProductivity(productivity.productivity, productivityBaseline.productivityBaseline),
                cognitiveLoad = Normalization.normalizeCognitiveLoad(emotional.cognitiveLoad)
            ),
            physiological = PhysiologicalMetrics(
                fatigue = Normalization.normalizePhysFatique(physiological.fatigue),
                stress = Normalization.normalizeStress(physiological.stress),
                relax = Normalization.normalizeRelax(physiological.relax),
                involvement = Normalization.normalizeInvolvement(physiological.involvement)
            ),
            psychological = PsychologicalMetrics(
                cognitiveLoad = Normalization.normalizeCognitiveLoad(emotional.cognitiveLoad),
                relaxation = Normalization.normalizeRelaxation(emotional.relaxation),
                selfControl = Normalization.normalizeSelfControl(emotional.selfControl),
                cognitiveControl = Normalization.normalizeCognitiveLoad(emotional.cognitiveControl)
            )
        )
    }

    override suspend fun getAllMinuteMetrics(sessionId: Long): List<MinuteFatigueData> {
        val productivityBatch = metricsDao.getProductivityCompressedMetrics(sessionId)
        val emotionalBatch = metricsDao.getEmotionalCompressedMetrics(sessionId)
        val physiologicalBatch = metricsDao.getPhysiologicalCompressedMetrics(sessionId)
        val productivityBaseline = metricsDao.getProductivityIndexes(sessionId)

        if (productivityBaseline == null) return emptyList()

        val maxSize = minOf(productivityBatch.size, emotionalBatch.size, physiologicalBatch.size)
        val result = mutableListOf<MinuteFatigueData>()

        for (i in 0 until maxSize) {
            val productivity = productivityBatch[i]
            val emotional = emotionalBatch[i]
            val physiological = physiologicalBatch[i]

            result.add(
                MinuteFatigueData(
                    minuteIndex = i + 1,
                    cognitive = CognitiveMetrics(
                        fatigue = Normalization.normalizeProdFatique(productivity.fatigue, productivityBaseline.fatigueBaseline),
                        concentration = Normalization.normalizeConcentration(productivity.concentration, productivityBaseline.concentrationBaseline),
                        productivity = Normalization.normalizeProductivity(productivity.productivity, productivityBaseline.productivityBaseline),
                        cognitiveLoad = Normalization.normalizeCognitiveLoad(emotional.cognitiveLoad)
                    ),
                    physiological = PhysiologicalMetrics(
                        fatigue = Normalization.normalizePhysFatique(physiological.fatigue),
                        stress = Normalization.normalizeStress(physiological.stress),
                        relax = Normalization.normalizeRelax(physiological.relax),
                        involvement = Normalization.normalizeInvolvement(physiological.involvement)
                    ),
                    psychological = PsychologicalMetrics(
                        cognitiveLoad = Normalization.normalizeCognitiveLoad(emotional.cognitiveLoad),
                        relaxation = Normalization.normalizeRelaxation(emotional.relaxation),
                        selfControl = Normalization.normalizeSelfControl(emotional.selfControl),
                        cognitiveControl = Normalization.normalizeCognitiveLoad(emotional.cognitiveControl)
                    )
                )
            )
        }

        return result
    }

    override suspend fun saveMinuteFatigueResult(result: FatigueResult) {
        fatigueDao.insertFatiqueResult(
            FatigueResultEntity(
                sessionId = result.sessionId,
                minuteIndex = result.minuteIndex,
                cognitiveResult = result.cognitive,
                physiologicalResult = result.physiological,
                psychologicalResult = result.psychological
            )
        )
    }

    override suspend fun getSessionFatigueResults(sessionId: Long): List<FatigueResult> {
        return fatigueDao.getFatigueResultsForSessionSync(sessionId).map { entity ->
            FatigueResult(
                minuteIndex = entity.minuteIndex,
                cognitive = entity.cognitiveResult,
                physiological = entity.physiologicalResult,
                psychological = entity.psychologicalResult,
                sessionId = entity.sessionId
            )
        }
    }

    override suspend fun getAvailableMinutesCount(sessionId: Long): Int {
        val productivityBatch = metricsDao.getProductivityCompressedMetrics(sessionId)
        val emotionalBatch = metricsDao.getEmotionalCompressedMetrics(sessionId)
        val physiologicalBatch = metricsDao.getPhysiologicalCompressedMetrics(sessionId)

        return minOf(productivityBatch.size, emotionalBatch.size, physiologicalBatch.size)
    }
}