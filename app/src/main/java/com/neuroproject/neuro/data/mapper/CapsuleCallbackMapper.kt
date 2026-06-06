package com.neuroproject.neuro.data.mapper
import com.neuroproject.neuro.domain.model.*
import com.neuroproject.neuro.domain.model.DeviceInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Преобразует сырые JNI callback данные в Domain модели.
 */
@Singleton
class CapsuleCallbackMapper @Inject constructor() {

    fun toDeviceInfo(name: String, address: String): DeviceInfo {
        return DeviceInfo(
            name = name,
            address = address
        )
    }

    fun toNFBSample(
        time: Long, alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float,
        sessionId: String
    ): NFBSample {
        return NFBSample(
            timestamp = time,
            sessionId = sessionId,
            alpha = alpha,
            beta = beta,
            theta = theta,
            delta = delta,
            smr = smr
        )
    }

    fun toCardioSample(
        time: Long, heartRate: Float, hasArtifacts: Boolean, kaplanIndex: Float,
        metricsAvailable: Boolean, motionArtifacts: Boolean, skinContact: Boolean, stress: Float,
        sessionId: String
    ): CardioSample {
        return CardioSample(
            timestamp = time,
            sessionId = sessionId,
            heartRate = heartRate,
            hasArtifacts = hasArtifacts,
            kaplanIndex = kaplanIndex,
            metricsAvailable = metricsAvailable,
            motionArtifacts = motionArtifacts,
            skinContact = skinContact,
            stress = stress
        )
    }

    fun toPhysiologicalSample(
        time: Long, relax: Float, fatigue: Float, none: Float,
        concentration: Float, involvement: Float, stress: Float,
        nfbArtifacts: Boolean, cardioArtifacts: Boolean,
        sessionId: String
    ): PhysiologicalSample {
        return PhysiologicalSample(
            timestamp = time,
            sessionId = sessionId,
            relaxation = relax,
            fatigue = fatigue,
            concentration = concentration,
            involvement = involvement,
            stress = stress,
            nfbArtifacts = nfbArtifacts,
            cardioArtifacts = cardioArtifacts
        )
    }

    fun toMEMSSample(
        time: Long, accX: Float, accY: Float, accZ: Float,
        gyrX: Float, gyrY: Float, gyrZ: Float,
        sessionId: String
    ): MEMSSample {
        return MEMSSample(
            timestamp = time,
            sessionId = sessionId,
            accelerometerX = accX,
            accelerometerY = accY,
            accelerometerZ = accZ,
            gyroscopeX = gyrX,
            gyroscopeY = gyrY,
            gyroscopeZ = gyrZ
        )
    }

    fun toProductivitySample(
        time: Long, timestampProd: Double, gravity: Float,
        productivity: Float, fatigue: Float, reverseFatigue: Float,
        relaxation: Float, concentration: Float,
        sessionId: String
    ): ProductivitySample {
        return ProductivitySample(
            timestamp = time,
            sessionId = sessionId,
            timestampProd = timestampProd,
            gravity = gravity,
            productivity = productivity,
            fatigue = fatigue,
            reverseFatigue = reverseFatigue,
            relaxation = relaxation,
            concentration = concentration
        )
    }

    fun toEmotionalSample(
        time: Long, attention: Float, relaxation: Float,
        cognitiveLoad: Float, cognitiveControl: Float, selfControl: Float,
        sessionId: String
    ): EmotionalSample {
        return EmotionalSample(
            timestamp = time,
            sessionId = sessionId,
            attention = attention,
            relaxation = relaxation,
            cognitiveLoad = cognitiveLoad,
            cognitiveControl = cognitiveControl,
            selfControl = selfControl
        )
    }

    fun toEegRawSample(
        timeStampMilli: Long, channel1: Float, channel2: Float,
        sessionId: String
    ): EEGRawSample {
        return EEGRawSample(
            timestamp = timeStampMilli,
            sessionId = sessionId,
            channel1 = channel1,
            channel2 = channel2
        )
    }

    fun toEegProcessedSample(
        timeStampMilli: Long, channel1: Float, channel2: Float,
        sessionId: String
    ): EEGProcessedSample {
        return EEGProcessedSample(
            timestamp = timeStampMilli,
            sessionId = sessionId,
            channel1 = channel1,
            channel2 = channel2
        )
    }

    fun toEegArtifactSample(
        timeStampMilli: Long, artifacts1: Boolean, artifacts2: Boolean,
        quality1: Float, quality2: Float,
        sessionId: String
    ): EEGArtifactSample {
        return EEGArtifactSample(
            timestamp = timeStampMilli,
            sessionId = sessionId,
            artifactChannel1 = artifacts1,
            artifactChannel2 = artifacts2,
            qualityChannel1 = quality1,
            qualityChannel2 = quality2
        )
    }

    fun toConnectionState(state: Int): DeviceConnectionState {
        return DeviceConnectionState.entries.getOrElse(state) { DeviceConnectionState.disconnected }
    }

    fun toCalibrationStage(stageNum: Int): CalibrationStage {
        return CalibrationStage.fromInt(stageNum)
    }
}