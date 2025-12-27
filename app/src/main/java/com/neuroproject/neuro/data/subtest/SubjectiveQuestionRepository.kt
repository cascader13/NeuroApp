package com.neuroproject.neuro.data.subtest

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectiveQuestionRepository @Inject constructor(
    private val dao: SubjectiveQuestionDao
) {
    suspend fun getAllQuestions(): List<SubjectiveQuestionEntity> {
        return dao.getAll()
    }

    suspend fun prefillQuestions() {
        val questions = listOf(
            SubjectiveQuestionEntity(1, "Насколько сложно вам сосредоточиться на текущих задачах?", "cognitive", 1),
            SubjectiveQuestionEntity(2, "Насколько снижена скорость мышления по сравнению с обычным состоянием?", "cognitive", 2),
            SubjectiveQuestionEntity(3, "Насколько замедлена ваша реакция на ошибки или отклонения в действиях?", "cognitive", 3),
            SubjectiveQuestionEntity(
                4,
                "Насколько выражены признаки рассеянности и трудности удержания внимания?", "cognitive", 4
            ),
            SubjectiveQuestionEntity(5, "Насколько утомительным стало принятие даже простых решений?", "cognitive", 5),
            SubjectiveQuestionEntity(6, "Насколько вы ощущаете раздражение или вспыльчивость?", "emotional", 6),
            SubjectiveQuestionEntity(
                7,
                "Насколько выражены тревожность, внутреннее беспокойство или эмоциональные колебания?", "emotional", 7
            ),
            SubjectiveQuestionEntity(8, "Насколько вы чувствуете эмоциональное истощение или опустошение?", "emotional", 8),
            SubjectiveQuestionEntity(9, "Насколько снижена ваша мотивация и интерес к происходящему?", "emotional", 9),
            SubjectiveQuestionEntity(
                10,
                "Насколько сильно ощущается внутреннее напряжение, не связанное с физической усталостью?", "emotional", 10
            ),
            SubjectiveQuestionEntity(
                11,
                "Насколько сильно вы ощущаете физическую усталость или мышечную слабость?", "physical", 11
            ),
            SubjectiveQuestionEntity(12, "Насколько выражены сонливость, тяжесть век, зевота?", "physical", 12),
            SubjectiveQuestionEntity(13, "Насколько снижено чувство бодрости и готовности к действиям?", "physical", 13),
            SubjectiveQuestionEntity(14, "Насколько сильно вы ощущаете физический дискомфорт или боль в теле?", "physical", 14),
            SubjectiveQuestionEntity(15, "Насколько низок ваш общий уровень физического тонуса?", "physical", 15)
        )
        dao.insertAll(questions)
    }
}