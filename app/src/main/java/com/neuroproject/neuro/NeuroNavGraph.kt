package com.neuroproject.neuro

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neuroproject.neuro.presentation.screens.calibration.CalibrationScreen
import com.neuroproject.neuro.presentation.screens.devicesearch.DeviceSearchScreen
import com.neuroproject.neuro.presentation.screens.charts.ChartsScreen
import com.neuroproject.neuro.presentation.screens.history.HistoryScreen
import com.neuroproject.neuro.presentation.screens.sessiondetail.SessionDetailScreen
import com.neuroproject.neuro.presentation.screens.login.LoginScreen
import com.neuroproject.neuro.presentation.screens.sensorchecking.SensorCheckingScreen
import com.neuroproject.neuro.presentation.screens.subtest.SubTestScreen

/**
 * Граф навигации приложения
 *
 * Определяет структуру навигации между экранами, включая вложенную навигацию.
 * Использует Jetpack Navigation Compose для декларативного описания маршрутов.
 *
 * ## Структура навигации:
 * ```
 * LOGIN (экран входа)
 *   └── MAIN (главный экран)
 *        ├── SETTINGS (настройки)
 *        └── PROBE_STACK (вложенный стек)
 *             ├── SEARCH (поиск устройства)
 *             ├── SENSOR_CHECK (проверка датчиков)
 *             ├── CALIBRATION (калибровка)
 *             └── SUB_TEST (субъективный тест)
 * ```
 *
 * @param modifier Модификатор для применения ко всем экранам
 * @param navController Контроллер навигации (по умолчанию создается через rememberNavController)
 * @param startDestination Начальный экран (по умолчанию LOGIN)
 * @param navActions Действия навигации (создаются автоматически)
 * @see NeuroNavigationActions
 */
@Composable
fun NeuroNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavDestinations.LOGIN,
    navActions: NeuroNavigationActions = remember(navController) {
        NeuroNavigationActions(navController)
    }
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {

        // ==================== ОСНОВНЫЕ ЭКРАНЫ ====================

        /**
         * Экран входа/авторизации
         * При успешном входе переходит на главный экран
         */
        composable(NavDestinations.LOGIN) {
            LoginScreen(
                modifier = Modifier.safeDrawingPadding(),
                onLoginSuccess = {
                    navActions.navigateToMain()
                },
                onBackPressed = {
                    navActions.navigateBack()
                },
                vm = hiltViewModel()
            )
        }

        /**
         * Главный экран приложения
         * Содержит кнопки для начала сессии и перехода в настройки
         */
        composable(NavDestinations.MAIN) {
            com.neuroproject.neuro.presentation.screens.main.MainScreen(
                modifier = Modifier.safeDrawingPadding(),
                vm = hiltViewModel(),
                onStartSessionClick = {
                    navActions.navigateToDeviceSearch()
                },
                onSettingsClick = {
                    navActions.navigateToSettings()
                },
                onViewResultsClick = {
                    navActions.navigateToHistory()
                }
            )
        }

        /**
         * Экран настроек
         * Управление ID пользователя, экспедиции и адресом сервера
         */
        composable(NavDestinations.SETTINGS) {
            com.neuroproject.neuro.presentation.screens.settings.SettingsScreen(
                modifier = Modifier.safeDrawingPadding(),
                onBackClick = {
                    navActions.navigateToMain()
                },
                vm = hiltViewModel(),
            )
        }

        // ==================== ЭКРАНЫ ИСТОРИИ И ГРАФИКОВ ====================

        /**
         * Экран истории сессий
         * Отображает список всех завершённых сессий с основными показателями
         */
        composable(NavDestinations.HISTORY) {
            HistoryScreen(
                onBackClick = { navActions.navigateBack() },
                onChartClick = { navActions.navigateToCharts() },
                onSessionClick = { sessionId -> navActions.navigateToSessionDetail(sessionId) }
            )
        }

        /**
         * Экран графиков тенденций
         * Позволяет выбрать метрики и построить график их изменения по сессиям
         */
        composable(NavDestinations.CHARTS) {
            ChartsScreen(
                onBackClick = { navActions.navigateBack() }
            )
        }

        composable(
            route = NavDestinations.SESSION_DETAIL,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            SessionDetailScreen(
                onBackClick = { navActions.navigateBack() }
            )
        }

        // ==================== ВЛОЖЕННАЯ НАВИГАЦИЯ (СТЕК ПОДКЛЮЧЕНИЯ) ====================

        /**
         * Вложенный граф навигации для процесса подключения устройства.
         * Все экраны внутри этого блока имеют общий обратный стек.
         */
        navigation(
            startDestination = NavDestinations.SEARCH,
            route = NavDestinations.PROBE_STACK
        ) {

            /**
             * Экран поиска устройств
             * Сканирует Bluetooth и отображает найденные нейро-гарнитуры
             */
            composable(NavDestinations.SEARCH) {
                DeviceSearchScreen(
                    modifier = Modifier.safeDrawingPadding(),
                    onBackPressed = {
                        navActions.navigateBack()
                    },
                    onDeviceConnected = {
                        navActions.navigateToSensorCheck()
                    },
                    vm = hiltViewModel()
                )
            }

            /**
             * Экран проверки датчиков
             * Отображает сопротивление электродов для проверки качества контакта
             */
            composable(NavDestinations.SENSOR_CHECK) {
                SensorCheckingScreen(
                    modifier = Modifier.safeDrawingPadding(),
                    onBackPressed = {
                        navActions.navigateBack()
                    },
                    onDeviceUnconnected = {
                        navActions.navigateToMain()
                    },
                    onSensorOk = {
                        navActions.navigateToCalibration()
                    },
                    vm = hiltViewModel()
                )
            }

            /**
             * Экран калибровки
             * Проводит 60-секундную калибровку с метрономом
             */
            composable(NavDestinations.CALIBRATION) {
                CalibrationScreen(
                    modifier = Modifier.safeDrawingPadding(),
                    onBackPressed = {
                        navActions.navigateBack()
                    },
                    onDeviceUnconnected = {
                        navActions.navigateToMain()
                    },
                    onCalibrationComplete = {
                        navActions.navigateToSubTest()
                    },
                    vm = hiltViewModel()
                )
            }

            /**
             * Экран субъективного тестирования
             * Опрос пользователя после завершения сессии
             */
            composable(NavDestinations.SUB_TEST) {
                SubTestScreen(
                    onFinish = {
                        navActions.navigateToMain()
                    },
                    onDeviceUnconnected = {
                        navActions.navigateToMain()
                    }
                )
            }
        }
    }
}