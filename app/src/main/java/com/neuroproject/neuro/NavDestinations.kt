package com.neuroproject.neuro

/**
 * Объект, содержащий константы маршрутов навигации
 *
 * Определяет все возможные экраны (destination) приложения и их маршруты.
 * Используется для безопасной навигации между экранами без строковых литералов.
 *
 * ## Структура навигации:
 * - **Основные экраны**: LOGIN, MAIN, SETTINGS
 * - **Стек проб**: PROBE_STACK (вложенная навигация для процесса подключения)
 * - **Экраны стека проб**: SEARCH, SENSOR_CHECK, CALIBRATION, SUB_TEST
 * - **Вспомогательные**: ANALYSIS (для будущего анализа данных)
 *
 * ## Пример использования:
 * ```kotlin
 * navController.navigate(NavDestinations.MAIN)
 * navController.navigate(NavDestinations.SETTINGS)
 * ```
 */
object NavDestinations {

    // ==================== ОСНОВНЫЕ ЭКРАНЫ ====================

    /** Экран входа/авторизации */
    const val LOGIN = "login"

    /** Главный экран приложения */
    const val MAIN = "main"

    /** Экран настроек */
    const val SETTINGS = "settings"

    /** Экран анализа данных (зарезервирован) */
    const val ANALYSIS = "analysis"

    /** Пустой экран (для будущего использования) */
    const val BLANK = "blank"

    /** История сессий */
    const val HISTORY = "history"

    /** Графики по сессиям */
    const val CHARTS = "charts"

    /** Подробности по сессии */
    const val SESSION_DETAIL = "session_detail/{sessionId}"

    // ==================== СТЕК ПОДКЛЮЧЕНИЯ УСТРОЙСТВА ====================

    /**
     * Корневой маршрут для стека подключения устройства.
     * Используется для вложенной навигации, объединяющей экраны:
     * - Поиск устройства
     * - Проверка датчиков
     * - Калибровка
     * - Субъективное тестирование
     */
    const val PROBE_STACK = "probe_stack"

    /** Экран поиска устройств */
    const val SEARCH = "search"

    /** Экран проверки датчиков (сопротивление электродов) */
    const val SENSOR_CHECK = "sensor_check"

    /** Экран калибровки устройства */
    const val CALIBRATION = "calibration"

    /** Экран субъективного тестирования после сессии */
    const val SUB_TEST = "sub_test"
}