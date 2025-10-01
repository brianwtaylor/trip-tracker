package com.triptracker.service.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TripWithLocations(
    @Embedded
    val trip: TripEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val locations: List<LocationEntity>
)
