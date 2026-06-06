// data/mapper/SessionMapper.kt
package com.neuroproject.neuro.data.mapper

import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.data.session.SessionEntity
import com.neuroproject.neuro.domain.model.*

object SessionMapper {

    fun toDomain(entity: SessionEntity): Session {
        return Session(
            sessionId = entity.sessionId,
            userId = entity.id,  // маппим id из Entity в userId
            expeditionId = entity.expedition_id,  // маппим expedition_id в expeditionId
            startTime = entity.sessionId,  // sessionId и есть startTime
            endTime = entity.endTime,
            durationMinutes = entity.durationMinutes ?: 0,
            category = entity.sessionCategory ?: SessionCategory.TECHNICAL,
            comment = entity.comment,
            passedPrematurely = entity.passingPrematurely,
            isSynced = false,  // нет в Entity, используем дефолт

            // Собираем результаты из плоских полей Entity
            subjectiveResult = buildSubjectiveResult(entity),
            objectiveResult = buildObjectiveResult(entity),
            totalResult = buildTotalResult(entity)
        )
    }

    fun toEntity(session: Session): SessionEntity {
        return SessionEntity(
            sessionId = session.sessionId,
            id = session.userId,  // маппим userId в id
            expedition_id = session.expeditionId,  // маппим expeditionId в expedition_id
            endTime = session.endTime,
            durationMinutes = session.durationMinutes,
            sessionCategory = session.category,
            comment = session.comment,
            passingPrematurely = session.passedPrematurely,
            isMarked = false,

            // Субъективные оценки
            subjectiveCognitive = session.subjectiveResult?.cognitiveIndex,
            subjectivePsychological = session.subjectiveResult?.emotionalIndex,
            subjectivePhysiological = session.subjectiveResult?.physicalIndex,
            averageSubjective = session.subjectiveResult?.averageIndex,

            // Объективные оценки
            objectiveCognitive = session.objectiveResult?.cognitiveIndex,
            objectivePsychological = session.objectiveResult?.psychologicalIndex,
            objectivePhysiological = session.objectiveResult?.physiologicalIndex,
            averageObjective = session.objectiveResult?.averageIndex,
            objectiveFatigue = session.objectiveResult?.fatigueLevel,
            objectiveStress = session.objectiveResult?.stressLevel,

            // Итоговые
            totalCognitive = session.totalResult?.cognitiveIndex,
            totalPsychological = session.totalResult?.psychologicalIndex,
            totalPhysiological = session.totalResult?.physiologicalIndex,
            totalIndex = session.totalResult?.averageIndex
        )
    }

    private fun buildSubjectiveResult(entity: SessionEntity): SubjectiveResult? {
        val hasData = entity.subjectiveCognitive != null ||
                entity.subjectivePsychological != null ||
                entity.subjectivePhysiological != null
        return if (hasData) {
            SubjectiveResult(
                cognitiveIndex = entity.subjectiveCognitive ?: 0,
                emotionalIndex = entity.subjectivePsychological ?: 0,
                physicalIndex = entity.subjectivePhysiological ?: 0,
                averageIndex = entity.averageSubjective ?: 0
            )
        } else null
    }

    private fun buildObjectiveResult(entity: SessionEntity): ObjectiveFatigueResult? {
        val hasData = entity.objectiveCognitive != null ||
                entity.objectivePsychological != null ||
                entity.objectivePhysiological != null
        return if (hasData) {
            ObjectiveFatigueResult(
                cognitiveIndex = entity.objectiveCognitive ?: 0,
                psychologicalIndex = entity.objectivePsychological ?: 0,
                physiologicalIndex = entity.objectivePhysiological ?: 0,
                averageIndex = entity.averageObjective ?: 0,
                fatigueLevel = entity.objectiveFatigue ?: "",
                stressLevel = entity.objectiveStress ?: ""
            )
        } else null
    }

    private fun buildTotalResult(entity: SessionEntity): TotalFatigueResult? {
        val hasData = entity.totalCognitive != null ||
                entity.totalPsychological != null ||
                entity.totalPhysiological != null
        return if (hasData) {
            TotalFatigueResult(
                cognitiveIndex = entity.totalCognitive ?: 0,
                psychologicalIndex = entity.totalPsychological ?: 0,
                physiologicalIndex = entity.totalPhysiological ?: 0,
                averageIndex = entity.totalIndex ?: 0
            )
        } else null
    }
}