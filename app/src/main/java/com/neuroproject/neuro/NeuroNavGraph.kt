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
                    navActions.navigateToDeviceSearch()
                },
                onBackPressed = {
                    navActions.navigateBack()
                },
                vm = hiltViewModel()
            )
        }

        // Главный экран (пока пустышка)
        composable(NavDestinations.MAIN) {
            com.neuroproject.neuro.screens.main.MainScreen(
                modifier = Modifier.safeDrawingPadding(),
                onBackPressed = {
                    navActions.navigateBack()
                },
                vm = hiltViewModel()
            )
        }

        /**
         * PROBE STACK
         */
        navigation(
            startDestination = NavDestinations.ProbeNavStack.SEARCH,
            route = NavDestinations.PROBE_STACK
        ) {
            composable(NavDestinations.ProbeNavStack.SEARCH) {
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

            composable(NavDestinations.ProbeNavStack.SENSOR_CHECK) {
                SensorCheckingScreen(
                    modifier = Modifier.safeDrawingPadding(),
                    onBackPressed = {
                        navActions.navigateBack()
                    },
                    onSensorOk = {
                        navActions.navigateToMain()
                    },
                    vm = hiltViewModel()
                )
            }
        }
    }
}