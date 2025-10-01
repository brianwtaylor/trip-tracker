package com.triptracker.service.location.data.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.triptracker.service.location.domain.model.LocationAccuracy
import com.triptracker.service.location.domain.model.TripState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service manager for LocationTrackingService
 *
 * Provides a clean API for controlling the foreground service
 * Handles service lifecycle, permissions, and state management
 */
@Singleton
class LocationServiceManager @Inject constructor(
    private val context: Context
) {

    /**
     * Start location tracking service
     */
    fun startLocationTracking(
        accuracy: LocationAccuracy = LocationAccuracy.BALANCED,
        tripState: TripState? = null
    ): Boolean {
        if (!isServiceAvailable()) {
            return false
        }

        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
            putExtra(LocationTrackingService.EXTRA_ACCURACY, accuracy.name)
            tripState?.let { putExtra(LocationTrackingService.EXTRA_TRIP_STATE, it) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        return true
    }

    /**
     * Stop location tracking service
     */
    fun stopLocationTracking() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(intent)
    }

    /**
     * Update tracking accuracy
     */
    fun updateTrackingAccuracy(accuracy: LocationAccuracy) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_UPDATE_ACCURACY
            putExtra(LocationTrackingService.EXTRA_ACCURACY, accuracy.name)
        }
        context.startService(intent)
    }

    /**
     * Update trip state for adaptive behavior
     */
    fun updateTripState(tripState: TripState) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_UPDATE_TRIP_STATE
            putExtra(LocationTrackingService.EXTRA_TRIP_STATE, tripState)
        }
        context.startService(intent)
    }

    /**
     * Check if service is currently running
     */
    fun isServiceRunning(): Boolean {
        // This is a simplified check - in a real implementation,
        // you might want to use a more sophisticated approach
        // like maintaining a service state in shared preferences
        // or using a bound service for real-time status
        return true // Placeholder - service state management would be more complex
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasRequiredPermissions(): Boolean {
        // This would check for location permissions
        // Implementation would depend on your permission checking logic
        return true // Placeholder
    }

    /**
     * Check if device supports the required features
     */
    private fun isServiceAvailable(): Boolean {
        return hasRequiredPermissions()
    }

    /**
     * Get service status information
     */
    fun getServiceStatus(): ServiceStatus {
        return ServiceStatus(
            isRunning = isServiceRunning(),
            hasPermissions = hasRequiredPermissions()
        )
    }

    /**
     * Service status information
     */
    data class ServiceStatus(
        val isRunning: Boolean,
        val hasPermissions: Boolean,
        val currentAccuracy: LocationAccuracy? = null,
        val tripState: TripState? = null
    ) {
        val canStartTracking: Boolean
            get() = hasPermissions && !isRunning

        val canStopTracking: Boolean
            get() = isRunning

        val statusMessage: String
            get() = when {
                !hasPermissions -> "Location permissions required"
                isRunning -> "Trip tracking active"
                else -> "Ready to start tracking"
            }
    }
}
