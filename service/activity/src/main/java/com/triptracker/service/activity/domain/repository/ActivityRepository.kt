package com.triptracker.service.activity.domain.repository

import com.triptracker.core.common.result.Result
import com.triptracker.service.activity.domain.model.SensorData
import com.triptracker.core.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for activity recognition and sensor data
 */
interface ActivityRepository {

    /**
     * Start collecting sensor data for activity recognition
     */
    fun startActivityRecognition(): Flow<Result<SensorData>>

    /**
     * Stop collecting sensor data
     */
    fun stopActivityRecognition()

    /**
     * Get the most recent sensor data
     */
    suspend fun getCurrentSensorData(): Result<SensorData>

    /**
     * Check if activity recognition is currently active
     */
    fun isActivityRecognitionActive(): Boolean

    /**
     * Get the most recent user role classification
     */
    suspend fun getLastUserRole(): Result<UserRole>

    /**
     * Clear any cached sensor data
     */
    fun clearSensorData()
}
