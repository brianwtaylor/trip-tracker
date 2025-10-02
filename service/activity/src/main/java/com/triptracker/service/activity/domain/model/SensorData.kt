package com.triptracker.service.activity.domain.model

import kotlin.time.Duration

/**
 * Aggregated sensor data collected during a trip segment
 * Used for heuristic-based driver/passenger classification
 */
data class SensorData(
    val timestamp: Long = System.currentTimeMillis(),

    // Accelerometer data (phone movement)
    val accelerometerVariance: Float = 0f,      // How much phone is moving
    val accelerometerStability: Float = 0f,     // How stable phone position is

    // Gyroscope data (phone rotation)
    val gyroscopeVariance: Float = 0f,          // How much phone is rotating
    val gyroscopeStability: Float = 0f,         // How stable phone orientation is

    // Motion correlation (phone vs expected vehicle motion)
    val motionCorrelation: Float = 0f,          // Correlation with vehicle motion patterns

    // Screen usage patterns
    val screenOnTime: Duration = Duration.ZERO,         // How long screen is on
    val touchEvents: Int = 0,                  // Number of touch interactions
    val appLaunches: Int = 0,                  // Number of app launches

    // Contextual information
    val tripDuration: Duration = Duration.ZERO,        // Current trip duration
    val timeOfDay: Int = 0,                    // Hour of day (0-23)
    val isCharging: Boolean = false,           // Phone charging status

    // Battery and performance
    val batteryLevel: Int = 100,               // Battery percentage
    val isPowerSaving: Boolean = false,        // Power saving mode active
) {

    /**
     * Indicates if the phone is relatively stable (driver behavior)
     */
    val isPhoneStable: Boolean
        get() = accelerometerStability > STABILITY_THRESHOLD &&
                gyroscopeStability > STABILITY_THRESHOLD

    /**
     * Indicates high screen usage (passenger behavior)
     */
    val isHighScreenUsage: Boolean
        get() = screenOnTime > HIGH_USAGE_THRESHOLD ||
                touchEvents > HIGH_TOUCH_THRESHOLD

    /**
     * Indicates low activity patterns (typical driver behavior)
     */
    val isLowActivity: Boolean
        get() = touchEvents < LOW_ACTIVITY_THRESHOLD &&
                appLaunches < LOW_ACTIVITY_THRESHOLD

    /**
     * Overall motion score (higher = more movement)
     */
    val motionScore: Float
        get() = (accelerometerVariance + gyroscopeVariance) / 2f

    /**
     * Activity score (higher = more interaction)
     */
    val activityScore: Float
        get() = (touchEvents + appLaunches).toFloat() / maxOf(tripDuration.inWholeSeconds.toFloat(), 1f)

    companion object {
        // Threshold constants for classification
        const val STABILITY_THRESHOLD = 0.7f
        val HIGH_USAGE_THRESHOLD = Duration.parse("5m")  // 5 minutes screen on
        const val HIGH_TOUCH_THRESHOLD = 50              // 50+ touches
        const val LOW_ACTIVITY_THRESHOLD = 5              // 5 or fewer interactions

        /**
         * Creates empty sensor data for initialization
         */
        fun empty() = SensorData()

        /**
         * Creates sensor data with default values for testing
         */
        fun createForTesting(
            accelerometerStability: Float = 0.5f,
            gyroscopeStability: Float = 0.5f,
            screenOnTime: Duration = Duration.parse("2m"),
            touchEvents: Int = 10,
            tripDuration: Duration = Duration.parse("10m")
        ) = SensorData(
            accelerometerStability = accelerometerStability,
            gyroscopeStability = gyroscopeStability,
            screenOnTime = screenOnTime,
            touchEvents = touchEvents,
            tripDuration = tripDuration
        )
    }
}
