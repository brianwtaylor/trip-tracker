package com.triptracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.triptracker.ui.screens.PermissionsScreen

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
            TripsListScreenPlaceholder(
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
            ActiveTripScreenPlaceholder(
                onBackClick = { navController.navigateUp() },
                onTripComplete = { tripId ->
                    navController.navigate(NavigationRoutes.tripDetail(tripId)) {
                        popUpTo(NavigationRoutes.TRIPS_LIST)
                    }
                }
            )
        }
        
        // Settings Screen
        composable(NavigationRoutes.SETTINGS) {
            SettingsScreenPlaceholder(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Permissions Screen (shown when permissions needed)
        composable(NavigationRoutes.PERMISSIONS) {
            PermissionsScreen(
                onPermissionsGranted = {
                    navController.navigate(NavigationRoutes.TRIPS_LIST) {
                        popUpTo(NavigationRoutes.PERMISSIONS) { inclusive = true }
                    }
                },
                onPermissionsDenied = {
                    // Handle permission denial - maybe show a message or exit
                    navController.navigate(NavigationRoutes.TRIPS_LIST) {
                        popUpTo(NavigationRoutes.PERMISSIONS) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Placeholder composables - these will be replaced with actual implementations
@Composable
private fun TripsListScreenPlaceholder(
    onTripClick: (String) -> Unit,
    onStartTripClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    androidx.compose.material3.Text("Trips List Screen - Coming Soon")
}

@Composable
private fun TripDetailScreenPlaceholder(
    tripId: String,
    onBackClick: () -> Unit
) {
    androidx.compose.material3.Text("Trip Detail Screen - Trip ID: $tripId")
}

@Composable
private fun ActiveTripScreenPlaceholder(
    onBackClick: () -> Unit,
    onTripComplete: (String) -> Unit
) {
    androidx.compose.material3.Text("Active Trip Screen - Coming Soon")
}

@Composable
private fun SettingsScreenPlaceholder(
    onBackClick: () -> Unit
) {
    androidx.compose.material3.Text("Settings Screen - Coming Soon")
}

