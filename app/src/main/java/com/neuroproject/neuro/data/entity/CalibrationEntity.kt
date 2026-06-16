package com.neuroproject.neuro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.neuroproject.neuro.domain.model.CalibrationSample

@Entity(
    tableName = "Calibration_History",
    indices = [Index(value = ["user_id"])]
)
data class CalibrationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user_id: String?,
    val individualFrequency: Float,
    val individualPeakFrequency: Float,
    val individualPeakFrequencyPower: Float,
    val individualPeakFrequencySuppression: Float,
    val individualBandwidth: Float,
    val individualNormalizedPower: Float,
    val lowerFrequency: Float,
    val upperFrequency: Float,
    val productivityGravity: Float? = null,
    val productivityProductivity: Float? = null,
    val productivityFatigue: Float? = null,
    val productivityReverseFatigue: Float? = null,
    val productivityRelaxation: Float? = null,
    val productivityConcentration: Float? = null,
    val physiologicalAlpha: Float? = null,
    val physiologicalBeta: Float? = null,
    val physiologicalAlphaGravity: Float? = null,
    val physiologicalBetaGravity: Float? = null,
    val physiologicalConcentration: Float? = null
) {
    companion object {
        fun fromDomain(userId: String, data: CalibrationSample): CalibrationHistoryEntity {
            return CalibrationHistoryEntity(
                user_id = userId,
                individualFrequency = data.individualFrequency,
                individualPeakFrequency = data.individualPeakFrequency,
                individualPeakFrequencyPower = data.individualPeakFrequencyPower,
                individualPeakFrequencySuppression = data.individualPeakFrequencySuppression,
                individualBandwidth = data.individualBandwidth,
                individualNormalizedPower = data.individualNormalizedPower,
                lowerFrequency = data.lowerFrequency,
                upperFrequency = data.upperFrequency,
                productivityGravity = data.productivityGravity,
                productivityProductivity = data.productivityProductivity,
                productivityFatigue = data.productivityFatigue,
                productivityReverseFatigue = data.productivityReverseFatigue,
                productivityRelaxation = data.productivityRelaxation,
                productivityConcentration = data.productivityConcentration,
                physiologicalAlpha = data.physiologicalAlpha,
                physiologicalBeta = data.physiologicalBeta,
                physiologicalAlphaGravity = data.physiologicalAlphaGravity,
                physiologicalBetaGravity = data.physiologicalBetaGravity,
                physiologicalConcentration = data.physiologicalConcentration
            )
        }
    }
}
