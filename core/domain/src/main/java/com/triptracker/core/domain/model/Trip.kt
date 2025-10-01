package com.triptracker.core.domain.model

import java.util.UUID

/**
 * Domain model representing a trip
 */
data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val startTime: Long,
    val endTime: Long? = null,
    val distance: Double = 0.0,
    val locations: List<TripLocation> = emptyList(),
    val averageSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val status: TripStatus = TripStatus.ACTIVE
) {
    /**
     * Calculate trip duration in milliseconds
     */
    fun getDuration(): Long {
        return (endTime ?: System.currentTimeMillis()) - startTime
    }
    
    /**
     * Check if trip is currently active
     */
    fun isActive(): Boolean {
        return status == TripStatus.ACTIVE
    }
    
    /**
     * Check if trip is completed
     */
    fun isCompleted(): Boolean {
        return status == TripStatus.COMPLETED
    }
    
    /**
     * Get start location
     */
    fun getStartLocation(): TripLocation? {
        return locations.firstOrNull()
    }
    
    /**
     * Get end location
     */
    fun getEndLocation(): TripLocation? {
        return locations.lastOrNull()
    }
    
    /**
     * Check if trip has location data
     */
    fun hasLocations(): Boolean {
        return locations.isNotEmpty()
    }
}
