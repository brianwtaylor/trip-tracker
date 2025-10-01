package com.triptracker.service.location.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.triptracker.service.location.data.repository.LocationRepositoryImpl
import com.triptracker.service.location.data.service.LocationClient
import com.triptracker.service.location.data.service.LocationFilter
import com.triptracker.service.location.domain.repository.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationFilter(): LocationFilter {
        return LocationFilter()
    }

    @Provides
    @Singleton
    fun provideLocationClient(
        @ApplicationContext context: Context,
        fusedLocationProviderClient: FusedLocationProviderClient,
        locationFilter: LocationFilter
    ): LocationClient {
        return LocationClient(context, fusedLocationProviderClient, locationFilter)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        locationClient: LocationClient
    ): LocationRepository {
        return LocationRepositoryImpl(locationClient)
    }
}
