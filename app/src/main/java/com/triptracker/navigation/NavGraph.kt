package com.triptracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.triptracker.ui.screens.ActiveTripScreen
import com.triptracker.ui.screens.PermissionsScreen
import com.triptracker.ui.screens.SettingsScreen
import com.triptracker.ui.screens.TripsListScreen

/**
 * Main navigation graph for the app
 */
@Composable
fun TripTrackerNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavigationRoutes.TRIPS_LIST
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Trips List Screen
        composable(NavigationRoutes.TRIPS_LIST) {
            TripsListScreen(
                onTripClick = { tripId ->
                    navController.navigate(NavigationRoutes.tripDetail(tripId))
                },
                onStartTripClick = {
                    navController.navigate(NavigationRoutes.ACTIVE_TRIP)
                },
                onSettingsClick = {
                    navController.navigate(NavigationRoutes.SETTINGS)
                }
            )
        }
        
        // Trip Detail Screen
        composable(
            route = NavigationRoutes.TRIP_DETAIL,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TripDetailScreenPlaceholder(
                tripId = tripId,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Active Trip Screen
        composable(NavigationRoutes.ACTIVE_TRIP) {
            ActiveTripScreen(
                onTripComplete = { tripId ->
                    navController.navigate(NavigationRoutes.tripDetail(tripId)) {
                        popUpTo(NavigationRoutes.TRIPS_LIST)
                    }
                },
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Settings Screen
        composable(NavigationRoutes.SETTINGS) {
            SettingsScreen(
                onBackClick = { navController.navigateUp() },
                onPermissionsClick = {
                    // Navigate to permissions in management mode
                    navController.navigate("${NavigationRoutes.PERMISSIONS}?management=true")
                }
            )
        }
        
        // Permissions Screen (shown when permissions needed)
        composable(
            route = NavigationRoutes.PERMISSIONS,
            arguments = listOf(
                navArgument("management") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isManagementMode = backStackEntry.arguments?.getBoolean("management") ?: false

            PermissionsScreen(
                onPermissionsGranted = {
                    if (isManagementMode) {
                        // In management mode, just go back to settings
                        navController.navigateUp()
                    } else {
                        // Initial setup: go to trips list
                        navController.navigate(NavigationRoutes.TRIPS_LIST) {
                            popUpTo(NavigationRoutes.PERMISSIONS) { inclusive = true }
                        }
                    }
                },
                onPermissionsDenied = {
                    if (isManagementMode) {
                        // In management mode, just go back to settings
                        navController.navigateUp()
                    } else {
                        // Initial setup: handle denial
                        navController.navigate(NavigationRoutes.TRIPS_LIST) {
                            popUpTo(NavigationRoutes.PERMISSIONS) { inclusive = true }
                        }
                    }
                },
                isManagementMode = isManagementMode,
                onBackClick = if (isManagementMode) {
                    { navController.navigateUp() }
                } else null
            )
        }
    }
}

// Placeholder composables - these will be replaced with actual implementations

@Composable
private fun TripDetailScreenPlaceholder(
    tripId: String,
    onBackClick: () -> Unit
) {
    androidx.compose.material3.Text("Trip Detail Screen - Trip ID: $tripId")
}



