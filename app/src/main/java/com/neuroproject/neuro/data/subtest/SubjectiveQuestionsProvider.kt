package com.neuroproject.neuro.data.subtest

object SubjectiveQuestionsProvider {

    fun getQuestions(): List<SubjectiveQuestionEntity> {
        return listOf(

            // КОГНИТИВНЫЙ БЛОК
            SubjectiveQuestionEntity(
                id = 1,
                text = "Мне трудно сосредоточиться",
                blockType = BlockType.COGNITIVE,
                displayOrder = 1,
                isReversed = false
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
                isReversed = true
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

            // ФИЗИЧЕСКИЙ БЛОК
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
                isReversed = false
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
                isReversed = false
            ),

            // ЭМОЦИОНАЛЬНЫЙ БЛОК
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