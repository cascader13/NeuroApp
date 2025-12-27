package com.neuroproject.neuro


import androidx.navigation.NavController

class NeuroNavigationActions(val navController: NavController) {

    fun navigateToLogin() {
        navController.navigate(NavDestinations.LOGIN) {
            launchSingleTop = true
        }
    }

    fun navigateToSubTest() {
        navController.navigate(NavDestinations.SUB_TEST) {
            launchSingleTop = true
        }
    }

    fun navigateToDeviceSearch() {
        navController.navigate(NavDestinations.PROBE_STACK) {
            launchSingleTop = true
        }
    }

    fun navigateToSensorCheck() {
        navController.navigate(NavDestinations.SENSOR_CHECK) {
            launchSingleTop = true
        }
    }

    fun navigateToCalibration(){
        navController.navigate(NavDestinations.CALIBRATION){
            launchSingleTop = true
        }
    }

    fun navigateToSettings(){
        navController.navigate(NavDestinations.SETTINGS){
            launchSingleTop = true
        }
    }

    fun navigateToMain() {
        navController.navigate(NavDestinations.MAIN) {
            launchSingleTop = true
            popUpTo(NavDestinations.LOGIN) { inclusive = true }
        }
    }

    fun navigateToAnalysis() {
        navController.navigate(NavDestinations.ANALYSIS) {
            launchSingleTop = true
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}