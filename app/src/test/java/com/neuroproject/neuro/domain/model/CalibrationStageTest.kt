package com.neuroproject.neuro.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CalibrationStageTest {

    @Test
    fun `fromInt returns correct stage for known values`() {
        assertThat(CalibrationStage.fromInt(-2)).isEqualTo(CalibrationStage.CALIBRATOR_UNKNOWN_STAGE)
        assertThat(CalibrationStage.fromInt(-1)).isEqualTo(CalibrationStage.CALIBRATOR_READY_STAGE)
        assertThat(CalibrationStage.fromInt(0)).isEqualTo(CalibrationStage.CALIBRATOR_STAGE1)
        assertThat(CalibrationStage.fromInt(1)).isEqualTo(CalibrationStage.CALIBRATOR_STAGE2)
        assertThat(CalibrationStage.fromInt(2)).isEqualTo(CalibrationStage.CALIBRATOR_STAGE3)
        assertThat(CalibrationStage.fromInt(3)).isEqualTo(CalibrationStage.CALIBRATOR_STAGE4)
        assertThat(CalibrationStage.fromInt(4)).isEqualTo(CalibrationStage.PHYSIO_INIT_STAGE)
        assertThat(CalibrationStage.fromInt(5)).isEqualTo(CalibrationStage.PHYSIO_BASELINE_STAGE)
        assertThat(CalibrationStage.fromInt(6)).isEqualTo(CalibrationStage.CALIBRATOR_ERROR_STAGE)
    }

    @Test
    fun `fromInt returns UNKNOWN for unknown values`() {
        assertThat(CalibrationStage.fromInt(99)).isEqualTo(CalibrationStage.CALIBRATOR_UNKNOWN_STAGE)
        assertThat(CalibrationStage.fromInt(-100)).isEqualTo(CalibrationStage.CALIBRATOR_UNKNOWN_STAGE)
    }

    @Test
    fun `each stage has unique value`() {
        val values = CalibrationStage.entries.map { it.value }
        assertThat(values.distinct()).hasSize(values.size)
    }
}
