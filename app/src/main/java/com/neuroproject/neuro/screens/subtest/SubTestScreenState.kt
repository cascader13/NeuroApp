package com.neuroproject.neuro.screens.subtest

sealed class SubTestScreenState {
    object SessionSettings : SubTestScreenState() // Выбор длительности сессии, калибровка, категория
    object Instruction : SubTestScreenState() // Вывод инструкции к тесту, кнопка начать
    object Question : SubTestScreenState()    // Вывод всех вопросов по очереди
    object Comment : SubTestScreenState()     // Окно для ввода комментария по сессии
    object Waiting : SubTestScreenState() // Ожидание, если тест закончился, таймер не закончился
    object Result : SubTestScreenState() // Вывод результатов
}