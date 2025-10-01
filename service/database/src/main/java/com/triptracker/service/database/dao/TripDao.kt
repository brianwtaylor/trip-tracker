package com.triptracker.service.database.dao

import androidx.room.*
import com.triptracker.service.database.entity.TripEntity
import com.triptracker.service.database.entity.TripWithLocations
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    
    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAllTrips(): Flow<List<TripEntity>>
    
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: String): TripEntity?
    
    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripWithLocations(tripId: String): TripWithLocations?
    
    @Transaction
    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAllTripsWithLocations(): Flow<List<TripWithLocations>>
    
    @Query("SELECT * FROM trips WHERE status = :status ORDER BY startTime DESC")
    fun getTripsByStatus(status: String): Flow<List<TripEntity>>
    
    @Query("SELECT * FROM trips WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    fun getTripsInDateRange(startTime: Long, endTime: Long): Flow<List<TripEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long
    
    @Update
    suspend fun updateTrip(trip: TripEntity)
    
    @Delete
    suspend fun deleteTrip(trip: TripEntity)
    
    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTripById(tripId: String)
    
    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()
    
    @Query("SELECT COUNT(*) FROM trips")
    suspend fun getTripCount(): Int
    
    @Query("SELECT SUM(distance) FROM trips WHERE status = 'COMPLETED'")
    suspend fun getTotalDistance(): Double?
}
