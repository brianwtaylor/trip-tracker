package com.triptracker.service.activity.domain.usecase

import com.triptracker.core.common.result.Result
import com.triptracker.core.domain.model.Trip
import com.triptracker.core.domain.model.TripStatus
import com.triptracker.core.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case that integrates trip tracking with activity recognition
 * Provides enhanced trip data with driver/passenger classification
 */
class IntegrateTripActivityUseCase @Inject constructor(
    private val manageActivityRecognitionUseCase: ManageActivityRecognitionUseCase
) {

    /**
     * Enhanced trip flow that includes activity recognition data
     * Combines trip status with user role classification
     */
    fun createEnhancedTripFlow(tripFlow: Flow<Trip>): Flow<Result<EnhancedTrip>> {
        val activityFlow = manageActivityRecognitionUseCase.getUserRoleFlow()

        return combine(tripFlow, activityFlow) { trip, activityResult ->
            when (activityResult) {
                is Result.Success -> {
                    val classification = activityResult.data
                    Result.Success(
                        EnhancedTrip(
                            trip = trip,
                            userRole = classification.role,
                            roleConfidence = classification.confidence,
                            roleReasoning = classification.reasoning,
                            sensorData = classification.sensorData
                        )
                    )
                }
                is Result.Error -> {
                    // Return trip without activity data if classification fails
                    Result.Success(
                        EnhancedTrip(
                            trip = trip,
                            userRole = UserRole.UNKNOWN,
                            roleConfidence = 0f,
                            roleReasoning = "Activity recognition unavailable: ${activityResult.exception.message}",
                            sensorData = null
                        )
                    )
                }
                Result.Loading -> {
                    Result.Success(
                        EnhancedTrip(
                            trip = trip,
                            userRole = UserRole.UNKNOWN,
                            roleConfidence = 0f,
                            roleReasoning = "Loading activity data...",
                            sensorData = null
                        )
                    )
                }
            }
        }
    }

    /**
     * Determine if a trip should be recorded based on activity recognition
     * Useful for filtering out passenger trips if desired
     */
    fun shouldRecordTrip(trip: Trip, userRole: UserRole): Boolean {
        return when {
            // Always record active trips
            trip.status == TripStatus.ACTIVE -> true

            // For completed trips, record based on settings and role
            trip.status == TripStatus.COMPLETED -> {
                // Could add user preference here: e.g., only record driver trips
                // For now, record all completed trips
                true
            }

            else -> false
        }
    }

    /**
     * Get insurance-relevant metrics from trip + activity data
     */
    fun calculateInsuranceMetrics(enhancedTrip: EnhancedTrip): InsuranceMetrics {
        val baseRiskScore = calculateBaseRiskScore(enhancedTrip.trip)
        val roleAdjustment = enhancedTrip.userRole.confidenceMultiplier
        val adjustedRiskScore = baseRiskScore * roleAdjustment

        return InsuranceMetrics(
            baseRiskScore = baseRiskScore,
            roleAdjustment = roleAdjustment,
            adjustedRiskScore = adjustedRiskScore,
            recommendedPremium = calculateRecommendedPremium(adjustedRiskScore),
            confidenceLevel = enhancedTrip.roleConfidence
        )
    }

    /**
     * Calculate base risk score from trip data
     */
    private fun calculateBaseRiskScore(trip: Trip): Float {
        // Simplified risk calculation based on trip characteristics
        var riskScore = 1.0f

        // Distance factor (longer trips = slightly higher risk)
        val distanceKm = trip.distance.toFloat()
        riskScore *= (1.0f + distanceKm / 1000f).coerceAtMost(2.0f)

        // Speed factor (higher average speed = higher risk)
        val avgSpeed = trip.averageSpeed.toFloat()
        if (avgSpeed > 80f) {
            riskScore *= 1.2f
        }

        // Duration factor (longer trips at night = higher risk)
        val durationHours = trip.getDuration().toFloat() / 3600000f
        val isNightTrip = trip.startTime?.let { startTime ->
            val hour = java.util.Date(startTime).hours
            hour < 6 || hour > 22
        } ?: false

        if (isNightTrip && durationHours > 2f) {
            riskScore *= 1.3f
        }

        return riskScore.coerceIn(0.5f, 3.0f)
    }

    /**
     * Calculate recommended premium adjustment
     */
    private fun calculateRecommendedPremium(riskScore: Float): Float {
        // Base premium adjustment (1.0 = no change, >1 = increase, <1 = decrease)
        return when {
            riskScore < 1.0f -> 0.9f  // Low risk = 10% discount
            riskScore < 1.5f -> 1.0f  // Normal risk = no change
            riskScore < 2.0f -> 1.2f  // Medium risk = 20% increase
            else -> 1.5f              // High risk = 50% increase
        }
    }
}

/**
 * Enhanced trip data that includes activity recognition results
 */
data class EnhancedTrip(
    val trip: Trip,
    val userRole: UserRole,
    val roleConfidence: Float,
    val roleReasoning: String,
    val sensorData: com.triptracker.service.activity.domain.model.SensorData?
)

/**
 * Insurance-relevant metrics calculated from trip + activity data
 */
data class InsuranceMetrics(
    val baseRiskScore: Float,
    val roleAdjustment: Float,
    val adjustedRiskScore: Float,
    val recommendedPremium: Float,
    val confidenceLevel: Float
)
