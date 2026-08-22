package com.neuroproject.neuro.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ObjectiveMetricsTest {

    @Test
    fun `FatigueLevel fromValue returns LOW for low values`() {
        assertThat(FatigueLevel.fromValue(0)).isEqualTo(FatigueLevel.LOW)
        assertThat(FatigueLevel.fromValue(20)).isEqualTo(FatigueLevel.LOW)
    }

    @Test
    fun `FatigueLevel fromValue returns MODERATE for moderate values`() {
        assertThat(FatigueLevel.fromValue(21)).isEqualTo(FatigueLevel.MODERATE)
        assertThat(FatigueLevel.fromValue(40)).isEqualTo(FatigueLevel.MODERATE)
    }

    @Test
    fun `FatigueLevel fromValue returns MEDIUM for medium values`() {
        assertThat(FatigueLevel.fromValue(41)).isEqualTo(FatigueLevel.MEDIUM)
        assertThat(FatigueLevel.fromValue(60)).isEqualTo(FatigueLevel.MEDIUM)
    }

    @Test
    fun `FatigueLevel fromValue returns ELEVATED for elevated values`() {
        assertThat(FatigueLevel.fromValue(61)).isEqualTo(FatigueLevel.ELEVATED)
        assertThat(FatigueLevel.fromValue(80)).isEqualTo(FatigueLevel.ELEVATED)
    }

    @Test
    fun `FatigueLevel fromValue returns HIGH for high values`() {
        assertThat(FatigueLevel.fromValue(81)).isEqualTo(FatigueLevel.HIGH)
        assertThat(FatigueLevel.fromValue(100)).isEqualTo(FatigueLevel.HIGH)
    }

    @Test
    fun `StressLevel fromValue returns correct levels`() {
        assertThat(StressLevel.fromValue(10)).isEqualTo(StressLevel.LOW)
        assertThat(StressLevel.fromValue(30)).isEqualTo(StressLevel.MODERATE)
        assertThat(StressLevel.fromValue(50)).isEqualTo(StressLevel.MEDIUM)
        assertThat(StressLevel.fromValue(70)).isEqualTo(StressLevel.ELEVATED)
        assertThat(StressLevel.fromValue(90)).isEqualTo(StressLevel.HIGH)
    }
}
