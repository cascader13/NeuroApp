package com.neuroproject.neuro.domain.model

/**
 * Domain-модель сессии для использования в UI слое
 * 
 * Отделён от Room Entity (SessionEntity) чтобы:
 * - UI не зависел от деталей реализации базы данных
 * - Можно было легко менять структуру Entity без изменения UI
 * - Упростить тестирование ViewModel
 * 
 * @property sessionId Уникальный идентификатор сессии (timestamp начала)
 * @property userId ID пользователя
 * @property expeditionId ID экспедиции
 * @property sessionCategory Категория сессии (утро/день/вечер)
 * @property startTime Время начала сессии в миллисекундах
 * @property endTime Время окончания сессии в миллисекундах
 * @property durationMinutes Длительность сессии в минутах
 * @property objectiveFatigue Объективный уровень утомления
 * @property objectiveStress Объективный уровень стресса
 * @property objectiveCognitive Объективный когнитивный индекс
 * @property objectivePsychological Объективный психологический индекс
 * @property objectivePhysiological Объективный физиологический индекс
 * @property subjectiveCognitive Субъективная когнитивная оценка
 * @property subjectivePsychological Субъективная психологическая оценка
 * @property subjectivePhysiological Субъективная физиологическая оценка
 * @property totalIndex Общий интегральный показатель
 * @property averageObjective Средний объективный показатель
 * @property averageSubjective Средний субъективный показатель
 * @property totalCognitive Комбинированный когнитивный показатель
 * @property totalPhysiological Комбинированный физиологический показатель
 * @property totalPsychological Комбинированный психологический показатель
 * @property comment Комментарий пользователя
 * @property passingPrematurely Флаг досрочного завершения
 * @property isSynced Флаг синхронизации с сервером
 */
data class Session(
    val sessionId: Long,
    val userId: String? = null,
    val expeditionId: String? = null,
    val sessionCategory: String? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMinutes: Int? = null,
    val objectiveFatigue: String? = null,
    val objectiveStress: String? = null,
    val objectiveCognitive: Int? = null,
    val objectivePsychological: Int? = null,
    val objectivePhysiological: Int? = null,
    val subjectiveCognitive: Int? = null,
    val subjectivePsychological: Int? = null,
    val subjectivePhysiological: Int? = null,
    val totalIndex: Int? = null,
    val averageObjective: Int? = null,
    val averageSubjective: Int? = null,
    val totalCognitive: Int? = null,
    val totalPhysiological: Int? = null,
    val totalPsychological: Int? = null,
    val comment: String? = null,
    val passingPrematurely: Boolean = false,
    val isSynced: Boolean = false
) {
    /**
     * Форматированная дата сессии для отображения в UI
     */
    val formattedDate: String
        get() {
            val date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            return date.format(java.util.Date(startTime))
        }
    
    /**
     * Форматированное время сессии для отображения в UI
     */
    val formattedTime: String
        get() {
            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return time.format(java.util.Date(startTime))
        }
    
    /**
     * Форматированная длительность
     */
    val formattedDuration: String
        get() = durationMinutes?.let { "$it мин" } ?: "—"
}

/**
 * Расширение для конвертации SessionEntity в Session
 * Должно быть в data слое, чтобы domain не зависел от Room
 */
fun com.neuroproject.neuro.data.session.SessionEntity.toDomain(): Session {
    return Session(
        sessionId = this.sessionId,
        userId = this.id,
        expeditionId = this.expedition_id,
        sessionCategory = this.sessionCategory?.name,
        startTime = this.sessionId, // sessionId это timestamp начала
        endTime = this.endTime,
        durationMinutes = this.durationMinutes,
        objectiveFatigue = this.objectiveFatigue,
        objectiveStress = this.objectiveStress,
        objectiveCognitive = this.objectiveCognitive,
        objectivePsychological = this.objectivePsychological,
        objectivePhysiological = this.objectivePhysiological,
        subjectiveCognitive = this.subjectiveCognitive,
        subjectivePsychological = this.subjectivePsychological,
        subjectivePhysiological = this.subjectivePhysiological,
        totalIndex = this.totalIndex,
        averageObjective = this.averageObjective,
        averageSubjective = this.averageSubjective,
        totalCognitive = this.totalCognitive,
        totalPhysiological = this.totalPhysiological,
        totalPsychological = this.totalPsychological,
        comment = this.comment,
        passingPrematurely = this.passingPrematurely,
        isSynced = this.isMarked
    )
}
