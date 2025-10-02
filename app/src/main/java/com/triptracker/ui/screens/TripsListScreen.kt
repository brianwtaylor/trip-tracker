package com.triptracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.triptracker.core.ui.components.TripCard
import com.triptracker.core.ui.components.TripItem
import com.triptracker.ui.viewmodel.TripsListState
import com.triptracker.ui.viewmodel.TripsListViewModel
import com.triptracker.ui.viewmodel.TripStats
import com.triptracker.R

/**
 * Main screen displaying list of recorded trips
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsListScreen(
    viewModel: TripsListViewModel = hiltViewModel(),
    onTripClick: (String) -> Unit,
    onStartTripClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Trip Tracker") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStartTripClick) {
                Icon(Icons.Default.Add, contentDescription = "Start New Trip")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                is TripsListState.Loading -> LoadingScreen()
                is TripsListState.Success -> {
                    val successState = state as TripsListState.Success
                    TripListContent(
                        trips = successState.trips,
                        stats = successState.stats,
                        onTripClick = onTripClick,
                        onRefresh = { viewModel.refresh() }
                    )
                }
                is TripsListState.Error -> {
                    val errorState = state as TripsListState.Error
                    ErrorScreen(
                        message = errorState.message,
                        onRetry = { viewModel.refresh() }
                    )
                }
            }
        }
    }
}

/**
 * Content for successful trip list loading
 */
@Composable
private fun TripListContent(
    trips: List<TripItem>,
    stats: TripStats,
    onTripClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Statistics summary
        item {
            TripStatsCard(stats = stats)
        }

        // Trip list
        if (trips.isEmpty()) {
            item {
                EmptyState(
                    onRefresh = onRefresh
                )
            }
        } else {
            items(trips) { trip ->
                TripCard(
                    trip = trip,
                    onClick = { onTripClick(trip.id) }
                )
            }
        }
    }
}

/**
 * Statistics summary card
 */
@Composable
private fun TripStatsCard(stats: TripStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Trip Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = stats.totalTrips.toString(),
                    label = "Total Trips"
                )
                StatItem(
                    value = "%.1f mi".format(stats.totalDistance * 0.621371),
                    label = "Total Distance"
                )
                StatItem(
                    value = formatDuration(stats.totalDuration),
                    label = "Total Time"
                )
            }

            if (stats.totalTrips > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = stats.driverTrips.toString(),
                        label = "Driver Trips",
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatItem(
                        value = stats.passengerTrips.toString(),
                        label = "Passenger Trips",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * Individual statistic item
 */
@Composable
private fun StatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Empty state when no trips exist
 */
@Composable
private fun EmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "No trips recorded yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Text(
            text = "Start your first trip to begin tracking your driving behavior and earning insights!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(onClick = onRefresh) {
            Text("Refresh")
        }
    }
}

/**
 * Loading screen
 */
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading trips...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Error screen with retry option
 */
@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Unable to load trips",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

/**
 * Utility function to format duration
 */
private fun formatDuration(durationMs: Long): String {
    val totalMinutes = durationMs / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}
