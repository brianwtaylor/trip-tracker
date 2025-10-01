package com.triptracker.service.database.mapper

import com.triptracker.core.domain.model.Trip
import com.triptracker.core.domain.model.TripStatus
import com.triptracker.service.database.entity.TripEntity
import com.triptracker.service.database.entity.TripWithLocations

/**
 * Convert TripEntity to Trip domain model
 */
fun TripEntity.toDomain(): Trip {
    return Trip(
        id = id,
        startTime = startTime,
        endTime = endTime,
        distance = distance,
        locations = emptyList(), // Locations loaded separately
        averageSpeed = averageSpeed,
        maxSpeed = maxSpeed,
        status = TripStatus.valueOf(status)
    )
}

/**
 * Convert TripWithLocations to Trip domain model with locations
 */
fun TripWithLocations.toDomain(): Trip {
    return Trip(
        id = trip.id,
        startTime = trip.startTime,
        endTime = trip.endTime,
        distance = trip.distance,
        locations = locations.map { it.toDomain() },
        averageSpeed = trip.averageSpeed,
        maxSpeed = trip.maxSpeed,
        status = TripStatus.valueOf(trip.status)
    )
}

/**
 * Convert Trip domain model to TripEntity
 */
fun Trip.toEntity(): TripEntity {
    return TripEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        distance = distance,
        averageSpeed = averageSpeed,
        maxSpeed = maxSpeed,
        status = status.name
    )
}
