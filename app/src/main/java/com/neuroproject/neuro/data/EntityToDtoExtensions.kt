package com.neuroproject.neuro.data

import com.neuroproject.neuro.data.remote.CardioMetricCompressedDto
import com.neuroproject.neuro.data.remote.CardioMetricDto
import com.neuroproject.neuro.data.remote.EEGArtifactMetricCompressedDto
import com.neuroproject.neuro.data.remote.EEGArtifactMetricDto
import com.neuroproject.neuro.data.remote.EEGProceedMetricCompressedDto
import com.neuroproject.neuro.data.remote.EEGProceedMetricDto
import com.neuroproject.neuro.data.remote.EEGRawMetricCompressedDto
import com.neuroproject.neuro.data.remote.EEGRawMetricDto
import com.neuroproject.neuro.data.remote.EmotionalMetricCompressedDto
import com.neuroproject.neuro.data.remote.EmotionalMetricDto
import com.neuroproject.neuro.data.remote.MemsMetricCompressedDto
import com.neuroproject.neuro.data.remote.MemsMetricDto
import com.neuroproject.neuro.data.remote.NfbMetricCompressedDto
import com.neuroproject.neuro.data.remote.NfbMetricDto
import com.neuroproject.neuro.data.remote.PhysiologicalBaselineDto
import com.neuroproject.neuro.data.remote.PhysiologicalMetricCompressedDto
import com.neuroproject.neuro.data.remote.PhysiologicalMetricDto
import com.neuroproject.neuro.data.remote.ProductivityBaselineDto
import com.neuroproject.neuro.data.remote.ProductivityIndexDto
import com.neuroproject.neuro.data.remote.ProductivityMetricCompressedDto
import com.neuroproject.neuro.data.remote.ProductivityMetricDto


// Для преобразования sessionId к инту
fun Long.toSecondsInt(): Int = (this / 1000).toInt()

// Существующие функции расширения для uncompressed entities
fun NFBMetricEntity.toServerDto(): NfbMetricDto {
    return NfbMetricDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
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
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
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

fun EEGRawMetricEntity.toServerDto(): EEGRawMetricDto {
    return EEGRawMetricDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        channel1 = this.channel1,
        channel2 = this.channel2
    )
}

fun EEGProceedMetricEntity.toServerDto(): EEGProceedMetricDto {
    return EEGProceedMetricDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        channel1 = this.channel1,
        channel2 = this.channel2
    )
}

fun EEGArtifactsMetricEntity.toServerDto(): EEGArtifactMetricDto {
    return EEGArtifactMetricDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        artifactsChannel1 = this.artifactsChannel1,
        artifactsChannel2 = this.artifactsChannel2,
        qualityChannel1 = this.qualityChannel1,
        qualityChannel2 = this.qualityChannel2
    )
}

fun MEMSMetricEntity.toServerDto(): MemsMetricDto {
    return MemsMetricDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
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
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
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
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
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
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        heartRate = this.heartRate.toDouble(),
        hasArtifacts = if (this.hasArtifacts) 1 else 0,
        kaplanIndex = this.kaplanIndex.toDouble(),
        metricsAvailable = if (this.metricsAvailable) 1 else 0,
        motionArtifacts = if (this.motionArtifacts) 1 else 0,
        skinContact = if (this.skinContact) 1 else 0,
        stressIndex = this.stressIndex.toDouble()
    )
}

// Функции расширения для compressed entities
fun NFBMetricCompressedEntity.toServerDto(): NfbMetricCompressedDto {
    return NfbMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        alpha = this.alpha.toDouble(),
        beta = this.beta.toDouble(),
        theta = this.theta.toDouble(),
        delta = this.delta.toDouble(),
        smr = this.smr.toDouble()
    )
}

fun PhysiologicalMetricCompressedEntity.toServerDto(): PhysiologicalMetricCompressedDto {
    return PhysiologicalMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
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

fun EEGRawMetricCompressedEntity.toServerDto(): EEGRawMetricCompressedDto {
    return EEGRawMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        channel1 = this.channel1,
        channel2 = this.channel2
    )
}

fun EEGProceedMetricCompressedEntity.toServerDto(): EEGProceedMetricCompressedDto {
    return EEGProceedMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        channel1 = this.channel1,
        channel2 = this.channel2
    )
}

fun EEGArtifactsMetricCompressedEntity.toServerDto(): EEGArtifactMetricCompressedDto {
    return EEGArtifactMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        artifactsChannel1 = this.artifactsChannel1,
        artifactsChannel2 = this.artifactsChannel2,
        qualityChannel1 = this.qualityChannel1,
        qualityChannel2 = this.qualityChannel2
    )
}

fun MEMSMetricCompressedEntity.toServerDto(): MemsMetricCompressedDto {
    return MemsMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        accelerometerX = this.accelerometerX.toDouble(),
        accelerometerY = this.accelerometerY.toDouble(),
        accelerometerZ = this.accelerometerZ.toDouble(),
        gyroscopeX = this.gyroscopeX.toDouble(),
        gyroscopeY = this.gyroscopeY.toDouble(),
        gyroscopeZ = this.gyroscopeZ.toDouble()
    )
}

fun ProductivityMetricCompressedEntity.toServerDto(): ProductivityMetricCompressedDto {
    return ProductivityMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        gravity = this.gravity.toDouble(),
        productivity = this.productivity.toDouble(),
        fatigue = this.fatigue.toDouble(),
        reverseFatigue = this.reverseFatigue.toDouble(),
        relaxation = this.relaxation.toDouble(),
        concentration = this.concentration.toDouble()
    )
}

fun EmotionalMetricCompressedEntity.toServerDto(): EmotionalMetricCompressedDto {
    return EmotionalMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        attention = this.attention.toDouble(),
        relaxation = this.relaxation.toDouble(),
        cognitiveLoad = this.cognitiveLoad.toDouble(),
        cognitiveControl = this.cognitiveControl.toDouble(),
        selfControl = this.selfControl.toDouble()
    )
}

fun CardioMetricCompressedEntity.toServerDto(): CardioMetricCompressedDto {
    return CardioMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        heartRate = this.heartRate.toDouble(),
        hasArtifacts = if (this.hasArtifacts) 1 else 0,
        kaplanIndex = this.kaplanIndex.toDouble(),
        metricsAvailable = if (this.metricsAvailable) 1 else 0,
        motionArtifacts = if (this.motionArtifacts) 1 else 0,
        skinContact = if (this.skinContact) 1 else 0,
        stressIndex = this.stressIndex.toDouble()
    )
}

// Функции расширения для baseline и index entities
fun PhysiologicalBaselinesEntity.toServerDto(): PhysiologicalBaselineDto {
    return PhysiologicalBaselineDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        alpha = this.alpha.toDouble(),
        beta = this.beta.toDouble(),
        alphaGravity = this.alphaGravity.toDouble(),
        betaGravity = this.betaGravity.toDouble(),
        concentration = this.concentration.toDouble()
    )
}

fun ProductivityBaselinesEntity.toServerDto(): ProductivityBaselineDto {
    return ProductivityBaselineDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        gravity = this.gravity.toDouble(),
        productivity = this.productivity.toDouble(),
        fatigue = this.fatigue.toDouble(),
        reverseFatigue = this.reverseFatigue.toDouble(),
        relaxation = this.relaxation.toDouble(),
        concentration = this.concentration.toDouble()
    )
}

fun ProductivityIndexesEntity.toServerDto(): ProductivityIndexDto {
    return ProductivityIndexDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        relaxation = this.relaxation,
        stress = this.stress,
        gravityBaseline = this.gravityBaseline.toDouble(),
        productivityBaseline = this.productivityBaseline.toDouble(),
        fatigueBaseline = this.fatiqueBaseline.toDouble(),
        reverseFatigueBaseline = this.reverseFatiqueBaseline.toDouble(),
        relaxationBaseline = this.relaxationBaselines.toDouble(),
        concentrationBaseline = this.concentrationBaselines.toDouble(),
        hasArtifacts = this.hasArtifacts
    )
}