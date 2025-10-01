package com.triptracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TripTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize app-level components here
    }
}
