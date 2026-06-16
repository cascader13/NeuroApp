package com.neuroproject.neuro.domain.usecase.objective

import com.neuroproject.neuro.domain.model.*
import javax.inject.Inject

/**
 * Use case расчёта объективных показателей утомления.
 *
 * Анализирует данные сенсоров (ЭЭГ, физиология, продуктивность) по минутам
 * и рассчитывает три индекса:
 * - **Когнитивный** — на основе疲劳, концентрации, продуктивности, когнитивной нагрузки
 * - **Физиологический** — на основе疲劳, стресса, расслабления, вовлечённости
 * - **Психологический** — на основе когнитивной нагрузки, расслабления, самоконтроля
 *
 * Веса коэффициентов настраивается через [ObjectiveCalculationConfig].
 *
 * Формула для каждого индекса: `sum(weight_i * value_i) * 100`
 */
class CalculateObjectiveFatigueUseCase @Inject constructor() {

    /**
     * Рассчитывает объективные показатели утомления.
     *
     * @param minuteDataList данные по минутам сессии.
     * @param config конфигурация весов (по умолчанию стандартные веса).
     * @return [ObjectiveFatigueResult] или null, если данных нет.
     */
    operator fun invoke(
        minuteDataList: List<MinuteFatigueData>,
        config: ObjectiveCalculationConfig = ObjectiveCalculationConfig()
    ): ObjectiveFatigueResult? {
        if (minuteDataList.isEmpty()) return null

        val avgCognitive = minuteDataList.map { calculateCognitiveIndex(it.cognitive, config.cognitiveWeights) }.average().toFloat()
        val avgPhysiological = minuteDataList.map { calculatePhysiologicalIndex(it.physiological, config.physiologicalWeights) }.average().toFloat()
        val avgPsychological = minuteDataList.map { calculatePsychologicalIndex(it.psychological, config.psychologicalWeights) }.average().toFloat()

        val cognitiveIndex = (avgCognitive * 100).toInt()
        val physiologicalIndex = (avgPhysiological * 100).toInt()
        val psychologicalIndex = (avgPsychological * 100).toInt()
        val averageIndex = (cognitiveIndex + physiologicalIndex + psychologicalIndex) / 3

        val fatigueLevel = FatigueLevel.fromValue(100 - averageIndex).description
        val stressLevel = getStressLevelFromData(minuteDataList)

        return ObjectiveFatigueResult(
            cognitiveIndex = cognitiveIndex,
            psychologicalIndex = psychologicalIndex,
            physiologicalIndex = physiologicalIndex,
            averageIndex = averageIndex,
            fatigueLevel = fatigueLevel,
            stressLevel = stressLevel
        )
    }

    private fun calculateCognitiveIndex(
        metrics: CognitiveMetrics,
        weights: CognitiveWeights
    ): Float {
        return weights.fatigue * metrics.fatigue +
                weights.concentration * metrics.concentration +
                weights.productivity * metrics.productivity +
                weights.cognitiveLoad * metrics.cognitiveLoad
    }

    private fun calculatePhysiologicalIndex(
        metrics: PhysiologicalMetrics,
        weights: PhysiologicalWeights
    ): Float {
        return weights.fatigue * metrics.fatigue +
                weights.stress * metrics.stress +
                weights.relax * metrics.relax +
                weights.involvement * metrics.involvement
    }

    private fun calculatePsychologicalIndex(
        metrics: PsychologicalMetrics,
        weights: PsychologicalWeights
    ): Float {
        return weights.cognitiveLoad * metrics.cognitiveLoad +
                weights.relaxation * metrics.relaxation +
                weights.selfControl * metrics.selfControl +
                weights.cognitiveControl * metrics.cognitiveControl
    }

    private fun getStressLevelFromData(minuteDataList: List<MinuteFatigueData>): String {
        val avgStress = minuteDataList.map { it.physiological.stress }.average().toFloat()
        val stressIndex = (avgStress * 100).toInt()
        return StressLevel.fromValue(stressIndex).description
    }
}