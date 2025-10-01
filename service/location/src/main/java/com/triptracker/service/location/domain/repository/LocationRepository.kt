package com.triptracker.service.location.domain.repository

import com.triptracker.core.common.result.Result
import com.triptracker.service.location.domain.model.LocationAccuracy
import com.triptracker.service.location.domain.model.LocationUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for location data access
 */
interface LocationRepository {

    /**
     * Start location updates with specified accuracy
     */
    fun startLocationUpdates(accuracy: LocationAccuracy): Flow<Result<LocationUpdate>>

    /**
     * Stop location updates
     */
    fun stopLocationUpdates(): Result<Unit>

    /**
     * Get last known location
     */
    suspend fun getLastKnownLocation(): Result<LocationUpdate?>

    /**
     * Check if location services are available
     */
    fun isLocationAvailable(): Boolean

    /**
     * Check if we have required permissions
     */
    fun hasLocationPermission(): Boolean

    /**
     * Get current location accuracy mode
     */
    fun getCurrentAccuracy(): LocationAccuracy

    /**
     * Update location accuracy mode
     */
    fun updateAccuracy(accuracy: LocationAccuracy): Result<Unit>

    /**
     * Get location update statistics
     */
    fun getLocationStats(): LocationStats
}

/**
 * Statistics about location tracking performance
 */
data class LocationStats(
    val totalUpdates: Int = 0,
    val successfulUpdates: Int = 0,
    val failedUpdates: Int = 0,
    val averageAccuracy: Float = 0f,
    val batteryUsageEstimate: Float = 0f, // percentage per hour
    val lastUpdateTime: Long = 0L
)
