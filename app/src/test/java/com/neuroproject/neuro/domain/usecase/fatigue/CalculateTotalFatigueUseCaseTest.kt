package com.neuroproject.neuro.domain.usecase.fatigue

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.ObjectiveFatigueResult
import com.neuroproject.neuro.domain.model.SubjectiveResult
import org.junit.Before
import org.junit.Test

class CalculateTotalFatigueUseCaseTest {

    private lateinit var useCase: CalculateTotalFatigueUseCase

    @Before
    fun setup() {
        useCase = CalculateTotalFatigueUseCase()
    }

    @Test
    fun `given matching values when invoke then total indices are averages`() {
        val subjective = SubjectiveResult(80, 60, 40, 60)
        val objective = ObjectiveFatigueResult(70, 50, 30, 50, "Средний", "Низкий")

        val result = useCase(subjective, objective)

        assertThat(result.subjective).isEqualTo(subjective)
        assertThat(result.objective).isEqualTo(objective)
        assertThat(result.total.cognitiveIndex).isEqualTo(75)  // (80+70)/2
        assertThat(result.total.psychologicalIndex).isEqualTo(55) // (60+50)/2
        assertThat(result.total.physiologicalIndex).isEqualTo(35) // (40+30)/2
        assertThat(result.total.averageIndex).isEqualTo(55)     // (60+50)/2
    }

    @Test
    fun `given zeros when invoke then total indices are zeros`() {
        val subjective = SubjectiveResult(0, 0, 0, 0)
        val objective = ObjectiveFatigueResult(0, 0, 0, 0, "Низкий", "Низкий")

        val result = useCase(subjective, objective)

        assertThat(result.total.cognitiveIndex).isEqualTo(0)
        assertThat(result.total.psychologicalIndex).isEqualTo(0)
        assertThat(result.total.physiologicalIndex).isEqualTo(0)
        assertThat(result.total.averageIndex).isEqualTo(0)
    }

    @Test
    fun `given max values when invoke then total indices are 100`() {
        val subjective = SubjectiveResult(100, 100, 100, 100)
        val objective = ObjectiveFatigueResult(100, 100, 100, 100, "Высокий", "Высокий")

        val result = useCase(subjective, objective)

        assertThat(result.total.cognitiveIndex).isEqualTo(100)
        assertThat(result.total.psychologicalIndex).isEqualTo(100)
        assertThat(result.total.physiologicalIndex).isEqualTo(100)
        assertThat(result.total.averageIndex).isEqualTo(100)
    }

    @Test
    fun `given odd sum when invoke then integer division truncates`() {
        val subjective = SubjectiveResult(51, 51, 51, 51)
        val objective = ObjectiveFatigueResult(50, 50, 50, 50, "Средний", "Средний")

        val result = useCase(subjective, objective)

        // (51+50)/2 = 101/2 = 50 (integer division)
        assertThat(result.total.cognitiveIndex).isEqualTo(50)
    }
}
