package com.neuroproject.neuro.presentation.screens.sensorchecking

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.ResistanceData
import org.junit.Test

class SensorCheckingStateTest {

    @Test
    fun `toElectrodeStates maps OK correctly`() {
        val data = ResistanceData(500.0, 600.0, 700.0, 800.0)
        val states = data.toElectrodeStates()
        assertThat(states.o1).isEqualTo(ElectrodeState.OK)
        assertThat(states.o2).isEqualTo(ElectrodeState.OK)
        assertThat(states.t3).isEqualTo(ElectrodeState.OK)
        assertThat(states.t4).isEqualTo(ElectrodeState.OK)
    }

    @Test
    fun `toElectrodeStates maps BAD correctly`() {
        val data = ResistanceData(1500.0, 2000.0, 1001.0, 3000.0)
        val states = data.toElectrodeStates()
        assertThat(states.o1).isEqualTo(ElectrodeState.BAD)
        assertThat(states.o2).isEqualTo(ElectrodeState.BAD)
        assertThat(states.t3).isEqualTo(ElectrodeState.BAD)
        assertThat(states.t4).isEqualTo(ElectrodeState.BAD)
    }

    @Test
    fun `toElectrodeStates maps mixed correctly`() {
        val data = ResistanceData(500.0, 1500.0, 700.0, 2000.0)
        val states = data.toElectrodeStates()
        assertThat(states.o1).isEqualTo(ElectrodeState.OK)
        assertThat(states.o2).isEqualTo(ElectrodeState.BAD)
        assertThat(states.t3).isEqualTo(ElectrodeState.OK)
        assertThat(states.t4).isEqualTo(ElectrodeState.BAD)
    }

    @Test
    fun `ElectrodeStates isAllOk returns true when 3 of 4 are OK`() {
        val states = ElectrodeStates(o1 = ElectrodeState.OK, o2 = ElectrodeState.OK, t3 = ElectrodeState.OK, t4 = ElectrodeState.BAD)
        assertThat(states.isAllOk()).isTrue()
    }

    @Test
    fun `ElectrodeStates isAllOk returns false when 2 or less are OK`() {
        val states = ElectrodeStates(o1 = ElectrodeState.OK, o2 = ElectrodeState.OK, t3 = ElectrodeState.BAD, t4 = ElectrodeState.BAD)
        assertThat(states.isAllOk()).isFalse()
    }
}
