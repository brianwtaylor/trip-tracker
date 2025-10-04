package com.triptracker.service.location.data.service

import android.location.Location
import com.triptracker.core.common.constants.Constants
import com.triptracker.service.location.domain.model.LocationUpdate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Filters location updates to remove noise, duplicates, and low-quality data
 */
@Singleton
class LocationFilter @Inject constructor() {

    private var lastValidLocation: Location? = null
    private var locationHistory = mutableListOf<Location>()

    // Keep only last 10 locations for filtering decisions
    private val maxHistorySize = 10

    /**
     * Filter a location update based on quality and relevance
     * @return true if location should be accepted, false if filtered out
     */
    fun shouldAcceptLocation(update: LocationUpdate): Boolean {
        val location = update.location

        // Basic quality checks
        if (!passesBasicQualityChecks(location)) {
            return false
        }

        // Check if location is significantly different from last valid location
        lastValidLocation?.let { last ->
            if (!passesDistanceFilter(location, last)) {
                return false
            }

            if (!passesTimeFilter(location, last)) {
                return false
            }

            if (!passesAccuracyFilter(location, last)) {
                return false
            }

            if (!passesSpeedConsistencyFilter(location, last)) {
                return false
            }
        }

        // If all filters pass, accept the location
        acceptLocation(location)
        return true
    }

    /**
     * Reset the filter state (useful when starting a new trip)
     */
    fun reset() {
        lastValidLocation = null
        locationHistory.clear()
    }

    /**
     * Get filter statistics
     */
    fun getFilterStats(): LocationFilterStats {
        return LocationFilterStats(
            totalLocationsProcessed = locationHistory.size,
            lastValidLocation = lastValidLocation
        )
    }

    /**
     * Basic quality checks - must pass all
     */
    private fun passesBasicQualityChecks(location: Location): Boolean {
        // Check accuracy
        if (!location.hasAccuracy() || location.accuracy > Constants.MIN_ACCURACY_METERS) {
            return false
        }

        // Check speed validity (negative speeds are invalid)
        if (location.speed < 0) {
            return false
        }

        // Check coordinates are reasonable
        if (abs(location.latitude) > 90 || abs(location.longitude) > 180) {
            return false
        }

        // Check timestamp is reasonable (not in future, not too old)
        val now = System.currentTimeMillis()
        val locationTime = location.time
        if (locationTime > now + 60000 || locationTime < now - 3600000) { // ±1 hour
            return false
        }

        return true
    }

    /**
     * Distance filter - ensure minimum movement
     */
    private fun passesDistanceFilter(newLocation: Location, lastLocation: Location): Boolean {
        val distance = newLocation.distanceTo(lastLocation)
        return distance >= Constants.MIN_DISTANCE_METERS
    }

    /**
     * Time filter - prevent too frequent updates
     */
    private fun passesTimeFilter(newLocation: Location, lastLocation: Location): Boolean {
        val timeDelta = newLocation.time - lastLocation.time
        val minInterval = Constants.LOCATION_UPDATE_INTERVAL / 2 // Allow some variance
        return timeDelta >= minInterval
    }

    /**
     * Accuracy filter - prefer more accurate locations
     */
    private fun passesAccuracyFilter(newLocation: Location, lastLocation: Location): Boolean {
        // Always accept if new location is significantly more accurate
        val accuracyImprovement = lastLocation.accuracy - newLocation.accuracy
        if (accuracyImprovement > 20) { // 20m improvement
            return true
        }

        // Accept if accuracy is reasonable
        return newLocation.accuracy <= lastLocation.accuracy * 1.5 // Allow 50% degradation
    }

    /**
     * Speed consistency filter - detect GPS jumps/errors
     */
    private fun passesSpeedConsistencyFilter(newLocation: Location, lastLocation: Location): Boolean {
        val distance = newLocation.distanceTo(lastLocation)
        val timeDeltaHours = (newLocation.time - lastLocation.time) / 3600000.0
        val calculatedSpeed = distance / 1000.0 / timeDeltaHours // km/h (internal calculation)

        // If calculated speed is impossibly high (> 186 mph), reject
        if (calculatedSpeed > 300) {
            return false
        }

        // Check speed consistency with reported speed
        val reportedSpeed = newLocation.speed * 3.6f // Convert m/s to km/h
        val speedDifference = abs(calculatedSpeed - reportedSpeed)

        // Allow reasonable speed differences (GPS can be noisy)
        return speedDifference < 50 // ~31 mph tolerance
    }

    /**
     * Accept a location and update filter state
     */
    private fun acceptLocation(location: Location) {
        lastValidLocation = location
        locationHistory.add(location)

        // Keep history size manageable
        if (locationHistory.size > maxHistorySize) {
            locationHistory.removeAt(0)
        }
    }
}

/**
 * Statistics about location filtering performance
 */
data class LocationFilterStats(
    val totalLocationsProcessed: Int = 0,
    val locationsAccepted: Int = 0,
    val locationsFiltered: Int = 0,
    val lastValidLocation: Location? = null
) {
    val filterRate: Float
        get() = if (totalLocationsProcessed > 0) {
            locationsFiltered.toFloat() / totalLocationsProcessed
        } else 0f

    val acceptanceRate: Float
        get() = if (totalLocationsProcessed > 0) {
            locationsAccepted.toFloat() / totalLocationsProcessed
        } else 0f
}
