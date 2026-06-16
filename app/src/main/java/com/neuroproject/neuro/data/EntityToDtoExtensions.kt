package com.neuroproject.neuro.data

import com.neuroproject.neuro.data.entity.*
import com.neuroproject.neuro.data.remote.*
import com.neuroproject.neuro.data.session.SessionEntity

/**
 * Преобразование сущностей Room в DTO для отправки на сервер
 *
 * Содержит функции расширения для преобразования всех типов метрик из
 * внутреннего формата (Room Entity) в формат для передачи по сети (DTO).
 *
 * ## Типы преобразований:
 * - **Uncompressed** - обычные метрики с полной точностью
 * - **Compressed** - сжатые метрики для уменьшения объема данных
 * - **Baseline** - базовые значения, полученные при калибровке
 * - **Indexes** - индексы продуктивности с текстовыми рекомендациями
 *
 * ## Принцип работы:
 * Каждой сущности Room соответствует функция .toServerDto(), которая
 * создает DTO объект для отправки на сервер.
 *
 * ## Важные преобразования:
 * - **sessionId** → преобразуется в Int (секунды) с помощью [toSecondsInt]
 * - **Boolean** → преобразуется в Int (0/1) для JSON сериализации
 * - **Float** → преобразуется в Double для единообразия на сервере
 *
 */

/**
 * Преобразование Long timestamp в секунды (Int)
 *
 * Используется для преобразования sessionId из миллисекунд в секунды
 * для соответствия формату API.
 *
 * @return Количество секунд от начала эпохи
 */
fun Long.toSecondsInt(): Int = (this / 1000).toInt()

// ==================== UNCOMPRESSED METRICS ====================

/**
 * Преобразование NFB метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        smr = this.smr.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование физиологической метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        cardioArtifacts = if (this.cardioArtifacts) 1 else 0,
        rowId = this.rowId
)
}

/**
 * Преобразование сырых данных ЭЭГ в DTO
 *
 * @return DTO для отправки на сервер
 */
fun EEGRawMetricEntity.toServerDto(): EEGRawMetricDto {
    return EEGRawMetricDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        channel1 = this.channel1,
        channel2 = this.channel2,
        rowId = this.rowId
)
}

/**
 * Преобразование обработанных данных ЭЭГ в DTO
 *
 * @return DTO для отправки на сервер
 */
fun EEGProceedMetricEntity.toServerDto(): EEGProceedMetricDto {
    return EEGProceedMetricDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        channel1 = this.channel1,
        channel2 = this.channel2,
        rowId = this.rowId
)
}

/**
 * Преобразование артефактов ЭЭГ в DTO
 *
 * @return DTO для отправки на сервер
 */
fun EEGArtifactsMetricEntity.toServerDto(): EEGArtifactMetricDto {
    return EEGArtifactMetricDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        artifactsChannel1 = this.artifactsChannel1,
        artifactsChannel2 = this.artifactsChannel2,
        qualityChannel1 = this.qualityChannel1,
        qualityChannel2 = this.qualityChannel2,
        rowId = this.rowId
)
}

/**
 * Преобразование MEMS метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        gyroscopeZ = this.gyroscopeZ.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование метрики продуктивности в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        concentration = this.concentration.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование эмоциональной метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        selfControl = this.selfControl.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование кардио метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        stressIndex = this.stressIndex.toDouble(),
        rowId = this.rowId
)
}

// ==================== COMPRESSED METRICS ====================

/**
 * Преобразование NFB Compressed метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        smr = this.smr.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование физиологической Compressed метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        cardioArtifacts = if (this.cardioArtifacts) 1 else 0,
        rowId = this.rowId
)
}

/**
 * Преобразование Compressed сырых данных ЭЭГ в DTO
 *
 * @return DTO для отправки на сервер
 */
fun EEGRawMetricCompressedEntity.toServerDto(): EEGRawMetricCompressedDto {
    return EEGRawMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        channel1 = this.channel1,
        channel2 = this.channel2,
        rowId = this.rowId
)
}

/**
 * Преобразование Compressed обработанных данных ЭЭГ в DTO
 *
 * @return DTO для отправки на сервер
 */
fun EEGProceedMetricCompressedEntity.toServerDto(): EEGProceedMetricCompressedDto {
    return EEGProceedMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        channel1 = this.channel1,
        channel2 = this.channel2,
        rowId = this.rowId
)
}

/**
 * Преобразование Compressed артефактов ЭЭГ в DTO
 *
 * @return DTO для отправки на сервер
 */
fun EEGArtifactsMetricCompressedEntity.toServerDto(): EEGArtifactMetricCompressedDto {
    return EEGArtifactMetricCompressedDto(
        individualNumber = this.id,
        expeditionId = this.expedition_id,
        timestamp = this.timestamp,
        session = this.sessionId.toSecondsInt(),
        artifactsChannel1 = this.artifactsChannel1,
        artifactsChannel2 = this.artifactsChannel2,
        qualityChannel1 = this.qualityChannel1,
        qualityChannel2 = this.qualityChannel2,
        rowId = this.rowId
)
}

/**
 * Преобразование Compressed MEMS метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        gyroscopeZ = this.gyroscopeZ.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование Compressed метрики продуктивности в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        concentration = this.concentration.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование Compressed эмоциональной метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        selfControl = this.selfControl.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование кардио метрики в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        stressIndex = this.stressIndex.toDouble(),
        rowId = this.rowId
)
}

// ==================== BASELINE AND INDEXES ====================

/**
 * Преобразование физиологических базовых значений в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        concentration = this.concentration.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование базовых значений продуктивности в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        concentration = this.concentration.toDouble(),
        rowId = this.rowId
)
}

/**
 * Преобразование индексов продуктивности в DTO
 *
 * @return DTO для отправки на сервер
 */
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
        fatigueBaseline = this.fatigueBaseline.toDouble(),
        reverseFatigueBaseline = this.reverseFatigueBaseline.toDouble(),
        relaxationBaseline = this.relaxationBaseline.toDouble(),
        concentrationBaseline = this.concentrationBaseline.toDouble(),
        hasArtifacts = this.hasArtifacts,
        rowId = this.rowId
)
}

/**
 * Преобразование данных о сессии в DTO
 *
 * @return DTO для отправки на сервер
 */
fun SessionEntity.toServerDto(): SessionDto {
    return SessionDto(
        session = sessionId.toSecondsInt(),
        expeditionId = expedition_id,
        individualNumber = id,
        objectiveCognitive = objectiveCognitive,
        objectivePsychological = objectivePsychological,
        objectivePhysiological = objectivePhysiological,
        subjectiveCognitive = subjectiveCognitive,
        subjectivePsychological = subjectivePsychological,
        subjectivePhysiological = subjectivePhysiological,
        totalIndex = totalIndex,
        averageObjective = averageObjective,
        averageSubjective = averageSubjective,
        totalCognitive = totalCognitive,
        totalPsychological = totalPsychological,
        totalPhysiological =  totalPhysiological,
        durationMinutes = durationMinutes,
        endTime = endTime?.toSecondsInt(),
        sessionCategory = sessionCategory,
        comment = comment,
        objectiveFatigue = objectiveFatigue,
        objectiveStress = objectiveStress,
        passingPrematurely = passingPrematurely,
        localSessionId = sessionId
)
}