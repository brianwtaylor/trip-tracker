package com.triptracker.service.database.mapper

import com.triptracker.core.domain.model.TripLocation
import com.triptracker.service.database.entity.LocationEntity

/**
 * Convert LocationEntity to TripLocation domain model
 */
fun LocationEntity.toDomain(): TripLocation {
    return TripLocation(
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        speed = speed,
        accuracy = accuracy
    )
}

/**
 * Convert TripLocation domain model to LocationEntity
 */
fun TripLocation.toEntity(tripId: String): LocationEntity {
    return LocationEntity(
        tripId = tripId,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        speed = speed,
        accuracy = accuracy
    )
}
