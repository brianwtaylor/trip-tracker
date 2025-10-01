package com.triptracker.service.database.dao

import androidx.room.*
import com.triptracker.service.database.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    
    @Query("SELECT * FROM locations WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getLocationsForTrip(tripId: String): Flow<List<LocationEntity>>
    
    @Query("SELECT * FROM locations WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getLocationsForTripSync(tripId: String): List<LocationEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)
    
    @Delete
    suspend fun deleteLocation(location: LocationEntity)
    
    @Query("DELETE FROM locations WHERE tripId = :tripId")
    suspend fun deleteLocationsForTrip(tripId: String)
    
    @Query("DELETE FROM locations")
    suspend fun deleteAllLocations()
    
    @Query("SELECT COUNT(*) FROM locations WHERE tripId = :tripId")
    suspend fun getLocationCountForTrip(tripId: String): Int
}
