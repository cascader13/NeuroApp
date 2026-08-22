// data/local/RoomCalibrationRepository.kt
package com.neuroproject.neuro.data.local

import com.neuroproject.neuro.data.MetricsDao
import com.neuroproject.neuro.data.dao.CalibrationDao
import com.neuroproject.neuro.data.entity.CalibrationHistoryEntity
import com.neuroproject.neuro.domain.model.CalibrationSample
import com.neuroproject.neuro.domain.repository.CalibrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/**
 * Реализация репозитория калибровки на основе Room.
 */
class RoomCalibrationRepository @Inject constructor(
    private val metricsDao: MetricsDao,
    private val calibrationDao: CalibrationDao
) : CalibrationRepository {

    override suspend fun getPreviousCalibration(userId: String): CalibrationSample? {
        val entities = metricsDao.getCalibration(userId)
        return entities.firstOrNull()?.toDomain()
    }


    override suspend fun saveCalibration(userId: String, data: CalibrationSample) {
        val entity = CalibrationHistoryEntity.fromDomain(userId, data)
        metricsDao.insertCalibrationData(entity)
    }

    override suspend fun hasPreviousCalibration(userId: String): Boolean {
        return metricsDao.getCalibration(userId).isNotEmpty()
    }

    override suspend fun updateProductivityCalibration(
        userId: String,
        gravity: Float,
        productivity: Float,
        fatigue: Float,
        reverseFatigue: Float,
        relaxation: Float,
        concentration: Float
    ) {
        calibrationDao.insertProductivityCalibration(
            userId, gravity, productivity, fatigue, reverseFatigue, relaxation, concentration
        )
    }

    override suspend fun updatePhysiologicalCalibration(
        userId: String,
        alpha: Float,
        beta: Float,
        alphaGravity: Float,
        betaGravity: Float,
        concentration: Float
    ) {
        calibrationDao.insertPhysiologicalCalibration(
            userId, alpha, beta, alphaGravity, betaGravity, concentration
        )
    }

    override fun observeCalibrationStage(): Flow<Int> {
        error("Use DeviceGateway.observeCalibrationState() instead")
    }
}


fun CalibrationHistoryEntity.toDomain(): CalibrationSample = CalibrationSample(
    individualFrequency = individualFrequency,
    individualPeakFrequency = individualPeakFrequency,
    individualPeakFrequencyPower = individualPeakFrequencyPower,
    individualPeakFrequencySuppression = individualPeakFrequencySuppression,
    individualBandwidth = individualBandwidth,
    individualNormalizedPower = individualNormalizedPower,
    lowerFrequency = lowerFrequency,
    upperFrequency = upperFrequency,
    productivityGravity = productivityGravity,
    productivityProductivity = productivityProductivity,
    productivityFatigue = productivityFatigue,
    productivityReverseFatigue = productivityReverseFatigue,
    productivityRelaxation = productivityRelaxation,
    productivityConcentration = productivityConcentration,
    physiologicalAlpha = physiologicalAlpha,
    physiologicalBeta = physiologicalBeta,
    physiologicalAlphaGravity = physiologicalAlphaGravity,
    physiologicalBetaGravity = physiologicalBetaGravity,
    physiologicalConcentration = physiologicalConcentration
)

fun CalibrationHistoryEntity.fromDomain(userId: String, data: CalibrationSample): CalibrationHistoryEntity {
    return CalibrationHistoryEntity(
        user_id = userId,
        individualFrequency = data.individualFrequency,
        individualPeakFrequency = data.individualPeakFrequency,
        individualPeakFrequencyPower = data.individualPeakFrequencyPower,
        individualPeakFrequencySuppression = data.individualPeakFrequencySuppression,
        individualBandwidth = data.individualBandwidth,
        individualNormalizedPower = data.individualNormalizedPower,
        lowerFrequency = data.lowerFrequency,
        upperFrequency = data.upperFrequency
    )
}