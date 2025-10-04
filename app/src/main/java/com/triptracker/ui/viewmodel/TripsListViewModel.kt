package com.triptracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptracker.core.common.result.Result
import com.triptracker.core.domain.model.UserRole
import com.triptracker.core.ui.components.TripItem
// import com.triptracker.service.database.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Trips List Screen
 * Manages trip list state and user interactions
 */
@HiltViewModel
class TripsListViewModel @Inject constructor(
    private val tripRepository: com.triptracker.service.database.TripRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // UI State
    private val _state = MutableStateFlow<TripsListState>(TripsListState.Loading)
    val state: StateFlow<TripsListState> = _state.asStateFlow()

    // Refresh trigger
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
    val refreshTrigger = _refreshTrigger.asSharedFlow()

    init {
        loadTrips()
    }

    /**
     * Load all trips from database
     */
    private fun loadTrips() {
        viewModelScope.launch {
            _state.value = TripsListState.Loading

            tripRepository.getAllTrips()
                .map { tripEntities ->
                    val tripItems = tripEntities.map { it.toTripItem() }
                    val stats = calculateStats(tripItems)

                    TripsListState.Success(tripItems, stats)
                }
                .catch { error ->
                    _state.value = TripsListState.Error(
                        error.message ?: "Failed to load trips"
                    )
                }
                .collect { newState ->
                    _state.value = newState
                }
        }
    }

    /**
     * Refresh trips data
     */
    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
            loadTrips()
        }
    }

    /**
     * Delete a specific trip
     */
    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            try {
                tripRepository.deleteTrip(tripId)
                // Refresh the list after deletion
                loadTrips()
            } catch (e: Exception) {
                // Update state to show error
                val currentState = _state.value
                if (currentState is TripsListState.Success) {
                    _state.value = TripsListState.Error("Failed to delete trip: ${e.message}")
                }
            }
        }
    }

    /**
     * Convert database entity to UI model
     */
    private fun com.triptracker.service.database.entity.TripWithLocations.toTripItem(): TripItem {
        val trip = this.trip
        val locations = this.locations

        // Calculate distance from locations
        val distance = calculateDistance(locations)

        // Calculate duration with validation
        val validStartTime = if (trip.startTime > 0 && trip.startTime < System.currentTimeMillis()) {
            trip.startTime
        } else {
            System.currentTimeMillis() - 300000 // Assume 5 minutes ago for invalid start times
        }

        val validEndTime = trip.endTime?.let { endTime ->
            if (endTime > validStartTime && endTime <= System.currentTimeMillis()) {
                endTime
            } else {
                System.currentTimeMillis()
            }
        } ?: System.currentTimeMillis()

        val duration = validEndTime - validStartTime

        // Calculate average speed
        val avgSpeed = if (duration > 0) (distance / (duration / 3600000.0)) else 0.0

        // For now, mock the role classification (will integrate with activity service)
        // TODO: Get actual classification from activity recognition results
        val role = UserRole.UNKNOWN // Will be replaced with real data
        val confidence = 0.5f

        return TripItem(
            id = trip.id,
            date = dateFormat.format(Date(validStartTime)),
            time = timeFormat.format(Date(validStartTime)),
            duration = formatDuration(duration),
            distance = formatDistance(distance),
            averageSpeed = formatSpeed(avgSpeed),
            role = role,
            confidence = confidence,
            locationCount = locations.size
        )
    }

    /**
     * Calculate total distance from location points
     */
    private fun calculateDistance(locations: List<com.triptracker.service.database.entity.LocationEntity>): Double {
        if (locations.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 1 until locations.size) {
            val prev = locations[i - 1]
            val curr = locations[i]

            // Simple distance calculation (could use more accurate method)
            val distance = calculateSimpleDistance(
                prev.latitude, prev.longitude,
                curr.latitude, curr.longitude
            )
            totalDistance += distance
        }

        return totalDistance
    }

    /**
     * Simple distance calculation using Haversine formula approximation
     */
    private fun calculateSimpleDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val latDiff = Math.toRadians(lat2 - lat1)
        val lonDiff = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371.0 * c // Earth's radius in kilometers
    }

    /**
     * Calculate statistics from trip items
     */
    private fun calculateStats(trips: List<TripItem>): TripStats {
        return TripStats(
            totalTrips = trips.size,
            totalDistance = trips.sumOf { parseDistance(it.distance) },
            totalDuration = trips.sumOf { parseDuration(it.duration) },
            driverTrips = trips.count { it.role == UserRole.DRIVER },
            passengerTrips = trips.count { it.role == UserRole.PASSENGER },
            averageConfidence = trips.map { it.confidence }.average().toFloat()
        )
    }

    // Utility functions for parsing formatted strings back to numbers
    private fun parseDistance(distanceStr: String): Double {
        return distanceStr.replace(" mi", "").toDoubleOrNull() ?: 0.0
    }

    private fun parseDuration(durationStr: String): Long {
        // Parse "2h 30m" format
        val hours = durationStr.substringBefore("h").trim().toLongOrNull() ?: 0L
        val minutes = durationStr.substringAfter("h ").substringBefore("m").trim().toLongOrNull() ?: 0L
        return (hours * 3600000) + (minutes * 60000)
    }

    // Formatting functions
    private fun formatDistance(meters: Double): String {
        val miles = meters * 0.000621371 // Convert meters to miles
        return "%.1f mi".format(miles)
    }

    private fun formatDuration(durationMs: Long): String {
        val totalMinutes = durationMs / 1000 / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    private fun formatSpeed(speedMps: Double): String {
        val mph = speedMps * 2.23694 // Convert m/s to mph
        return "%.1f mph".format(mph)
    }
}

// UI State classes
sealed class TripsListState {
    object Loading : TripsListState()
    data class Success(
        val trips: List<TripItem>,
        val stats: TripStats
    ) : TripsListState()
    data class Error(val message: String) : TripsListState()
}

// UI Data classes
data class TripStats(
    val totalTrips: Int = 0,
    val totalDistance: Double = 0.0,
    val totalDuration: Long = 0L,
    val driverTrips: Int = 0,
    val passengerTrips: Int = 0,
    val averageConfidence: Float = 0f
)
