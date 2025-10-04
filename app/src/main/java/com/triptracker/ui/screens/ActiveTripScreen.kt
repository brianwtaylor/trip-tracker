package com.triptracker.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptracker.core.common.result.Result
import com.triptracker.core.domain.model.UserRole
import com.triptracker.core.ui.components.RoleIndicator
import com.triptracker.service.database.TripRepository
import com.triptracker.service.database.entity.TripEntity
import com.triptracker.service.database.entity.TripWithLocations
import com.triptracker.service.database.entity.LocationEntity
import com.triptracker.service.location.domain.model.LocationAccuracy
import com.triptracker.service.location.domain.repository.LocationRepository
import com.triptracker.service.location.domain.usecase.ManageLocationServiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

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
 * Speedometer component
 */
@Composable
private fun Speedometer(
    currentSpeed: Float,
    modifier: Modifier = Modifier
) {
    val mph = (currentSpeed * 3.6f * 0.621371f).coerceIn(0f, 140f) // Convert m/s to mph, cap at 140

    Box(
        modifier = modifier
            .size(180.dp)
            .background(
                color = Color.Black,
                shape = CircleShape
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer speed markings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2 - 20

            // Draw speed markings (0 to 140 mph)
            for (speed in 0..140 step 10) {
                val angle = (speed / 140f) * 270f - 135f // Start at -135°, end at +135°
                val angleRad = Math.toRadians(angle.toDouble())

                val startX = centerX + (radius - 15) * cos(angleRad).toFloat()
                val startY = centerY + (radius - 15) * sin(angleRad).toFloat()
                val endX = centerX + radius * cos(angleRad).toFloat()
                val endY = centerY + radius * sin(angleRad).toFloat()

                drawLine(
                    color = Color.White,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2f
                )

                // TODO: Draw speed numbers at major intervals (20, 40, 60, etc.)
                // This requires TextMeasurer in Compose - will implement later
            }

            // Draw needle
            val needleAngle = (mph / 140f) * 270f - 135f
            val needleAngleRad = Math.toRadians(needleAngle.toDouble())
            val needleLength = radius - 25

            drawLine(
                color = Color.Red,
                start = Offset(centerX, centerY),
                end = Offset(
                    centerX + needleLength * cos(needleAngleRad).toFloat(),
                    centerY + needleLength * sin(needleAngleRad).toFloat()
                ),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Draw center dot
            drawCircle(
                color = Color.Red,
                radius = 8f,
                center = Offset(centerX, centerY)
            )
        }

        // Digital speed display in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "%.0f".format(mph),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "MPH",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f)
                )
            )
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

        // Speedometer and Location Points
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speedometer takes up 2/3 of the width
            Box(
                modifier = Modifier.weight(2f),
                contentAlignment = Alignment.Center
            ) {
                Speedometer(
                    currentSpeed = state.averageSpeedKmh / 3.6f // Convert km/h back to m/s for speedometer
                )
            }

            // Location count takes up 1/3 of the width
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
    val miles = distanceKm * 0.621371 // Convert km to miles
    return "${"%.1f".format(miles)} miles"
}

private fun formatSpeed(speedKmh: Float): String {
    val mph = speedKmh * 0.621371 // Convert km/h to mph
    return "${"%.0f".format(mph)}mph"
}

// ViewModel for Active Trip Screen
@HiltViewModel
class ActiveTripViewModel @Inject constructor(
    private val tripRepository: com.triptracker.service.database.TripRepository,
    private val locationRepository: com.triptracker.service.location.domain.repository.LocationRepository,
    private val locationServiceManager: ManageLocationServiceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ActiveTripState>(ActiveTripState.Idle)
    val state: StateFlow<ActiveTripState> = _state.asStateFlow()

    private var currentTripId: String? = null
    private var startTime: Long = 0
    private var locationTrackingJob: Job? = null
    private var durationUpdateJob: Job? = null
    private val tripLocations = mutableListOf<LocationEntity>()
    private var lastLocation: android.location.Location? = null
    private var totalDistance: Double = 0.0
    private var maxSpeed: Float = 0f
    private var speedReadings = mutableListOf<Float>()

    fun startTrip() {
        // Reset trip data
        tripLocations.clear()
        lastLocation = null
        totalDistance = 0.0
        maxSpeed = 0f
        speedReadings.clear()

        startTime = System.currentTimeMillis()
        currentTripId = "trip_${startTime}"

        _state.value = ActiveTripState.Recording(
            durationMs = 0,
            distanceKm = 0f,
            averageSpeedKmh = 0f,
            locationCount = 0,
            detectedRole = UserRole.UNKNOWN,
            roleConfidence = 0f
        )

        // Start duration update timer
        durationUpdateJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Update every second
                val currentTime = System.currentTimeMillis()
                val duration = currentTime - startTime

                // Update duration in current state if it's recording
                val currentState = _state.value
                if (currentState is ActiveTripState.Recording) {
                    _state.value = currentState.copy(durationMs = duration)
                }
            }
        }

        // Start location tracking
        locationTrackingJob = viewModelScope.launch {
            locationRepository.startLocationUpdates(LocationAccuracy.HIGH_ACCURACY)
                .onEach { result ->
                    when (result) {
                        is Result.Success -> {
                            val locationUpdate = result.data
                            processLocationUpdate(locationUpdate)
                        }
                        is Result.Error -> {
                            // Handle location error - could show user warning
                            // For now, just continue
                        }
                        Result.Loading -> {
                            // Location service initializing
                        }
                    }
                }
                .collect()
        }
    }

    private fun processLocationUpdate(locationUpdate: com.triptracker.service.location.domain.model.LocationUpdate) {
        val location = locationUpdate.location

        // Skip invalid locations
        if (!locationUpdate.hasMinimumQuality()) return

        // Calculate distance from last location
        lastLocation?.let { last ->
            val distanceIncrement = calculateDistance(last, location)
            totalDistance += distanceIncrement
        }

        // Update speed tracking
        val speedMps = location.speed
        if (speedMps > 0) {
            speedReadings.add(speedMps)
            maxSpeed = maxOf(maxSpeed, speedMps)
        }

        // Store location data
        val locationEntity = LocationEntity(
            id = 0, // Auto-generated by Room
            tripId = currentTripId ?: return,
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = location.time,
            speed = speedMps,
            accuracy = location.accuracy
        )
        tripLocations.add(locationEntity)

        // Update last location
        lastLocation = location

        // Calculate current metrics
        val currentTime = System.currentTimeMillis()
        val duration = currentTime - startTime
        val averageSpeed = if (speedReadings.isNotEmpty()) {
            speedReadings.average().toFloat()
        } else 0f

        // Update UI state
        _state.value = ActiveTripState.Recording(
            durationMs = duration,
            distanceKm = (totalDistance / 1000.0).toFloat(), // Convert to km for state (will be converted to miles in UI)
            averageSpeedKmh = averageSpeed * 3.6f, // Convert m/s to km/h for state (will be converted to mph in UI)
            locationCount = tripLocations.size,
            detectedRole = UserRole.UNKNOWN, // TODO: Integrate activity recognition
            roleConfidence = 0f
        )
    }

    private fun calculateDistance(loc1: android.location.Location, loc2: android.location.Location): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            loc1.latitude, loc1.longitude,
            loc2.latitude, loc2.longitude,
            results
        )
        return results[0].toDouble()
    }

    fun stopTrip() {
        _state.value = ActiveTripState.Saving

        // Stop duration update timer
        durationUpdateJob?.cancel()
        durationUpdateJob = null

        // Stop location tracking
        locationTrackingJob?.cancel()
        locationTrackingJob = null

        // Stop location service
        locationRepository.stopLocationUpdates()

        // Save trip to database
        viewModelScope.launch {
            try {
                val endTime = System.currentTimeMillis()
                val averageSpeed = if (speedReadings.isNotEmpty()) {
                    speedReadings.average().toFloat()
                } else 0f

                // Create trip entity with real data
                val tripEntity = TripEntity(
                    id = currentTripId ?: "unknown",
                    startTime = startTime,
                    endTime = endTime,
                    distance = totalDistance / 1000.0, // Convert meters to kilometers for storage
                    averageSpeed = averageSpeed * 3.6, // Convert m/s to km/h for storage
                    maxSpeed = maxSpeed * 3.6, // Convert m/s to km/h for storage
                    status = "COMPLETED"
                )

                val tripWithLocations = TripWithLocations(
                    trip = tripEntity,
                    locations = tripLocations
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

    override fun onCleared() {
        super.onCleared()
        durationUpdateJob?.cancel()
        locationTrackingJob?.cancel()
        locationRepository.stopLocationUpdates()
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
