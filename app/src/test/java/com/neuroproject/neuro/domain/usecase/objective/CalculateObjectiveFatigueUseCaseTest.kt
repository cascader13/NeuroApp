package com.neuroproject.neuro.domain.usecase.objective

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.*
import org.junit.Before
import org.junit.Test

class CalculateObjectiveFatigueUseCaseTest {

    private lateinit var useCase: CalculateObjectiveFatigueUseCase

    @Before
    fun setup() {
        useCase = CalculateObjectiveFatigueUseCase()
    }

    @Test
    fun `given empty list when invoke then returns null`() {
        val result = useCase(emptyList())
        assertThat(result).isNull()
    }

    @Test
    fun `given single minute with all 1_0 when invoke then returns high indices`() {
        val data = listOf(
            MinuteFatigueData(
                minuteIndex = 0,
                cognitive = CognitiveMetrics(1f, 1f, 1f, 1f),
                physiological = PhysiologicalMetrics(1f, 1f, 1f, 1f),
                psychological = PsychologicalMetrics(1f, 1f, 1f, 1f)
            )
        )
        val result = useCase(data)!!
        assertThat(result.cognitiveIndex).isEqualTo(100)
        assertThat(result.physiologicalIndex).isEqualTo(100)
        assertThat(result.psychologicalIndex).isEqualTo(100)
        assertThat(result.averageIndex).isEqualTo(100)
    }

    @Test
    fun `given single minute with all zeros when invoke then returns low indices`() {
        val data = listOf(
            MinuteFatigueData(
                minuteIndex = 0,
                cognitive = CognitiveMetrics(0f, 0f, 0f, 0f),
                physiological = PhysiologicalMetrics(0f, 0f, 0f, 0f),
                psychological = PsychologicalMetrics(0f, 0f, 0f, 0f)
            )
        )
        val result = useCase(data)!!
        assertThat(result.cognitiveIndex).isEqualTo(0)
        assertThat(result.physiologicalIndex).isEqualTo(0)
        assertThat(result.psychologicalIndex).isEqualTo(0)
        assertThat(result.averageIndex).isEqualTo(0)
    }

    @Test
    fun `given multiple minutes when invoke then averages across minutes`() {
        val data = listOf(
            MinuteFatigueData(
                minuteIndex = 0,
                cognitive = CognitiveMetrics(1f, 0f, 0f, 0f),
                physiological = PhysiologicalMetrics(1f, 0f, 0f, 0f),
                psychological = PsychologicalMetrics(1f, 0f, 0f, 0f)
            ),
            MinuteFatigueData(
                minuteIndex = 1,
                cognitive = CognitiveMetrics(0f, 1f, 0f, 0f),
                physiological = PhysiologicalMetrics(0f, 1f, 0f, 0f),
                psychological = PsychologicalMetrics(0f, 1f, 0f, 0f)
            )
        )
        val result = useCase(data)!!
        // cognitive: minute0=0.30, minute1=0.25, avg=0.275 -> 27
        // physiological: minute0=0.35, minute1=0.25, avg=0.30 -> 30
        // psychological: minute0=0.30, minute1=0.25, avg=0.275 -> 27
        assertThat(result.cognitiveIndex).isEqualTo(27)
        assertThat(result.physiologicalIndex).isEqualTo(30)
        assertThat(result.psychologicalIndex).isEqualTo(27)
    }

    @Test
    fun `fatigueLevel is derived from averageIndex`() {
        val data = listOf(
            MinuteFatigueData(
                minuteIndex = 0,
                cognitive = CognitiveMetrics(1f, 1f, 1f, 1f),
                physiological = PhysiologicalMetrics(1f, 1f, 1f, 1f),
                psychological = PsychologicalMetrics(1f, 1f, 1f, 1f)
            )
        )
        val result = useCase(data)!!
        assertThat(result.fatigueLevel).isNotEmpty()
        // averageIndex=100, 100-100=0 -> LOW
        assertThat(result.fatigueLevel).isEqualTo("Низкий уровень утомления")
    }

    @Test
    fun `stressLevel is derived from average stress`() {
        val data = listOf(
            MinuteFatigueData(
                minuteIndex = 0,
                cognitive = CognitiveMetrics(0f, 0f, 0f, 0f),
                physiological = PhysiologicalMetrics(0f, 1f, 0f, 0f),
                psychological = PsychologicalMetrics(0f, 0f, 0f, 0f)
            )
        )
        val result = useCase(data)!!
        // avgStress=1.0, stressIndex=100 -> HIGH
        assertThat(result.stressLevel).isEqualTo("Высокий уровень стресса")
    }

    @Test
    fun `custom weights affect calculation`() {
        val data = listOf(
            MinuteFatigueData(
                minuteIndex = 0,
                cognitive = CognitiveMetrics(1f, 0f, 0f, 0f),
                physiological = PhysiologicalMetrics(1f, 0f, 0f, 0f),
                psychological = PsychologicalMetrics(1f, 0f, 0f, 0f)
            )
        )
        val config = ObjectiveCalculationConfig(
            cognitiveWeights = CognitiveWeights(fatigue = 1f, concentration = 0f, productivity = 0f, cognitiveLoad = 0f),
            physiologicalWeights = PhysiologicalWeights(fatigue = 1f, stress = 0f, relax = 0f, involvement = 0f),
            psychologicalWeights = PsychologicalWeights(cognitiveLoad = 1f, relaxation = 0f, selfControl = 0f, cognitiveControl = 0f)
        )
        val result = useCase(data, config)!!
        assertThat(result.cognitiveIndex).isEqualTo(100)
        assertThat(result.physiologicalIndex).isEqualTo(100)
        assertThat(result.psychologicalIndex).isEqualTo(100)
    }
}
