package com.triptracker.service.location.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Tasks
import com.triptracker.core.common.constants.Constants
import com.triptracker.core.common.result.Result
import com.triptracker.service.location.domain.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for managing location updates with adaptive accuracy
 */
@Singleton
class LocationClient @Inject constructor(
    private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationFilter: LocationFilter
) {

    private var currentAccuracy: LocationAccuracy = LocationAccuracy.BALANCED
    private var locationCallback: LocationCallback? = null
    private var isTracking = false

    /**
     * Start location updates with specified accuracy
     */
    fun startLocationUpdates(accuracy: LocationAccuracy): Flow<Result<LocationUpdate>> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(Result.Error(Exception("Location permission not granted")))
            close()
            return@callbackFlow
        }

        if (!isLocationAvailable()) {
            trySend(Result.Error(Exception("Location services not available")))
            close()
            return@callbackFlow
        }

        currentAccuracy = accuracy
        isTracking = true

        val locationRequest = createLocationRequest(accuracy)
        locationCallback = createLocationCallback { location ->
            launch {
                val update = LocationUpdate(
                    location = location,
                    accuracy = accuracy,
                    timestamp = System.currentTimeMillis()
                )

                // Apply location filtering
                if (locationFilter.shouldAcceptLocation(update)) {
                    trySend(Result.Success(update))
                }
                // Filtered locations are silently ignored
            }
        }

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            trySend(Result.Success(LocationUpdate(Location(""), accuracy))) // Signal start
        } catch (e: SecurityException) {
            trySend(Result.Error(e))
            close()
            return@callbackFlow
        }

        awaitClose {
            stopLocationUpdates()
        }
    }

    /**
     * Stop location updates
     */
    fun stopLocationUpdates(): Result<Unit> {
        return try {
            locationCallback?.let { callback ->
                fusedLocationProviderClient.removeLocationUpdates(callback)
            }
            locationCallback = null
            isTracking = false
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Update location accuracy dynamically
     */
    fun updateAccuracy(newAccuracy: LocationAccuracy): Result<Unit> {
        if (!isTracking) {
            currentAccuracy = newAccuracy
            return Result.Success(Unit)
        }

        // Stop current updates
        stopLocationUpdates()

        // Start with new accuracy
        currentAccuracy = newAccuracy

        return Result.Success(Unit)
    }

    /**
     * Get last known location
     */
    suspend fun getLastKnownLocation(): Result<LocationUpdate?> {
        return try {
            if (!hasLocationPermission()) {
                return Result.Error(Exception("Location permission not granted"))
            }

            val location = Tasks.await(fusedLocationProviderClient.lastLocation)
            val update = location?.let {
                LocationUpdate(
                    location = it,
                    accuracy = currentAccuracy,
                    timestamp = System.currentTimeMillis()
                )
            }

            Result.Success(update)
        } catch (e: SecurityException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Calculate optimal accuracy based on trip state
     */
    fun calculateAdaptiveAccuracy(tripState: TripState): LocationAccuracy {
        // Start with battery-aware baseline
        val batteryAccuracy = LocationAccuracy.fromBatteryLevel(tripState.batteryLevel)

        // Adjust based on speed
        val speedAccuracy = LocationAccuracy.fromSpeed(tripState.currentSpeed)

        // Adjust based on trip duration (high accuracy at start/end)
        val durationAccuracy = when {
            tripState.durationMs < 120_000 -> LocationAccuracy.HIGH_ACCURACY // First 2 minutes
            tripState.durationMs > 3_600_000 -> LocationAccuracy.LOW_POWER // After 1 hour
            else -> LocationAccuracy.BALANCED
        }

        // Take the most restrictive accuracy (lowest priority number)
        val accuracies = listOf(batteryAccuracy, speedAccuracy, durationAccuracy)
        return accuracies.minByOrNull { it.priority } ?: LocationAccuracy.BALANCED
    }

    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation && coarseLocation
    }

    /**
     * Check if location services are available
     */
    fun isLocationAvailable(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Get current accuracy mode
     */
    fun getCurrentAccuracy(): LocationAccuracy = currentAccuracy

    /**
     * Check if currently tracking
     */
    fun isTracking(): Boolean = isTracking

    /**
     * Create location request based on accuracy
     */
    private fun createLocationRequest(accuracy: LocationAccuracy): LocationRequest {
        return LocationRequest.Builder(
            when (accuracy) {
                LocationAccuracy.HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
                LocationAccuracy.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
                LocationAccuracy.LOW_POWER -> Priority.PRIORITY_LOW_POWER
                LocationAccuracy.PASSIVE -> Priority.PRIORITY_PASSIVE
            },
            when (accuracy) {
                LocationAccuracy.HIGH_ACCURACY -> Constants.LOCATION_UPDATE_INTERVAL
                LocationAccuracy.BALANCED -> Constants.LOCATION_UPDATE_INTERVAL * 2
                LocationAccuracy.LOW_POWER -> Constants.LOCATION_UPDATE_INTERVAL * 4
                LocationAccuracy.PASSIVE -> Constants.LOCATION_UPDATE_INTERVAL * 8
            }
        )
            .setMinUpdateDistanceMeters(
                when (accuracy) {
                    LocationAccuracy.HIGH_ACCURACY -> Constants.MIN_DISTANCE_METERS.toFloat()
                    LocationAccuracy.BALANCED -> (Constants.MIN_DISTANCE_METERS * 2).toFloat()
                    LocationAccuracy.LOW_POWER -> (Constants.MIN_DISTANCE_METERS * 4).toFloat()
                    LocationAccuracy.PASSIVE -> (Constants.MIN_DISTANCE_METERS * 8).toFloat()
                }
            )
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setWaitForAccurateLocation(true)
            .build()
    }

    /**
     * Create location callback
     */
    private fun createLocationCallback(onLocation: (Location) -> Unit): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onLocation(location)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    // Location became unavailable - could notify user or adjust strategy
                }
            }
        }
    }
}
