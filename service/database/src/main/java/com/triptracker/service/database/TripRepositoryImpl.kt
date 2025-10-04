package com.triptracker.service.database

import com.triptracker.service.database.dao.LocationDao
import com.triptracker.service.database.dao.TripDao
import com.triptracker.service.database.entity.TripWithLocations
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of TripRepository using Room database
 */
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val locationDao: LocationDao
) : TripRepository {

    override fun getAllTrips(): Flow<List<TripWithLocations>> {
        return tripDao.getAllTripsWithLocations()
    }

    override suspend fun getTripById(tripId: String): TripWithLocations? {
        return tripDao.getTripWithLocations(tripId)
    }

    override suspend fun saveTrip(trip: TripWithLocations) {
        // Insert or update the trip and its locations
        tripDao.insertTrip(trip.trip)
        locationDao.insertLocations(trip.locations)
    }

    override suspend fun deleteTrip(tripId: String) {
        tripDao.deleteTripById(tripId)
    }

    override suspend fun deleteAllTrips() {
        tripDao.deleteAllTrips()
    }

    override suspend fun getTripCount(): Int {
        return tripDao.getTripCount()
    }
}
