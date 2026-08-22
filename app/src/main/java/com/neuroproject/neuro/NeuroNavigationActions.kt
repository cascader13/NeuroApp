package com.neuroproject.neuro

import androidx.navigation.NavController

/**
 * Класс, инкапсулирующий действия навигации приложения
 *
 * Предоставляет типобезопасные методы для навигации между экранами.
 * Используется для централизации логики перехода и управления стеком.
 *
 * ## Принципы:
 * - Все методы используют константы из [NavDestinations]
 * - Настройка launchSingleTop предотвращает дублирование экранов
 * - Очистка стека (popUpTo) при переходе на главный экран
 *
 * @param navController Контроллер навигации для выполнения переходов
 * @see NavDestinations
 */
class NeuroNavigationActions(val navController: NavController) {

    /**
     * Переход на экран входа
     */
    fun navigateToLogin() {
        navController.navigate(NavDestinations.LOGIN) {
            launchSingleTop = true  // Не создавать новый экземпляр, если уже на этом экране
        }
    }

    /**
     * Переход на экран субъективного тестирования
     */
    fun navigateToSubTest() {
        navController.navigate(NavDestinations.SUB_TEST) {
            launchSingleTop = true
        }
    }

    /**
     * Переход в стек подключения устройства (экран поиска)
     */
    fun navigateToDeviceSearch() {
        navController.navigate(NavDestinations.PROBE_STACK) {
            launchSingleTop = true
        }
    }

    /**
     * Переход на пустой экран (зарезервирован для будущего использования)
     */
    fun navigateToBlank() {
        navController.navigate(NavDestinations.BLANK) {
            launchSingleTop = true
        }
    }

    /**
     * Переход на экран проверки датчиков
     */
    fun navigateToSensorCheck() {
        navController.navigate(NavDestinations.SENSOR_CHECK) {
            launchSingleTop = true
        }
    }

    /**
     * Переход на экран калибровки
     */
    fun navigateToCalibration() {
        navController.navigate(NavDestinations.CALIBRATION) {
            launchSingleTop = true
        }
    }

    /**
     * Переход на экран настроек
     */
    fun navigateToSettings() {
        navController.navigate(NavDestinations.SETTINGS) {
            launchSingleTop = true
        }
    }

    fun navigateToHistory() {
        navController.navigate(NavDestinations.HISTORY) { launchSingleTop = true }
    }

    fun navigateToCharts() {
        navController.navigate(NavDestinations.CHARTS) { launchSingleTop = true }
    }

    fun navigateToSessionDetail(sessionId: Long) {
        navController.navigate("session_detail/$sessionId") { launchSingleTop = true }
    }

    /**
     * Переход на главный экран с очисткой стека
     *
     * Удаляет из стека все экраны вплоть до LOGIN включительно,
     * чтобы при нажатии "Назад" пользователь не возвращался на предыдущие экраны.
     */
    fun navigateToMain() {
        navController.navigate(NavDestinations.MAIN) {
            launchSingleTop = true
            popUpTo(NavDestinations.LOGIN) { inclusive = true }  // Очищаем стек до LOGIN
        }
    }

    /**
     * Переход на экран анализа данных
     */
    fun navigateToAnalysis() {
        navController.navigate(NavDestinations.ANALYSIS) {
            launchSingleTop = true
        }
    }

    /**
     * Возврат на предыдущий экран
     *
     * Удаляет текущий экран из стека навигации.
     */
    fun navigateBack() {
        navController.popBackStack()
    }
}