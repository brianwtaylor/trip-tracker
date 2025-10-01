package com.triptracker.service.location.domain.usecase

import com.triptracker.core.common.result.Result
import com.triptracker.service.location.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Use case for stopping location updates
 */
class StopLocationUpdatesUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {

    /**
     * Stop all location updates
     * @return Success if stopped, Error if failed
     */
    operator fun invoke(): Result<Unit> {
        return locationRepository.stopLocationUpdates()
    }
}
