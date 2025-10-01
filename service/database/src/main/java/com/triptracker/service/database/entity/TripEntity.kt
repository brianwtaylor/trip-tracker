package com.triptracker.service.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val id: String,
    val startTime: Long,
    val endTime: Long?,
    val distance: Double,
    val averageSpeed: Double,
    val maxSpeed: Double,
    val status: String
)
