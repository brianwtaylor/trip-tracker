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
     * @param useImperial true for mph, false for km/h
     * @return formatted string like "65 mph" or "105 km/h"
     */
    fun formatSpeed(metersPerSecond: Float, useImperial: Boolean = true): String {
        return if (useImperial) {
            val mph = metersPerSecondToMph(metersPerSecond)
            "${mph.toInt()} mph"
        } else {
            val kmh = metersPerSecondToKmh(metersPerSecond)
            "${kmh.toInt()} km/h"
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
