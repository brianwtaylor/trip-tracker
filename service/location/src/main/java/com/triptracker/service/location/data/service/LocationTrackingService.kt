package com.triptracker.service.location.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.triptracker.core.common.constants.Constants
import com.triptracker.core.common.result.Result
import com.triptracker.service.activity.domain.usecase.ManageActivityRecognitionUseCase
import com.triptracker.service.location.domain.model.LocationAccuracy
import com.triptracker.service.location.domain.model.LocationUpdate
import com.triptracker.service.location.domain.model.TripState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Enterprise-grade Foreground Service for scalable trip tracking
 *
 * Key Features:
 * - Battery-optimized GPS tracking with adaptive accuracy
 * - Comprehensive service lifecycle management
 * - Real-time trip state monitoring
 * - Scalable notification system
 * - Error recovery and resilience
 */
@AndroidEntryPoint
class LocationTrackingService : LifecycleService() {

    @Inject
    lateinit var locationClient: LocationClient

    @Inject
    lateinit var activityRecognitionUseCase: ManageActivityRecognitionUseCase

    // Service state management
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationJob: Job? = null
    private var currentAccuracy: LocationAccuracy = LocationAccuracy.BALANCED
    private var currentTripState: TripState? = null

    // Performance monitoring
    private var serviceStartTime: Long = 0
    private var locationUpdateCount = 0
    private var lastLocationUpdateTime = 0L

    // Battery and system monitoring
    private lateinit var batteryManager: BatteryManager

    override fun onCreate() {
        super.onCreate()
        initializeService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        serviceStartTime = System.currentTimeMillis()

        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val accuracy = intent.getStringExtra(EXTRA_ACCURACY)?.let {
                    LocationAccuracy.valueOf(it)
                } ?: LocationAccuracy.BALANCED

                val tripState = intent.getParcelableExtra(EXTRA_TRIP_STATE, TripState::class.java)
                startLocationTracking(accuracy, tripState)
            }

            ACTION_UPDATE_ACCURACY -> {
                val accuracy = intent.getStringExtra(EXTRA_ACCURACY)?.let {
                    LocationAccuracy.valueOf(it)
                } ?: LocationAccuracy.BALANCED

                updateTrackingAccuracy(accuracy)
            }

            ACTION_UPDATE_TRIP_STATE -> {
                val tripState = intent.getParcelableExtra(EXTRA_TRIP_STATE, TripState::class.java)
                updateTripState(tripState)
            }

            ACTION_STOP_TRACKING -> {
                stopLocationTracking()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopLocationTracking()
    }

    override fun onBind(intent: Intent): IBinder? = null

    /**
     * Initialize service components
     */
    private fun initializeService() {
        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        createNotificationChannels()
    }

    /**
     * Start foreground location tracking
     */
    private fun startLocationTracking(accuracy: LocationAccuracy, tripState: TripState? = null) {
        if (isTracking()) return

        currentAccuracy = accuracy
        currentTripState = tripState

        // Start foreground service with notification
        startForegroundService()

        // Start location updates
        startLocationUpdates()

        // Start activity recognition for driver/passenger detection
        startActivityRecognition()

        // Broadcast service started
        broadcastServiceState(true)
    }

    /**
     * Start the foreground service with notification
     */
    private fun startForegroundService() {
        val notification = createTrackingNotification()
        val notificationId = Constants.NOTIFICATION_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(notificationId, notification)
        }
    }

    /**
     * Start location updates with adaptive accuracy
     */
    private fun startLocationUpdates() {
        locationJob = serviceScope.launch {
            // Use adaptive accuracy based on current context
            val adaptiveAccuracy = determineAdaptiveAccuracy()

            locationClient.startLocationUpdates(adaptiveAccuracy)
                .onEach { result -> handleLocationResult(result) }
                .catch { error -> handleLocationError(error) }
                .collect()
        }
    }

    /**
     * Start activity recognition for driver/passenger detection
     */
    private fun startActivityRecognition() {
        serviceScope.launch {
            try {
                activityRecognitionUseCase.startActivityRecognition()
                // Activity recognition flow will run in background
                // Results are available through the use case
            } catch (e: Exception) {
                // Log error but don't stop location tracking
                // Activity recognition is enhancement, not core functionality
                e.printStackTrace()
            }
        }
    }

    /**
     * Determine optimal accuracy based on context
     */
    private fun determineAdaptiveAccuracy(): LocationAccuracy {
        // Start with current accuracy setting
        var accuracy = currentAccuracy

        // Adjust for battery level
        val batteryLevel = getBatteryLevel()
        if (batteryLevel < 20) {
            accuracy = LocationAccuracy.LOW_POWER
        }

        // Adjust for trip state
        currentTripState?.let { tripState ->
            // High accuracy for first 2 minutes of trip
            if (tripState.durationMs < 120_000) {
                accuracy = LocationAccuracy.HIGH_ACCURACY
            }

            // Adjust based on speed
            accuracy = when {
                tripState.currentSpeed < 5 -> LocationAccuracy.HIGH_ACCURACY // Stopped/traffic
                tripState.currentSpeed < 30 -> LocationAccuracy.BALANCED     // City driving
                else -> LocationAccuracy.LOW_POWER                          // Highway
            }
        }

        return accuracy
    }

    /**
     * Handle location update results
     */
    private fun handleLocationResult(result: Result<LocationUpdate>) {
        when (result) {
            is Result.Success -> {
                locationUpdateCount++
                lastLocationUpdateTime = System.currentTimeMillis()

                val update = result.data

                // Update notification with current status
                updateNotification()

                // Broadcast location update (for UI updates)
                broadcastLocationUpdate(update)

                // Adapt accuracy if needed
                adaptAccuracyBasedOnConditions(update)
            }
            is Result.Error -> {
                handleLocationError(result.exception)
            }
            Result.Loading -> {
                // Initial loading state
            }
        }
    }

    /**
     * Adapt accuracy based on current conditions
     */
    private fun adaptAccuracyBasedOnConditions(update: LocationUpdate) {
        val newAccuracy = determineAdaptiveAccuracy()

        if (newAccuracy != currentAccuracy) {
            serviceScope.launch {
                locationClient.updateAccuracy(newAccuracy)
                currentAccuracy = newAccuracy
                updateNotification()
            }
        }
    }

    /**
     * Handle location errors with recovery strategies
     */
    private fun handleLocationError(error: Throwable) {
        // Log error for debugging
        error.printStackTrace()

        // Update notification to show error state
        updateNotificationWithError()

        // Implement recovery strategies
        when (error) {
            is SecurityException -> {
                // Permission revoked - notify user and stop
                broadcastPermissionError()
                stopLocationTracking()
            }
            else -> {
                // Network/GPS issues - could retry with different accuracy
                if (currentAccuracy != LocationAccuracy.LOW_POWER) {
                    // Try lower accuracy as fallback
                    updateTrackingAccuracy(LocationAccuracy.LOW_POWER)
                }
            }
        }
    }

    /**
     * Update tracking accuracy dynamically
     */
    private fun updateTrackingAccuracy(newAccuracy: LocationAccuracy) {
        if (currentAccuracy == newAccuracy) return

        serviceScope.launch {
            locationClient.updateAccuracy(newAccuracy)
            currentAccuracy = newAccuracy
            updateNotification()
        }
    }

    /**
     * Update trip state for adaptive behavior
     */
    private fun updateTripState(tripState: TripState?) {
        currentTripState = tripState

        // Re-evaluate accuracy with new trip state
        if (isTracking()) {
            val newAccuracy = determineAdaptiveAccuracy()
            updateTrackingAccuracy(newAccuracy)
        }
    }

    /**
     * Stop location tracking
     */
    private fun stopLocationTracking() {
        if (!isTracking()) return

        // Cancel location job
        locationJob?.cancel()
        locationJob = null

        // Stop activity recognition
        stopActivityRecognition()

        // Stop foreground service
        stopForegroundService()

        // Reset state
        currentAccuracy = LocationAccuracy.BALANCED
        currentTripState = null

        // Broadcast service stopped
        broadcastServiceState(false)
    }

    /**
     * Stop activity recognition
     */
    private fun stopActivityRecognition() {
        activityRecognitionUseCase.stopActivityRecognition()
    }

    /**
     * Stop foreground service properly
     */
    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    /**
     * Create comprehensive tracking notification
     */
    private fun createTrackingNotification(): Notification {
        val title = when (currentTripState) {
            null -> "Trip Tracking Active"
            else -> "Trip in Progress"
        }

        val accuracyText = when (currentAccuracy) {
            LocationAccuracy.HIGH_ACCURACY -> "High accuracy"
            LocationAccuracy.BALANCED -> "Balanced"
            LocationAccuracy.LOW_POWER -> "Battery saving"
            LocationAccuracy.PASSIVE -> "Passive"
        }

        val statusText = buildString {
            append("GPS tracking • $accuracyText")
            currentTripState?.let { state ->
                val duration = (System.currentTimeMillis() - state.startTime) / 1000
                append(" • ${formatDuration(duration)}")
            }
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_TRACKING)
            .setContentTitle(title)
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(createAppIntent())
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Tracking",
                createStopIntent()
            )
            .setWhen(serviceStartTime)
            .setShowWhen(true)
            .build()
    }

    /**
     * Update notification with current status
     */
    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Constants.NOTIFICATION_ID, createTrackingNotification())
    }

    /**
     * Show error state in notification
     */
    private fun updateNotificationWithError() {
        val errorNotification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_TRACKING)
            .setContentTitle("Trip Tracking")
            .setContentText("Location tracking unavailable")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Constants.NOTIFICATION_ID, errorNotification)
    }

    /**
     * Create pending intent to open app
     */
    private fun createAppIntent(): PendingIntent {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        } ?: Intent()

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getActivity(this, 0, intent, flags)
    }

    /**
     * Create pending intent to stop tracking
     */
    private fun createStopIntent(): PendingIntent {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getService(this, 1, intent, flags)
    }

    /**
     * Create notification channels
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackingChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_TRACKING,
                "Trip Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows trip tracking status and allows control"
                setShowBadge(false)
                enableVibration(false)
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(trackingChannel)
        }
    }

    /**
     * Broadcast service state changes
     */
    private fun broadcastServiceState(isActive: Boolean) {
        val intent = Intent(ACTION_SERVICE_STATE_CHANGED).apply {
            putExtra(EXTRA_SERVICE_ACTIVE, isActive)
            putExtra(EXTRA_ACCURACY, currentAccuracy.name)
        }
        sendBroadcast(intent)
    }

    /**
     * Broadcast location updates
     */
    private fun broadcastLocationUpdate(update: LocationUpdate) {
        val intent = Intent(ACTION_LOCATION_UPDATE).apply {
            putExtra(EXTRA_LOCATION_UPDATE, update)
        }
        sendBroadcast(intent)
    }

    /**
     * Broadcast permission error
     */
    private fun broadcastPermissionError() {
        val intent = Intent(ACTION_PERMISSION_ERROR)
        sendBroadcast(intent)
    }

    /**
     * Get current battery level
     */
    private fun getBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    /**
     * Check if service is currently tracking
     */
    private fun isTracking(): Boolean = locationJob?.isActive == true

    /**
     * Format duration for display
     */
    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            hours > 0 -> String.format("%dh %dm", hours, minutes)
            minutes > 0 -> String.format("%dm %ds", minutes, secs)
            else -> String.format("%ds", secs)
        }
    }

    companion object {
        // Service actions
        const val ACTION_START_TRACKING = "com.triptracker.service.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.triptracker.service.STOP_TRACKING"
        const val ACTION_UPDATE_ACCURACY = "com.triptracker.service.UPDATE_ACCURACY"
        const val ACTION_UPDATE_TRIP_STATE = "com.triptracker.service.UPDATE_TRIP_STATE"

        // Broadcast actions
        const val ACTION_SERVICE_STATE_CHANGED = "com.triptracker.service.STATE_CHANGED"
        const val ACTION_LOCATION_UPDATE = "com.triptracker.service.LOCATION_UPDATE"
        const val ACTION_PERMISSION_ERROR = "com.triptracker.service.PERMISSION_ERROR"

        // Intent extras
        const val EXTRA_ACCURACY = "accuracy"
        const val EXTRA_TRIP_STATE = "trip_state"
        const val EXTRA_SERVICE_ACTIVE = "service_active"
        const val EXTRA_LOCATION_UPDATE = "location_update"

        // Notification channels
        const val NOTIFICATION_CHANNEL_TRACKING = "trip_tracking_channel"
    }
}
