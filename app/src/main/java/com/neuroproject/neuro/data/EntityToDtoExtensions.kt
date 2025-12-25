package com.neuroproject.neuro.data

import com.neuroproject.neuro.data.remote.*
import java.sql.Timestamp



fun NFBMetricEntity.toServerDto(): NfbMetricDto {
    return NfbMetricDto(
        individualNumber = this.id,
        timestamp = this.timestamp,
        session = this.session.toInt(), // Преобразуем Timestamp в Int
        alpha = this.alpha.toDouble(),
        beta = this.beta.toDouble(),
        theta = this.theta.toDouble(),
        delta = this.delta.toDouble(),
        smr = this.smr.toDouble()
    )
}

fun PhysiologicalMetricEntity.toServerDto(): PhysiologicalMetricDto {
    return PhysiologicalMetricDto(
        individualNumber = this.id,
        timestamp = this.timestamp,
        session = this.session.toInt(),
        relax = this.relax.toDouble(),
        fatigue = this.fatigue.toDouble(),
        none = this.none.toDouble(),
        concentration = this.concentration.toDouble(),
        involvement = this.involvement.toDouble(),
        stress = this.stress.toDouble(),
        nfbArtifacts = if (this.nfbArtifacts) 1 else 0,
        cardioArtifacts = if (this.cardioArtifacts) 1 else 0
    )
}

fun EEGRawMetricEntity.toServerDto(): EEGRawMetricDto{
    return EEGRawMetricDto(
        individualNumber = this.id,
        timestamp = this.timestamp,
        session = this.session.toInt(),
        channel1 = this.channel1,
        channel2 = this.channel2
    )
}

fun EEGProceedMetricEntity.toServerDto(): EEGProceedMetricDto{
    return EEGProceedMetricDto(
        individualNumber = this.id,
        timestamp = this.timestamp,
        session = this.session.toInt(),
        channel1 = this.channel1,
        channel2 = this.channel2
    )
}

fun EEGArtifactsMetricEntity.toServerDto(): EEGArtifactMetricDto{
    return EEGArtifactMetricDto(
        individualNumber = this.id,
        timestamp = this.timestamp,
        session = this.session.toInt(),
        artifactsChannel1 = this.artifactsChannel1,
        artifactsChannel2 = this.artifactsChannel2,
        qualityChannel1 = this.qualityChannel1,
        qualityChannel2 = this.qualityChannel2
    )
}

fun MEMSMetricEntity.toServerDto(): MemsMetricDto {
    return MemsMetricDto(
        individualNumber = this.id,
        timestamp = this.timestamp,
        session = this.session.toInt(),
        accelerometerX = this.accelerometerX.toDouble(),
        accelerometerY = this.accelerometerY.toDouble(),
        accelerometerZ = this.accelerometerZ.toDouble(),
        gyroscopeX = this.gyroscopeX.toDouble(),
        gyroscopeY = this.gyroscopeY.toDouble(),
        gyroscopeZ = this.gyroscopeZ.toDouble()
    )
}

fun ProductivityMetricEntity.toServerDto(): ProductivityMetricDto {
    return ProductivityMetricDto(
        individualNumber = this.id,
        timestamp = this.timestamp,
        session = this.session.toInt(),
        gravity = this.gravity.toDouble(),
        productivity = this.productivity.toDouble(),
        fatigue = this.fatigue.toDouble(),
        reverseFatigue = this.reverseFatigue.toDouble(),
        relaxation = this.relaxation.toDouble(),
        concentration = this.concentration.toDouble()
    )
}

fun EmotionalMetricEntity.toServerDto(): EmotionalMetricDto {
    return EmotionalMetricDto(
        individualNumber = this.id,
        timestamp = this.timestamp,
        session = this.session.toInt(),
        attention = this.attention.toDouble(),
        relaxation = this.relaxation.toDouble(),
        cognitiveLoad = this.cognitiveLoad.toDouble(),
        cognitiveControl = this.cognitiveControl.toDouble(),
        selfControl = this.selfControl.toDouble()
    )
}

fun CardioMetricEntity.toServerDto(): CardioMetricDto {
    return CardioMetricDto(
        individualNumber = this.id,
        timestamp = this.timestamp,
        session = this.session.toInt(),
        heartRate = this.heartRate.toDouble(),
        hasArtifacts = if (this.hasArtifacts) 1 else 0,
        kaplanIndex = this.kaplanIndex.toDouble(),
        metricsAvailable = if (this.metricsAvailable) 1 else 0,
        motionArtifacts = if (this.motionArtifacts) 1 else 0,
        skinContact = if (this.skinContact) 1 else 0,
        stressIndex = this.stressIndex.toDouble()
    )
}

// Extension для преобразования Timestamp в Int
fun Timestamp.toInt(): Int {
    return (this.time / 1000).toInt() // Или другая логика преобразования
}