package com.triptracker.service.activity.domain.model

/**
 * Represents the user's role in the vehicle (driver or passenger)
 * Used for insurance and risk assessment purposes
 */
enum class UserRole {
    DRIVER,
    PASSENGER,
    UNKNOWN;

    /**
     * Returns true if the role indicates the user is driving
     */
    val isDriving: Boolean
        get() = this == DRIVER

    /**
     * Returns true if the role indicates the user is a passenger
     */
    val isPassenger: Boolean
        get() = this == PASSENGER

    /**
     * Returns true if the role is uncertain/unknown
     */
    val isUncertain: Boolean
        get() = this == UNKNOWN

    /**
     * Confidence level for insurance calculations
     * Lower confidence = higher risk assessment
     */
    val confidenceMultiplier: Float
        get() = when (this) {
            DRIVER -> 1.0f      // High confidence in driver identification
            PASSENGER -> 0.7f   // Moderate confidence (passengers can still drive occasionally)
            UNKNOWN -> 0.5f     // Low confidence requires conservative risk assessment
        }

    companion object {
        /**
         * Safe default for when role detection fails
         */
        val DEFAULT = UNKNOWN
    }
}
