package com.triptracker.service.location.data.repository

import com.triptracker.core.common.result.Result
import com.triptracker.service.location.data.service.LocationClient
import com.triptracker.service.location.domain.model.LocationAccuracy
import com.triptracker.service.location.domain.model.LocationUpdate
import com.triptracker.service.location.domain.repository.LocationRepository
import com.triptracker.service.location.domain.repository.LocationStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LocationRepository using LocationClient
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationClient: LocationClient
) : LocationRepository {

    private var locationStats = LocationStats()

    override fun startLocationUpdates(accuracy: LocationAccuracy): Flow<Result<LocationUpdate>> {
        return locationClient.startLocationUpdates(accuracy)
            .map { result ->
                when (result) {
                    is Result.Success -> {
                        val update = result.data
                        // Update statistics
                        locationStats = locationStats.copy(
                            totalUpdates = locationStats.totalUpdates + 1,
                            successfulUpdates = locationStats.successfulUpdates + 1,
                            averageAccuracy = calculateNewAverageAccuracy(update.location.accuracy),
                            lastUpdateTime = update.timestamp
                        )
                    }
                    is Result.Error -> {
                        // Update error statistics
                        locationStats = locationStats.copy(
                            totalUpdates = locationStats.totalUpdates + 1,
                            failedUpdates = locationStats.failedUpdates + 1
                        )
                    }
                    Result.Loading -> {
                        // No statistics update needed for loading
                    }
                }
                result
            }
    }

    override fun stopLocationUpdates(): Result<Unit> {
        return locationClient.stopLocationUpdates()
    }

    override suspend fun getLastKnownLocation(): Result<LocationUpdate?> {
        return locationClient.getLastKnownLocation()
    }

    override fun isLocationAvailable(): Boolean {
        return locationClient.isLocationAvailable()
    }

    override fun hasLocationPermission(): Boolean {
        return locationClient.hasLocationPermission()
    }

    override fun getCurrentAccuracy(): LocationAccuracy {
        return locationClient.getCurrentAccuracy()
    }

    override fun updateAccuracy(accuracy: LocationAccuracy): Result<Unit> {
        return locationClient.updateAccuracy(accuracy)
    }

    override fun getLocationStats(): LocationStats {
        return locationStats
    }

    /**
     * Calculate running average accuracy
     */
    private fun calculateNewAverageAccuracy(newAccuracy: Float): Float {
        val totalUpdates = locationStats.successfulUpdates
        if (totalUpdates <= 0) return newAccuracy

        val currentAverage = locationStats.averageAccuracy
        // Weighted average: 90% historical + 10% new
        return (currentAverage * 0.9f) + (newAccuracy * 0.1f)
    }
}
