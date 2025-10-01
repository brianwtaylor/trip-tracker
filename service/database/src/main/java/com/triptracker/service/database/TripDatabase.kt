package com.triptracker.service.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.triptracker.service.database.dao.LocationDao
import com.triptracker.service.database.dao.TripDao
import com.triptracker.service.database.entity.LocationEntity
import com.triptracker.service.database.entity.TripEntity

@Database(
    entities = [
        TripEntity::class,
        LocationEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun locationDao(): LocationDao
}
