package com.triptracker.service.location.domain.model

import android.location.Location

/**
 * Enhanced location update with additional context for adaptive tracking
 */
data class LocationUpdate(
    val location: Location,
    val accuracy: LocationAccuracy,
    val tripState: TripState? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Convert to TripLocation domain model
     */
    fun toTripLocation() = com.triptracker.core.domain.model.TripLocation(
        latitude = location.latitude,
        longitude = location.longitude,
        timestamp = location.time,
        speed = location.speed,
        accuracy = location.accuracy
    )

    /**
     * Check if location meets minimum quality standards
     */
    fun hasMinimumQuality(): Boolean {
        return location.accuracy <= 100f && // Within 100 meters
               location.speed >= 0f &&      // Valid speed
               location.hasAccuracy()       // Has accuracy data
    }

    /**
     * Get location confidence score (0-100)
     */
    fun getConfidenceScore(): Int {
        val accuracy = location.accuracy
        val speed = location.speed

        // Accuracy score (0-50 points)
        val accuracyScore = when {
            accuracy <= 10f -> 50
            accuracy <= 25f -> 40
            accuracy <= 50f -> 30
            accuracy <= 100f -> 20
            else -> 0
        }

        // Speed validity score (0-50 points)
        val speedScore = when {
            speed >= 0f && speed <= 200f -> 50 // Reasonable speed range
            speed < 0f -> 0 // Invalid negative speed
            else -> 25 // Questionable but possible
        }

        return accuracyScore + speedScore
    }
}

/**
 * Represents the current state of a trip for adaptive decisions
 */
data class TripState(
    val tripId: String,
    val startTime: Long,
    val durationMs: Long,
    val currentSpeed: Float, // km/h
    val batteryLevel: Int,   // percentage
    val roadType: RoadType = RoadType.UNKNOWN
)

enum class RoadType {
    URBAN,      // City streets, frequent stops
    HIGHWAY,    // High-speed roads, steady travel
    RURAL,      // Country roads, variable conditions
    UNKNOWN     // Cannot determine
}
