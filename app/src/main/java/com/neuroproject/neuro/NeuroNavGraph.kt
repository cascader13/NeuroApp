package com.neuroproject.neuro


import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.neuroproject.neuro.screens.calibration.CalibrationScreen
import com.neuroproject.neuro.screens.devicesearch.DeviceSearchScreen
import com.neuroproject.neuro.screens.login.LoginScreen
import com.neuroproject.neuro.screens.sensorchecking.SensorCheckingScreen
import com.neuroproject.neuro.screens.subtest.SubTestScreen

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

        // Экран логина
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

        // Главный экран
        composable(NavDestinations.MAIN) {
            com.neuroproject.neuro.screens.main.MainScreen(
                modifier = Modifier.safeDrawingPadding(),
                vm = hiltViewModel(),
                onStartSessionClick = {
                    navActions.navigateToDeviceSearch()
                } ,
                onSettingsClick = {
                    navActions.navigateToSettings()
                }
            )
        }

        // Экран настроек
        composable(NavDestinations.SETTINGS){
            com.neuroproject.neuro.screens.settings.SettingsScreen(
                modifier = Modifier.safeDrawingPadding(),
                onBackClick = {
                    navActions.navigateToMain()
                },
                vm = hiltViewModel(),
            )
        }

        navigation(
            startDestination = NavDestinations.SEARCH,
            route = NavDestinations.PROBE_STACK
        ) {
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

            composable(NavDestinations.CALIBRATION) {
                CalibrationScreen (
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

            composable(NavDestinations.SUB_TEST) {
                SubTestScreen(
                    onFinish = {
                        navActions.navigateToMain()
                    }
                )
            }
        }
    }
}