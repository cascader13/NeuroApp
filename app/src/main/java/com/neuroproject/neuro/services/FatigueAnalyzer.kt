package com.neuroproject.neuro.services

import android.util.Log
import com.neuroproject.neuro.data.repository.MetricsAggregationRepository
import com.neuroproject.neuro.models.FatigueMinuteData
import com.neuroproject.neuro.models.FatigueResult
import com.neuroproject.neuro.models.SessionFatigueResult
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FatigueAnalyzer @Inject constructor(
    private val metricsAggregationRepository: MetricsAggregationRepository
) {

    companion object {
        // Веса для когнитивного модуля
        private const val COG_FATIGUE_WEIGHT = 0.30f
        private const val COG_CONCENTRATION_WEIGHT = 0.25f
        private const val COG_PRODUCTIVITY_WEIGHT = 0.20f
        private const val COG_LOAD_WEIGHT = 0.25f

        // Веса для физиологического модуля
        private const val PHYS_FATIGUE_WEIGHT = 0.35f
        private const val PHYS_STRESS_WEIGHT = 0.25f
        private const val PHYS_RELAX_WEIGHT = 0.20f
        private const val PHYS_INVOLVEMENT_WEIGHT = 0.20f

        // Веса для психологического модуля
        private const val PSY_LOAD_WEIGHT = 0.30f
        private const val PSY_RELAXATION_WEIGHT = 0.25f
        private const val PSY_SELF_CONTROL_WEIGHT = 0.25f
        private const val PSY_COGNITIVE_CONTROL_WEIGHT = 0.20f
    }




    suspend fun calculateAll(
        countMinute: Int,
        sessionId: Long?
    ): SessionFatigueResult?{
        if(sessionId==null){
            return null
        }
        var results: MutableList<FatigueResult> = mutableListOf()

        for(i in 1..countMinute){
            var cognitiveFatigue = metricsAggregationRepository.getCognitiveMetricsForMinute(sessionId, i)
            var physiologicalFatigue = metricsAggregationRepository.getPhysiologicalMetricsForMinute(sessionId, i)
            var psychologicalFatigue = metricsAggregationRepository.getPsychologicalMetricsForMinute(sessionId, i)
            if(cognitiveFatigue != null && physiologicalFatigue != null && psychologicalFatigue != null) {
                var fatigueMinuteData = FatigueMinuteData(
                    i,
                    cognitiveFatigue,
                    physiologicalFatigue,
                    psychologicalFatigue
                )
                var fatigueResult = calculateMinuteFatigue(fatigueMinuteData, sessionId)
                results.add(fatigueResult)
            }else{
                Log.e("RESULT_ERROR", "cannot find new data for minute $i")
            }
        }
        return calculateSessionSummary(results, sessionId)
    }



    suspend fun calculateMinuteFatigue(
        minuteData: FatigueMinuteData,
        sessionId: Long
    ): FatigueResult {
        with(minuteData) {
            val normFatigue =  cognitiveMetrics.fatigue
            val normConcentration = cognitiveMetrics.concentration
            val normProductivity = cognitiveMetrics.productivity
            val normCognitiveLoad = cognitiveMetrics.cognitiveLoad

            val cognitiveFatigue =
                COG_FATIGUE_WEIGHT * normFatigue +
                        COG_CONCENTRATION_WEIGHT * normConcentration +
                        COG_PRODUCTIVITY_WEIGHT * normProductivity +
                        COG_LOAD_WEIGHT * normCognitiveLoad

            val normPhysFatigue = physiologicalMetrics.fatigue
            val normStress = physiologicalMetrics.stress
            val normRelax = physiologicalMetrics.relax
            val normInvolvement = physiologicalMetrics.involvement

            val physiologicalFatigue =
                PHYS_FATIGUE_WEIGHT * normPhysFatigue +
                        PHYS_STRESS_WEIGHT * normStress +
                        PHYS_RELAX_WEIGHT * normRelax +
                        PHYS_INVOLVEMENT_WEIGHT * normInvolvement

            val normPsyLoad = psychologicalMetrics.cognitiveLoad
            val normRelaxation = psychologicalMetrics.relaxation
            val normSelfControl = psychologicalMetrics.selfControl
            val normCognitiveControl = psychologicalMetrics.cognitiveControl

            val psychologicalFatigue =
                PSY_LOAD_WEIGHT * normPsyLoad +
                        PSY_RELAXATION_WEIGHT * normRelaxation +
                        PSY_SELF_CONTROL_WEIGHT * normSelfControl +
                        PSY_COGNITIVE_CONTROL_WEIGHT * normCognitiveControl

            var fatigueResult = FatigueResult(
                minuteIndex = minuteIndex,
                cognitive = cognitiveFatigue * 100,
                physiological = physiologicalFatigue * 100,
                psychological = psychologicalFatigue * 100,
                sessionId = sessionId)

            metricsAggregationRepository.writeResultForMinute(fatigueResult)
            return fatigueResult
        }
    }
    fun calculateSessionSummary(
        results: List<FatigueResult>,
        sessionId: Long
    ): SessionFatigueResult {
        val durationMinutes = results.size

        return SessionFatigueResult(
            sessionId = sessionId,
            results = results,
            averageCognitive = results.map { it.cognitive }.average().toFloat(),
            averagePhysiological = results.map { it.physiological }.average().toFloat(),
            averagePsychological = results.map { it.psychological }.average().toFloat(),
            durationMinutes = durationMinutes
        )
    }
}