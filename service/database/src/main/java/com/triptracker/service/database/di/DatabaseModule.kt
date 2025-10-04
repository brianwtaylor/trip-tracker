package com.triptracker.service.database.di

import android.content.Context
import androidx.room.Room
import com.triptracker.core.common.constants.Constants
import com.triptracker.service.database.TripRepository
import com.triptracker.service.database.TripRepositoryImpl
import com.triptracker.service.database.TripDatabase
import com.triptracker.service.database.dao.LocationDao
import com.triptracker.service.database.dao.TripDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTripDatabase(
        @ApplicationContext context: Context
    ): TripDatabase {
        return Room.databaseBuilder(
            context,
            TripDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTripDao(database: TripDatabase): TripDao {
        return database.tripDao()
    }
    
    @Provides
    @Singleton
    fun provideLocationDao(database: TripDatabase): LocationDao {
        return database.locationDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTripRepository(impl: TripRepositoryImpl): TripRepository
}
