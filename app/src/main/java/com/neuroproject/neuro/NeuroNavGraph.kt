package com.neuroproject.neuro


import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.navigation
import com.neuroproject.neuro.screens.analysis.AnalysisScreen
import com.neuroproject.neuro.screens.calibration.CalibrationScreen
import com.neuroproject.neuro.screens.devicesearch.DeviceSearchScreen
import com.neuroproject.neuro.screens.login.LoginScreen
import com.neuroproject.neuro.screens.sensorchecking.SensorCheckingScreen

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
                }
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
                    onCalibrationComplete = {
                        navActions.navigateToAnalysis()
                    },
                    vm = hiltViewModel()
                )

            }

            composable(NavDestinations.ANALYSIS) {
                AnalysisScreen(
                    modifier = Modifier.safeDrawingPadding(),
                    onBackPressed = {
                        navActions.navigateBack()
                    },
                    vm = hiltViewModel()
                )
            }

        }
    }
}