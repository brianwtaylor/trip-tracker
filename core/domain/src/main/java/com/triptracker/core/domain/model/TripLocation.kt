package com.triptracker.core.domain.model

/**
 * Represents a single GPS location point during a trip
 */
data class TripLocation(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val speed: Float = 0f,
    val accuracy: Float = 0f
) {
    /**
     * Check if this location has acceptable accuracy
     */
    fun hasGoodAccuracy(threshold: Float = 50f): Boolean {
        return accuracy <= threshold && accuracy > 0
    }
    
    /**
     * Check if location is moving (speed > 0)
     */
    fun isMoving(): Boolean {
        return speed > 0.5f // ~1 mph threshold to filter out GPS drift
    }
}
