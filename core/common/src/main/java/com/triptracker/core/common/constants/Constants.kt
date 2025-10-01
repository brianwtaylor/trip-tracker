package com.triptracker.core.common.constants

object Constants {
    // Location tracking
    const val LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
    const val LOCATION_FASTEST_INTERVAL = 3000L // 3 seconds
    const val MIN_DISTANCE_METERS = 10.0
    const val MIN_ACCURACY_METERS = 50f
    
    // Trip detection
    const val TRIP_END_TIMEOUT = 300000L // 5 minutes in milliseconds
    const val ACTIVITY_DETECTION_INTERVAL = 10000L // 10 seconds
    const val ACTIVITY_CONFIDENCE_THRESHOLD = 75
    
    // Database
    const val DATABASE_NAME = "trip_database"
    const val DATABASE_VERSION = 1
    
    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "trip_tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Trip Tracking"
    const val NOTIFICATION_ID = 1001
    
    // Permissions
    const val PERMISSION_REQUEST_CODE = 100
    const val BACKGROUND_LOCATION_REQUEST_CODE = 101
    const val ACTIVITY_RECOGNITION_REQUEST_CODE = 102
    
    // Speed conversions
    const val METERS_PER_SECOND_TO_MPH = 2.23694
    const val METERS_PER_SECOND_TO_KMH = 3.6
    const val METERS_TO_MILES = 0.000621371
    const val METERS_TO_KILOMETERS = 0.001
}
