package com.triptracker.core.common.util

import android.location.Location
import com.triptracker.core.common.constants.Constants
import kotlin.math.*

object DistanceCalculator {
    
    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * @return distance in meters
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // meters
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Calculate distance between two Android Location objects
     * @return distance in meters
     */
    fun calculateDistance(location1: Location, location2: Location): Double {
        return calculateDistance(
            location1.latitude,
            location1.longitude,
            location2.latitude,
            location2.longitude
        )
    }
    
    /**
     * Convert meters to miles
     */
    fun metersToMiles(meters: Double): Double {
        return meters * Constants.METERS_TO_MILES
    }
    
    /**
     * Convert meters to kilometers
     */
    fun metersToKilometers(meters: Double): Double {
        return meters * Constants.METERS_TO_KILOMETERS
    }
    
    /**
     * Format distance for display
     * @param meters distance in meters
     * @param useMetric true for kilometers, false for miles
     * @return formatted string like "5.2 mi" or "8.4 km"
     */
    fun formatDistance(meters: Double, useMetric: Boolean = false): String {
        return if (useMetric) {
            val km = metersToKilometers(meters)
            "%.1f km".format(km)
        } else {
            val miles = metersToMiles(meters)
            "%.1f mi".format(miles)
        }
    }
}
