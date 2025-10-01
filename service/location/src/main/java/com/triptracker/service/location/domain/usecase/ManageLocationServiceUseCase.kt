package com.triptracker.service.location.domain.usecase

import com.triptracker.core.common.result.Result
import com.triptracker.service.location.data.service.LocationServiceManager
import com.triptracker.service.location.domain.model.LocationAccuracy
import com.triptracker.service.location.domain.model.TripState
import javax.inject.Inject

/**
 * Use case for managing the location tracking service
 *
 * Provides high-level operations for starting/stopping service,
 * updating accuracy, and monitoring status
 */
class ManageLocationServiceUseCase @Inject constructor(
    private val serviceManager: LocationServiceManager
) {

    /**
     * Start location tracking with specified accuracy
     */
    operator fun invoke(
        accuracy: LocationAccuracy = LocationAccuracy.BALANCED,
        tripState: TripState? = null
    ): Result<Unit> {
        return try {
            val success = serviceManager.startLocationTracking(accuracy, tripState)
            if (success) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Failed to start location service"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Start location tracking with adaptive accuracy
     */
    fun startAdaptiveTracking(tripState: TripState? = null): Result<Unit> {
        return invoke(LocationAccuracy.BALANCED, tripState)
    }

    /**
     * Stop location tracking
     */
    fun stopTracking(): Result<Unit> {
        return try {
            serviceManager.stopLocationTracking()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Update tracking accuracy
     */
    fun updateAccuracy(accuracy: LocationAccuracy): Result<Unit> {
        return try {
            serviceManager.updateTrackingAccuracy(accuracy)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Update trip state for adaptive behavior
     */
    fun updateTripState(tripState: TripState): Result<Unit> {
        return try {
            serviceManager.updateTripState(tripState)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Get current service status
     */
    fun getServiceStatus(): LocationServiceManager.ServiceStatus {
        return serviceManager.getServiceStatus()
    }

    /**
     * Check if service can be started
     */
    fun canStartTracking(): Boolean {
        return serviceManager.getServiceStatus().canStartTracking
    }

    /**
     * Check if service can be stopped
     */
    fun canStopTracking(): Boolean {
        return serviceManager.getServiceStatus().canStopTracking
    }
}
