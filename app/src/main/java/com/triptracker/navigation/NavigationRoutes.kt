package com.triptracker.navigation

/**
 * Navigation routes for the app
 */
object NavigationRoutes {
    const val TRIPS_LIST = "trips_list"
    const val TRIP_DETAIL = "trip_detail/{tripId}"
    const val ACTIVE_TRIP = "active_trip"
    const val SETTINGS = "settings"
    const val PERMISSIONS = "permissions"
    
    /**
     * Build trip detail route with tripId
     */
    fun tripDetail(tripId: String): String {
        return "trip_detail/$tripId"
    }
}
