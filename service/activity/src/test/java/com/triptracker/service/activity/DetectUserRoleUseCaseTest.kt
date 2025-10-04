package com.triptracker.service.activity

import com.triptracker.service.activity.domain.model.SensorData
import com.triptracker.core.domain.model.UserRole
import com.triptracker.service.activity.domain.usecase.DetectUserRoleUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Unit tests for Level 1 driver/passenger detection
 * Validates the heuristic-based classification accuracy
 */
class DetectUserRoleUseCaseTest {

    private lateinit var useCase: DetectUserRoleUseCase

    @Before
    fun setup() {
        useCase = DetectUserRoleUseCase()
    }

    @Test
    fun `detects driver with stable phone and low usage`() = runTest {
        // Given: Classic driver scenario
        val driverData = SensorData.createForTesting(
            accelerometerStability = 0.9f,  // Very stable (mounted phone)
            gyroscopeStability = 0.85f,     // Very stable orientation
            screenOnTime = 2.minutes,       // Minimal screen usage
            touchEvents = 5,                // Few interactions
            tripDuration = 15.minutes
        )

        // When: Classify role
        val result = useCase.execute(driverData)

        // Then: Should detect driver with high confidence
        assertTrue("Classification should succeed", result.isSuccess)
        val classification = result.getOrThrow()

        assertEquals("Should detect DRIVER", UserRole.DRIVER, classification.role)
        assertTrue("Confidence should be high", classification.confidence > 0.7f)
        assertTrue("Reasoning should mention stability", classification.reasoning.contains("stable"))
    }

    @Test
    fun `detects passenger with unstable phone and high usage`() = runTest {
        // Given: Classic passenger scenario
        val passengerData = SensorData.createForTesting(
            accelerometerStability = 0.3f,  // Unstable (holding phone)
            gyroscopeStability = 0.2f,      // Unstable orientation
            screenOnTime = 12.minutes,      // High screen usage
            touchEvents = 45,               // Many interactions
            tripDuration = 15.minutes
        )

        // When: Classify role
        val result = useCase.execute(passengerData)

        // Then: Should detect passenger with high confidence
        assertTrue("Classification should succeed", result.isSuccess)
        val classification = result.getOrThrow()

        assertEquals("Should detect PASSENGER", UserRole.PASSENGER, classification.role)
        assertTrue("Confidence should be high", classification.confidence > 0.7f)
        assertTrue("Reasoning should mention usage", classification.reasoning.contains("usage"))
    }

    @Test
    fun `returns unknown for ambiguous data`() = runTest {
        // Given: Mixed signals (edge case)
        val ambiguousData = SensorData.createForTesting(
            accelerometerStability = 0.6f,  // Moderately stable
            gyroscopeStability = 0.55f,     // Moderately stable
            screenOnTime = 7.minutes,       // Moderate usage
            touchEvents = 20,               // Moderate interactions
            tripDuration = 15.minutes
        )

        // When: Classify role
        val result = useCase.execute(ambiguousData)

        // Then: Should return unknown with lower confidence
        assertTrue("Classification should succeed", result.isSuccess)
        val classification = result.getOrThrow()

        assertEquals("Should return UNKNOWN for ambiguous data", UserRole.UNKNOWN, classification.role)
        assertTrue("Confidence should be lower", classification.confidence < 0.7f)
    }

    @Test
    fun `handles empty sensor data gracefully`() = runTest {
        // Given: Empty/default sensor data
        val emptyData = SensorData.empty()

        // When: Classify role
        val result = useCase.execute(emptyData)

        // Then: Should return unknown but not crash
        assertTrue("Classification should succeed even with empty data", result.isSuccess)
        val classification = result.getOrThrow()

        // Should default to unknown for insufficient data
        assertEquals("Should default to UNKNOWN", UserRole.UNKNOWN, classification.role)
    }

    @Test
    fun `confidence reflects classification strength`() = runTest {
        val testCases = listOf(
            // Strong driver case
            Triple(
                SensorData.createForTesting(0.95f, 0.9f, 1.minutes, 2, 10.minutes),
                UserRole.DRIVER,
                0.8f // High confidence expected
            ),
            // Strong passenger case
            Triple(
                SensorData.createForTesting(0.1f, 0.15f, 15.minutes, 60, 10.minutes),
                UserRole.PASSENGER,
                0.8f // High confidence expected
            ),
            // Weak case
            Triple(
                SensorData.createForTesting(0.5f, 0.5f, 8.minutes, 25, 10.minutes),
                UserRole.UNKNOWN,
                0.6f // Lower confidence expected
            )
        )

        testCases.forEach { (data, expectedRole, minConfidence) ->
            val result = useCase.execute(data)
            assertTrue("Classification should succeed", result.isSuccess)

            val classification = result.getOrThrow()
            assertEquals("Role should match expected", expectedRole, classification.role)
            assertTrue("Confidence should meet minimum threshold",
                classification.confidence >= minConfidence)
        }
    }

    /**
     * Integration test demonstrating Level 1 accuracy expectations
     * Based on research showing 70-80% accuracy for heuristic approaches
     */
    @Test
    fun `demonstrates expected Level 1 accuracy range`() = runTest {
        // Test cases representing typical real-world scenarios
        val testScenarios = listOf(
            // Clear driver cases (should be 90%+ accurate)
            SensorData.createForTesting(0.9f, 0.85f, 3.minutes, 8, 20.minutes) to UserRole.DRIVER,
            SensorData.createForTesting(0.95f, 0.9f, 1.minutes, 3, 15.minutes) to UserRole.DRIVER,

            // Clear passenger cases (should be 85%+ accurate)
            SensorData.createForTesting(0.2f, 0.25f, 14.minutes, 55, 20.minutes) to UserRole.PASSENGER,
            SensorData.createForTesting(0.15f, 0.1f, 16.minutes, 70, 15.minutes) to UserRole.PASSENGER,

            // Ambiguous cases (may be 50-70% accurate - this is expected)
            SensorData.createForTesting(0.6f, 0.55f, 8.minutes, 28, 20.minutes) to UserRole.UNKNOWN,
            SensorData.createForTesting(0.45f, 0.5f, 6.minutes, 18, 15.minutes) to UserRole.UNKNOWN
        )

        var correctClassifications = 0
        val totalTests = testScenarios.size

        testScenarios.forEach { (data, expectedRole) ->
            val result = useCase.execute(data)
            if (result.isSuccess) {
                val classification = result.getOrThrow()
                if (classification.role == expectedRole) {
                    correctClassifications++
                }
            }
        }

        val accuracy = correctClassifications.toFloat() / totalTests.toFloat()

        // Level 1 should achieve 70-80% accuracy on typical scenarios
        // This test validates our implementation meets these expectations
        assertTrue("Level 1 accuracy should be >= 70%", accuracy >= 0.7f)
        println("Level 1 Test Accuracy: ${(accuracy * 100).toInt()}% ($correctClassifications/$totalTests correct)")

        // This demonstrates the research-backed 70-80% accuracy range
        // Real-world accuracy may vary based on phone mounting, user behavior, etc.
    }
}
