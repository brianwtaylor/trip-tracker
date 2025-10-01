package com.triptracker.core.common.util

import com.triptracker.core.common.constants.Constants

object SpeedConverter {
    
    /**
     * Convert meters per second to miles per hour
     */
    fun metersPerSecondToMph(metersPerSecond: Float): Double {
        return metersPerSecond * Constants.METERS_PER_SECOND_TO_MPH
    }
    
    /**
     * Convert meters per second to kilometers per hour
     */
    fun metersPerSecondToKmh(metersPerSecond: Float): Double {
        return metersPerSecond * Constants.METERS_PER_SECOND_TO_KMH
    }
    
    /**
     * Format speed for display
     * @param metersPerSecond speed in m/s
     * @param useMetric true for km/h, false for mph
     * @return formatted string like "65 mph" or "105 km/h"
     */
    fun formatSpeed(metersPerSecond: Float, useMetric: Boolean = false): String {
        return if (useMetric) {
            val kmh = metersPerSecondToKmh(metersPerSecond)
            "${kmh.toInt()} km/h"
        } else {
            val mph = metersPerSecondToMph(metersPerSecond)
            "${mph.toInt()} mph"
        }
    }
    
    /**
     * Calculate average speed
     * @param distanceMeters total distance in meters
     * @param durationMillis total duration in milliseconds
     * @return average speed in meters per second
     */
    fun calculateAverageSpeed(distanceMeters: Double, durationMillis: Long): Float {
        if (durationMillis <= 0) return 0f
        val durationSeconds = durationMillis / 1000.0
        return (distanceMeters / durationSeconds).toFloat()
    }
}
