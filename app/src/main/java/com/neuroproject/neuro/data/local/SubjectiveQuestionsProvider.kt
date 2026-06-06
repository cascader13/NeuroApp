package com.neuroproject.neuro.data.local

import com.neuroproject.neuro.data.subtest.BlockType
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
import com.neuroproject.neuro.domain.model.SubjectiveQuestion

/**
 * Провайдер вопросов субъективного тестирования
 *
 * Содержит статический список всех вопросов теста, разделенных на три блока:
 * - **Когнитивный блок** (вопросы 1-10): оценка мышления, концентрации, принятия решений
 * - **Физический блок** (вопросы 11-20): оценка физического состояния, энергии, усталости
 * - **Эмоциональный блок** (вопросы 21-30): оценка настроения, стабильности, стресса
 *
 * ## Принципы составления вопросов:
 * - Каждый блок содержит 10 вопросов для сбалансированной оценки
 * - Вопросы чередуют прямые и обратные формулировки для снижения эффекта социальной желательности
 * - Прямые вопросы описывают негативное состояние (высокий балл = хуже)
 * - Обратные вопросы описывают позитивное состояние (высокий балл = лучше)
 *
 * ## Обработка результатов:
 * Для расчета итоговых индексов по каждому блоку:
 * 1. Нормализация ответов с учетом [isReversed]
 * 2. Усреднение нормализованных значений
 * 3. Преобразование в шкалу 1-10 для [SessionEntity] индексов
 *
 * @see SubjectiveQuestion
 * @see BlockType
 */
object SubjectiveQuestionsProvider {

    /**
     * Получение полного списка вопросов
     *
     * @return Список из 30 вопросов для субъективного тестирования
     */
    fun getQuestions(): List<SubjectiveQuestionEntity> {
        return listOf(
            // ==================== КОГНИТИВНЫЙ БЛОК ====================
            // Вопросы о мышлении, концентрации, принятии решений
            SubjectiveQuestionEntity(
                id = 1,
                text = "Мне трудно сосредоточиться",
                blockType = BlockType.COGNITIVE,
                displayOrder = 1,
                isReversed = false      // Прямой вопрос: высокий балл = хуже
            ),
            SubjectiveQuestionEntity(
                id = 2,
                text = "Я стал(а) медленнее думать",
                blockType = BlockType.COGNITIVE,
                displayOrder = 2,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 3,
                text = "Мне сложно удерживать внимание на задаче",
                blockType = BlockType.COGNITIVE,
                displayOrder = 3,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 4,
                text = "Мне легко анализировать информацию",
                blockType = BlockType.COGNITIVE,
                displayOrder = 4,
                isReversed = true       // Обратный вопрос: высокий балл = лучше
            ),
            SubjectiveQuestionEntity(
                id = 5,
                text = "Я чувствую умственное истощение",
                blockType = BlockType.COGNITIVE,
                displayOrder = 5,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 6,
                text = "Я совершаю больше ошибок, чем обычно",
                blockType = BlockType.COGNITIVE,
                displayOrder = 6,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 7,
                text = "Мне трудно принимать решения",
                blockType = BlockType.COGNITIVE,
                displayOrder = 7,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 8,
                text = "Я ясно мыслю",
                blockType = BlockType.COGNITIVE,
                displayOrder = 8,
                isReversed = true
            ),
            SubjectiveQuestionEntity(
                id = 9,
                text = "Простые задачи требуют от меня усилий",
                blockType = BlockType.COGNITIVE,
                displayOrder = 9,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 10,
                text = "Мне сложно переключаться между задачами",
                blockType = BlockType.COGNITIVE,
                displayOrder = 10,
                isReversed = false
            ),

            // ==================== ФИЗИЧЕСКИЙ БЛОК ====================
            // Вопросы о физическом состоянии, энергии, усталости
            SubjectiveQuestionEntity(
                id = 11,
                text = "Я чувствую физическое истощение",
                blockType = BlockType.PHYSICAL,
                displayOrder = 1,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 12,
                text = "Моему телу тяжело двигаться",
                blockType = BlockType.PHYSICAL,
                displayOrder = 2,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 13,
                text = "У меня мало энергии",
                blockType = BlockType.PHYSICAL,
                displayOrder = 3,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 14,
                text = "Я чувствую себя физически бодро",
                blockType = BlockType.PHYSICAL,
                displayOrder = 4,
                isReversed = true
            ),
            SubjectiveQuestionEntity(
                id = 15,
                text = "Мне хочется прилечь или отдохнуть",
                blockType = BlockType.PHYSICAL,
                displayOrder = 5,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 16,
                text = "Я ощущаю мышечную слабость",
                blockType = BlockType.PHYSICAL,
                displayOrder = 6,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 17,
                text = "Я легко выполняю физические действия",
                blockType = BlockType.PHYSICAL,
                displayOrder = 7,
                isReversed = false  // Примечание: возможно, должен быть true
            ),
            SubjectiveQuestionEntity(
                id = 18,
                text = "Я чувствую сонливость",
                blockType = BlockType.PHYSICAL,
                displayOrder = 8,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 19,
                text = "Моё тело напряжено",
                blockType = BlockType.PHYSICAL,
                displayOrder = 9,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 20,
                text = "Я чувствую себя физически выносливым(ой)",
                blockType = BlockType.PHYSICAL,
                displayOrder = 10,
                isReversed = true
            ),

            // ==================== ЭМОЦИОНАЛЬНЫЙ БЛОК ====================
            // Вопросы о настроении, стабильности, стрессе
            SubjectiveQuestionEntity(
                id = 21,
                text = "Я чувствую эмоциональное истощение",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 1,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 22,
                text = "Меня легко вывести из себя",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 2,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 23,
                text = "Мне трудно сохранять спокойствие",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 3,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 24,
                text = "Я чувствую эмоциональную стабильность",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 4,
                isReversed = true
            ),
            SubjectiveQuestionEntity(
                id = 25,
                text = "Я ощущаю внутреннее напряжение",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 5,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 26,
                text = "Мне сложно радоваться",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 6,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 27,
                text = "Я стал(а) менее терпеливым(ой)",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 7,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 28,
                text = "Я чувствую эмоциональную опустошенность",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 8,
                isReversed = false
            ),
            SubjectiveQuestionEntity(
                id = 29,
                text = "Я сохраняю позитивный настрой",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 9,
                isReversed = true
            ),
            SubjectiveQuestionEntity(
                id = 30,
                text = "Мне хочется дистанцироваться от людей",
                blockType = BlockType.EMOTIONAL,
                displayOrder = 10,
                isReversed = false
            )
        )
    }
}