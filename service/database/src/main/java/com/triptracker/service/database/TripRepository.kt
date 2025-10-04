package com.triptracker.service.database

import com.triptracker.service.database.entity.TripWithLocations
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for trip data operations
 * Provides a clean API for accessing trip data from the database
 */
interface TripRepository {

    /**
     * Get all trips with their associated locations
     */
    fun getAllTrips(): Flow<List<TripWithLocations>>

    /**
     * Get a specific trip by ID with locations
     */
    suspend fun getTripById(tripId: String): TripWithLocations?

    /**
     * Insert or update a trip
     */
    suspend fun saveTrip(trip: TripWithLocations)

    /**
     * Delete a trip by ID
     */
    suspend fun deleteTrip(tripId: String)

    /**
     * Delete all trips
     */
    suspend fun deleteAllTrips()

    /**
     * Get the total count of trips
     */
    suspend fun getTripCount(): Int
}
