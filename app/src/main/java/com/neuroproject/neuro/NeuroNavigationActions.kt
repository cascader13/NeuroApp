package com.neuroproject.neuro


import androidx.navigation.NavController

class NeuroNavigationActions(val navController: NavController) {

    fun navigateToLogin() {
        navController.navigate(NavDestinations.LOGIN) {
            launchSingleTop = true
        }
    }

    fun navigateToDeviceSearch() {
        navController.navigate(NavDestinations.PROBE_STACK) {
            launchSingleTop = true
        }
    }

    fun navigateToSensorCheck() {
        navController.navigate(NavDestinations.ProbeNavStack.SENSOR_CHECK) {
            launchSingleTop = true
        }
    }

    fun navigateToMain() {
        navController.navigate(NavDestinations.MAIN) {
            launchSingleTop = true
            popUpTo(NavDestinations.LOGIN) { inclusive = true }
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}