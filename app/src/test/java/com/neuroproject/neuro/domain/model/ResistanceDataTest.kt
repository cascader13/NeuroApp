package com.neuroproject.neuro.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResistanceDataTest {

    @Test
    fun `isChannelOk returns true when value is 1000 or less`() {
        val data = ResistanceData(1000.0, 500.0, 0.0, 999.9)
        assertThat(data.isChannelOk(1000.0)).isTrue()
        assertThat(data.isChannelOk(500.0)).isTrue()
        assertThat(data.isChannelOk(0.0)).isTrue()
    }

    @Test
    fun `isChannelOk returns false when value is greater than 1000`() {
        val data = ResistanceData(1001.0, 5000.0, 2000.0, 1500.0)
        assertThat(data.isChannelOk(1001.0)).isFalse()
        assertThat(data.isChannelOk(5000.0)).isFalse()
    }

    @Test
    fun `isAllOk returns true when all channels are OK`() {
        val data = ResistanceData(500.0, 600.0, 700.0, 800.0)
        assertThat(data.isAllOk()).isTrue()
    }

    @Test
    fun `isAllOk returns false when any channel exceeds 1000`() {
        val data = ResistanceData(500.0, 1500.0, 700.0, 800.0)
        assertThat(data.isAllOk()).isFalse()
    }

    @Test
    fun `isAllOk returns true when all channels are exactly 1000`() {
        val data = ResistanceData(1000.0, 1000.0, 1000.0, 1000.0)
        assertThat(data.isAllOk()).isTrue()
    }
}
