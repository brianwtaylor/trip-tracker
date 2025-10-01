package com.triptracker.service.location.domain.model

/**
 * Represents different location accuracy modes for adaptive tracking
 */
enum class LocationAccuracy(val priority: Int, val batteryImpact: Float) {
    HIGH_ACCURACY(100, 5.0f),      // GPS only, best accuracy, high battery
    BALANCED(75, 3.0f),            // GPS + Network, good accuracy, moderate battery
    LOW_POWER(50, 1.5f),           // Network primarily, acceptable accuracy, low battery
    PASSIVE(25, 0.5f);             // Existing locations only, minimal battery

    companion object {
        fun fromBatteryLevel(batteryPercent: Int): LocationAccuracy {
            return when {
                batteryPercent < 15 -> PASSIVE
                batteryPercent < 30 -> LOW_POWER
                batteryPercent < 60 -> BALANCED
                else -> HIGH_ACCURACY
            }
        }

        fun fromSpeed(speedKmh: Float): LocationAccuracy {
            return when {
                speedKmh < 5 -> HIGH_ACCURACY     // Stopped/traffic - need precision
                speedKmh < 30 -> BALANCED         // City driving - balanced approach
                else -> LOW_POWER                 // Highway - efficiency matters more
            }
        }
    }
}
