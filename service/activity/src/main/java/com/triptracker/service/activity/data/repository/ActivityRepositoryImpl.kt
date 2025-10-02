package com.triptracker.service.activity.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.util.Log
import com.triptracker.core.common.result.Result
import com.triptracker.service.activity.domain.model.SensorData
import com.triptracker.core.domain.model.UserRole
import com.triptracker.service.activity.domain.repository.ActivityRepository
import com.triptracker.service.activity.domain.usecase.DetectUserRoleUseCase
import com.triptracker.service.activity.domain.usecase.UserRoleClassification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of ActivityRepository that collects sensor data
 * and provides activity recognition capabilities for Level 1 classification
 */
class ActivityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val detectUserRoleUseCase: DetectUserRoleUseCase
) : ActivityRepository, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    // Sensor data collection
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Data collection state
    private var isCollecting = false
    private val collectionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Current sensor readings
    private val currentAccelerometer = AtomicReference<FloatArray>()
    private val currentGyroscope = AtomicReference<FloatArray>()

    // Historical data for stability calculation
    private val accelerometerHistory = mutableListOf<FloatArray>()
    private val gyroscopeHistory = mutableListOf<FloatArray>()
    private val maxHistorySize = 50  // Keep last 50 readings (~2.5 seconds at 20Hz)

    // Activity tracking
    private var screenOnTime = Duration.ZERO
    private var touchEvents = 0
    private var appLaunches = 0
    private var collectionStartTime = 0L

    // Latest classification result
    private val lastClassification = AtomicReference<UserRoleClassification>()

    // Flow for sensor data updates
    private val _sensorDataFlow = MutableSharedFlow<Result<SensorData>>()

    override fun startActivityRecognition(): Flow<Result<SensorData>> {
        if (isCollecting) {
            return _sensorDataFlow.asSharedFlow()
        }

        isCollecting = true
        collectionStartTime = System.currentTimeMillis()

        // Reset activity tracking
        resetActivityData()

        // Register sensors
        registerSensors()

        // Start periodic data emission
        startDataCollection()

        Log.d(TAG, "Activity recognition started")
        return _sensorDataFlow.asSharedFlow()
    }

    override fun stopActivityRecognition() {
        if (!isCollecting) return

        isCollecting = false

        // Unregister sensors
        sensorManager.unregisterListener(this)

        // Clear historical data
        accelerometerHistory.clear()
        gyroscopeHistory.clear()

        // Cancel collection job
        collectionScope.coroutineContext.cancelChildren()

        Log.d(TAG, "Activity recognition stopped")
    }

    override suspend fun getCurrentSensorData(): Result<SensorData> {
        return try {
            val data = collectCurrentSensorData()
            Result.Success(data)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current sensor data", e)
            Result.Error(e)
        }
    }

    override fun isActivityRecognitionActive(): Boolean = isCollecting

    override suspend fun getLastUserRole(): Result<UserRole> {
        val classification = lastClassification.get()
        return if (classification != null) {
            Result.Success(classification.role)
        } else {
            Result.Success(UserRole.UNKNOWN) // Default when no data available
        }
    }

    override fun clearSensorData() {
        accelerometerHistory.clear()
        gyroscopeHistory.clear()
        lastClassification.set(null)
        resetActivityData()
    }

    // SensorEventListener implementation
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                currentAccelerometer.set(event.values.clone())
                addToHistory(accelerometerHistory, event.values)
            }
            Sensor.TYPE_GYROSCOPE -> {
                currentGyroscope.set(event.values.clone())
                addToHistory(gyroscopeHistory, event.values)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: ${sensor.name}, accuracy: $accuracy")
    }

    /**
     * Register sensors for data collection
     */
    private fun registerSensors() {
        accelerometerSensor?.let { sensor ->
            val registered = sensorManager.registerListener(
                this, sensor, SensorManager.SENSOR_DELAY_NORMAL  // ~20Hz
            )
            if (!registered) {
                Log.w(TAG, "Failed to register accelerometer sensor")
            }
        }

        gyroscopeSensor?.let { sensor ->
            val registered = sensorManager.registerListener(
                this, sensor, SensorManager.SENSOR_DELAY_NORMAL  // ~20Hz
            )
            if (!registered) {
                Log.w(TAG, "Failed to register gyroscope sensor")
            }
        }
    }

    /**
     * Start periodic emission of sensor data
     */
    private fun startDataCollection() {
        collectionScope.launch {
            while (isCollecting && isActive) {
                try {
                    val sensorData = collectCurrentSensorData()
                    _sensorDataFlow.emit(Result.Success(sensorData))

                    // Update classification periodically
                    updateUserRoleClassification(sensorData)

                } catch (e: Exception) {
                    Log.e(TAG, "Error collecting sensor data", e)
                    _sensorDataFlow.emit(Result.Error(e))
                }

                delay(DATA_COLLECTION_INTERVAL)
            }
        }
    }

    /**
     * Collect current sensor data into SensorData object
     */
    private fun collectCurrentSensorData(): SensorData {
        val accelerometer = currentAccelerometer.get()
        val gyroscope = currentGyroscope.get()

        val accelVariance = calculateVariance(accelerometerHistory)
        val accelStability = calculateStability(accelerometerHistory)

        val gyroVariance = calculateVariance(gyroscopeHistory)
        val gyroStability = calculateStability(gyroscopeHistory)

        val tripDuration = (System.currentTimeMillis() - collectionStartTime).milliseconds

        return SensorData(
            timestamp = System.currentTimeMillis(),
            accelerometerVariance = accelVariance,
            accelerometerStability = accelStability,
            gyroscopeVariance = gyroVariance,
            gyroscopeStability = gyroStability,
            motionCorrelation = calculateMotionCorrelation(),
            screenOnTime = screenOnTime,
            touchEvents = touchEvents,
            appLaunches = appLaunches,
            tripDuration = tripDuration,
            timeOfDay = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
            isCharging = isDeviceCharging(),
            batteryLevel = getBatteryLevel(),
            isPowerSaving = isPowerSaveMode()
        )
    }

    /**
     * Calculate variance of sensor readings (measure of movement)
     */
    private fun calculateVariance(history: List<FloatArray>): Float {
        if (history.size < 2) return 0f

        val means = FloatArray(3) { 0f }
        val variances = FloatArray(3) { 0f }

        // Calculate means
        history.forEach { reading ->
            for (i in 0..2) {
                means[i] += reading[i]
            }
        }
        means.forEachIndexed { i, mean -> means[i] = mean / history.size }

        // Calculate variances
        history.forEach { reading ->
            for (i in 0..2) {
                variances[i] += (reading[i] - means[i]).pow(2)
            }
        }
        variances.forEachIndexed { i, variance -> variances[i] = variance / history.size }

        // Return average variance across axes
        return variances.average().toFloat()
    }

    /**
     * Calculate stability (inverse of variance, normalized)
     */
    private fun calculateStability(history: List<FloatArray>): Float {
        val variance = calculateVariance(history)
        // Convert variance to stability score (0-1, higher = more stable)
        return (1f / (1f + variance)).coerceIn(0f, 1f)
    }

    /**
     * Calculate correlation between phone motion and expected vehicle motion
     * This is a simplified implementation for Level 1
     */
    private fun calculateMotionCorrelation(): Float {
        // Simple heuristic: stable phones correlate with vehicle motion
        val accelStability = calculateStability(accelerometerHistory)
        val gyroStability = calculateStability(gyroscopeHistory)
        return (accelStability + gyroStability) / 2f
    }

    /**
     * Add reading to historical data, maintaining max size
     */
    private fun addToHistory(history: MutableList<FloatArray>, values: FloatArray) {
        history.add(values.clone())
        if (history.size > maxHistorySize) {
            history.removeAt(0)
        }
    }

    /**
     * Update user role classification using the heuristic algorithm
     */
    private suspend fun updateUserRoleClassification(sensorData: SensorData) {
        val result = detectUserRoleUseCase.execute(sensorData)
        when (result) {
            is Result.Success -> {
                val classification = result.data
                lastClassification.set(classification)
                Log.d(TAG, "User role classified: ${classification.role} " +
                      "(confidence: ${"%.2f".format(classification.confidence)})")
            }
            is Result.Error -> {
                Log.e(TAG, "Failed to classify user role", result.exception)
            }
            Result.Loading -> {
                // Do nothing for loading state
            }
        }
    }

    /**
     * Update activity data (called by external components)
     */
    fun updateActivityData(screenOnTime: Duration, touchEvents: Int, appLaunches: Int) {
        this.screenOnTime = screenOnTime
        this.touchEvents = touchEvents
        this.appLaunches = appLaunches
    }

    /**
     * Check if device is charging
     */
    private fun isDeviceCharging(): Boolean {
        // This would need to be implemented with BatteryManager
        // For now, return false as default
        return false
    }

    /**
     * Get current battery level
     */
    private fun getBatteryLevel(): Int {
        // This would need to be implemented with BatteryManager
        // For now, return 100 as default
        return 100
    }

    /**
     * Check if device is in power save mode
     */
    private fun isPowerSaveMode(): Boolean {
        return powerManager.isPowerSaveMode
    }

    /**
     * Reset activity tracking data
     */
    private fun resetActivityData() {
        screenOnTime = Duration.ZERO
        touchEvents = 0
        appLaunches = 0
        collectionStartTime = System.currentTimeMillis()
    }

    companion object {
        private const val TAG = "ActivityRepository"
        private val DATA_COLLECTION_INTERVAL = 5.seconds  // Emit data every 5 seconds
    }
}
