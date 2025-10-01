package com.triptracker.service.location.domain.usecase

import com.triptracker.core.common.result.Result
import com.triptracker.service.location.domain.model.LocationAccuracy
import com.triptracker.service.location.domain.model.LocationUpdate
import com.triptracker.service.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for starting location updates with adaptive accuracy
 */
class StartLocationUpdatesUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {

    /**
     * Start location updates with the specified accuracy mode
     * @param accuracy The desired accuracy level (HIGH_ACCURACY, BALANCED, etc.)
     * @return Flow of location updates or errors
     */
    operator fun invoke(accuracy: LocationAccuracy = LocationAccuracy.BALANCED): Flow<Result<LocationUpdate>> {
        return locationRepository.startLocationUpdates(accuracy)
    }

    /**
     * Start location updates with adaptive accuracy (recommended)
     * The system will automatically adjust accuracy based on context
     */
    fun invokeAdaptive(): Flow<Result<LocationUpdate>> {
        // Start with balanced accuracy, will adapt based on trip state
        return locationRepository.startLocationUpdates(LocationAccuracy.BALANCED)
    }
}
