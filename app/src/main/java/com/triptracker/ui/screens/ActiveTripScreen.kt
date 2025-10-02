package com.triptracker.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.triptracker.core.common.result.Result
import com.triptracker.core.domain.model.UserRole
import com.triptracker.core.ui.components.RoleIndicator
import com.triptracker.service.location.domain.usecase.ManageLocationServiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptracker.service.database.TripRepository
import com.triptracker.service.database.entity.TripEntity
import com.triptracker.service.database.entity.TripWithLocations
import com.triptracker.service.database.entity.LocationEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Active trip recording screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTripScreen(
    onTripComplete: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ActiveTripViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Permission launcher for background location (needed for trip tracking)
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startTrip()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Trip") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.stopTrip() }) {
                        Text("⏹️", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is ActiveTripState.Idle -> IdleState(
                    onStartTrip = {
                        // Check for background location permission
                        val hasBackgroundLocation = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PermissionChecker.PERMISSION_GRANTED

                        if (hasBackgroundLocation) {
                            viewModel.startTrip()
                        } else {
                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    }
                )

                is ActiveTripState.Recording -> RecordingState(
                    state = currentState,
                    onStopTrip = { viewModel.stopTrip() }
                )

                is ActiveTripState.Saving -> SavingState()

                is ActiveTripState.Completed -> CompletedState(
                    tripId = currentState.tripId,
                    onViewTrip = { onTripComplete(currentState.tripId) },
                    onBackToList = onBackClick
                )

                is ActiveTripState.Error -> ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.startTrip() },
                    onBack = onBackClick
                )
            }
        }
    }
}

/**
 * Idle state - waiting to start trip
 */
@Composable
private fun IdleState(onStartTrip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("▶️", style = MaterialTheme.typography.displayMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ready to Start Trip",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tap the button below to begin recording your trip. The app will track your location and detect driver/passenger activity.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartTrip,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Trip Recording")
        }
    }
}

/**
 * Recording state - actively tracking trip
 */
@Composable
private fun RecordingState(
    state: ActiveTripState.Recording,
    onStopTrip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Recording indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔴", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recording Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Trip tracking in progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Trip statistics
        TripStatsGrid(state = state)

        Spacer(modifier = Modifier.height(32.dp))

        // Stop trip button
        Button(
            onClick = onStopTrip,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("⏹️", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Trip & Save")
        }
    }
}

/**
 * Trip statistics grid during recording
 */
@Composable
private fun TripStatsGrid(state: ActiveTripState.Recording) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Duration and Distance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TripStatCard(
                iconText = "⏱️",
                title = "Duration",
                value = formatDuration(state.durationMs),
                modifier = Modifier.weight(1f)
            )

            TripStatCard(
                iconText = "📏",
                title = "Distance",
                value = formatDistance(state.distanceKm),
                modifier = Modifier.weight(1f)
            )
        }

        // Speed and Location Points
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TripStatCard(
                iconText = "🚗",
                title = "Avg Speed",
                value = formatSpeed(state.averageSpeedKmh),
                modifier = Modifier.weight(1f)
            )

            TripStatCard(
                iconText = "📍",
                title = "Locations",
                value = state.locationCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Role Detection
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Activity Detection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleIndicator(
                        role = state.detectedRole,
                        confidence = state.roleConfidence,
                        size = 32.dp
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (state.detectedRole) {
                                UserRole.DRIVER -> "Driver Detected"
                                UserRole.PASSENGER -> "Passenger Detected"
                                UserRole.UNKNOWN -> "Analyzing Activity..."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(state.roleConfidence * 100).toInt()}% confidence",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual stat card
 */
@Composable
private fun TripStatCard(
    iconText: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = iconText,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Saving state - processing trip data
 */
@Composable
private fun SavingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Saving Trip Data",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Processing location data and activity detection results...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Completed state - trip saved successfully
 */
@Composable
private fun CompletedState(
    tripId: String,
    onViewTrip: () -> Unit,
    onBackToList: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✅", style = MaterialTheme.typography.displayMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Trip Completed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your trip data has been saved successfully.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onViewTrip,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("👁️", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Trip Details")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBackToList,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Trip List")
        }
    }
}

/**
 * Error state - something went wrong
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", style = MaterialTheme.typography.displayMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Trip Recording Error",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retry")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Trips")
        }
    }
}

// Utility functions
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

private fun formatDistance(distanceKm: Float): String {
    return if (distanceKm < 1f) {
        "${"%.0f".format(distanceKm * 1000)}m"
    } else {
        "${"%.1f".format(distanceKm * 0.621371)}mi"
    }
}

private fun formatSpeed(speedKmh: Float): String {
    val mph = speedKmh * 0.621371 // Convert km/h to mph
    return "${"%.0f".format(mph)}mph"
}

// ViewModel for Active Trip Screen
@HiltViewModel
class ActiveTripViewModel @Inject constructor(
    private val tripRepository: com.triptracker.service.database.TripRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ActiveTripState>(ActiveTripState.Idle)
    val state: StateFlow<ActiveTripState> = _state.asStateFlow()

    private var currentTripId: String? = null
    private var startTime: Long = 0

    fun startTrip() {
        _state.value = ActiveTripState.Recording(
            durationMs = 0,
            distanceKm = 0f,
            averageSpeedKmh = 0f,
            locationCount = 0,
            detectedRole = UserRole.UNKNOWN,
            roleConfidence = 0f
        )

        // TODO: Start location tracking when service is implemented
        // locationServiceManager.startLocationTracking()

        startTime = System.currentTimeMillis()
        currentTripId = "trip_${System.currentTimeMillis()}"

        // Start periodic mock updates for demo purposes
        viewModelScope.launch {
            while (_state.value is ActiveTripState.Recording) {
                delay(2000) // Update every 2 seconds

                val currentTime = System.currentTimeMillis()
                val duration = currentTime - startTime

                // Mock realistic data progression
                       val mockDistance = (duration / 1000.0 / 60.0) * 0.5 // ~18 mph average
                       val mockSpeed = 15f + (4f * Math.random().toFloat()) // 15-19 mph variation
                val mockLocations = (duration / 1000).toInt() / 5 // Location every 5 seconds
                val mockRole = if (Math.random() > 0.3) UserRole.DRIVER else UserRole.PASSENGER
                val mockConfidence = 0.7f + (0.2f * Math.random().toFloat()) // 70-90% confidence

                _state.value = ActiveTripState.Recording(
                    durationMs = duration,
                    distanceKm = mockDistance.toFloat(),
                    averageSpeedKmh = mockSpeed,
                    locationCount = mockLocations,
                    detectedRole = mockRole,
                    roleConfidence = mockConfidence
                )
            }
        }
    }

    fun stopTrip() {
        _state.value = ActiveTripState.Saving

        // TODO: Stop location tracking when service is implemented
        // locationServiceManager.stopLocationTracking()

        // Save trip to database
        viewModelScope.launch {
            try {
                val endTime = System.currentTimeMillis()

                // Create mock trip entity - replace with real data when location service is connected
                val tripEntity = TripEntity(
                    id = currentTripId ?: "unknown",
                    startTime = startTime,
                    endTime = endTime,
                    distance = 0.0, // TODO: Calculate from location data
                    averageSpeed = 0.0, // TODO: Calculate from location data
                    maxSpeed = 0.0, // TODO: Calculate from location data
                    status = "COMPLETED"
                )

                // Create mock location entities - replace with real GPS data
                val mockLocations = listOf(
                    LocationEntity(
                        id = 1,
                        tripId = currentTripId ?: "unknown",
                        latitude = 0.0, // TODO: Real GPS coordinates
                        longitude = 0.0,
                        timestamp = startTime,
                        speed = 0.0f,
                        accuracy = 0.0f
                    )
                )

                val tripWithLocations = TripWithLocations(
                    trip = tripEntity,
                    locations = mockLocations
                )

                tripRepository.saveTrip(tripWithLocations)

                _state.value = ActiveTripState.Completed(
                    tripId = currentTripId ?: "unknown"
                )
            } catch (e: Exception) {
                _state.value = ActiveTripState.Error(
                    message = "Failed to save trip: ${e.message}"
                )
            }
        }
    }
}

// State classes
sealed class ActiveTripState {
    object Idle : ActiveTripState()

    data class Recording(
        val durationMs: Long,
        val distanceKm: Float,
        val averageSpeedKmh: Float,
        val locationCount: Int,
        val detectedRole: UserRole,
        val roleConfidence: Float
    ) : ActiveTripState()

    object Saving : ActiveTripState()

    data class Completed(val tripId: String) : ActiveTripState()

    data class Error(val message: String) : ActiveTripState()
}
