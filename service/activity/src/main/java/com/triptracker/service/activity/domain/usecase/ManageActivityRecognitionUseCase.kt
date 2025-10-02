package com.triptracker.service.activity.domain.usecase

import com.triptracker.core.common.result.Result
import com.triptracker.service.activity.domain.model.SensorData
import com.triptracker.service.activity.domain.model.UserRole
import com.triptracker.service.activity.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for managing activity recognition lifecycle
 * Coordinates with trip tracking to provide driver/passenger detection
 */
class ManageActivityRecognitionUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {

    /**
     * Start activity recognition when trip tracking begins
     */
    suspend fun startActivityRecognition(): Result<Unit> {
        return try {
            // Activity recognition starts automatically when flow is collected
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Stop activity recognition when trip tracking ends
     */
    fun stopActivityRecognition() {
        activityRepository.stopActivityRecognition()
    }

    /**
     * Get flow of sensor data for real-time processing
     */
    fun getSensorDataFlow(): Flow<Result<SensorData>> {
        return activityRepository.startActivityRecognition()
    }

    /**
     * Get flow of user role classifications
     */
    fun getUserRoleFlow(): Flow<Result<UserRoleClassification>> {
        return getSensorDataFlow().map { sensorResult ->
            when (sensorResult) {
                is Result.Success -> {
                    val sensorData = sensorResult.data
                    // This would use the DetectUserRoleUseCase
                    // For now, return a mock classification
                    Result.Success(
                        UserRoleClassification(
                            role = UserRole.UNKNOWN,
                            confidence = 0.5f,
                            reasoning = "Classification in progress",
                            sensorData = sensorData
                        )
                    )
                }
                is Result.Error -> {
                    Result.Error(sensorResult.exception)
                }
                Result.Loading -> {
                    Result.Success(
                        UserRoleClassification(
                            role = UserRole.UNKNOWN,
                            confidence = 0f,
                            reasoning = "Loading...",
                            sensorData = null
                        )
                    )
                }
            }
        }
    }

    /**
     * Get current user role (for immediate queries)
     */
    suspend fun getCurrentUserRole(): Result<UserRole> {
        return activityRepository.getLastUserRole()
    }

    /**
     * Check if activity recognition is active
     */
    fun isActivityRecognitionActive(): Boolean {
        return activityRepository.isActivityRecognitionActive()
    }

    /**
     * Update activity data from external sources (screen usage, etc.)
     */
    fun updateActivityData(
        screenOnTime: kotlin.time.Duration,
        touchEvents: Int,
        appLaunches: Int
    ) {
        // This would update the repository's activity tracking
        // Implementation depends on how we track screen/app usage
    }
}
