package com.triptracker.service.activity.di

import com.triptracker.service.activity.data.repository.ActivityRepositoryImpl
import com.triptracker.service.activity.domain.repository.ActivityRepository
import com.triptracker.service.activity.domain.usecase.DetectUserRoleUseCase
import com.triptracker.service.activity.domain.usecase.ManageActivityRecognitionUseCase
import com.triptracker.service.activity.domain.usecase.IntegrateTripActivityUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for activity recognition service
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityModule {

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        impl: ActivityRepositoryImpl
    ): ActivityRepository

    companion object {
        @Provides
        @Singleton
        fun provideDetectUserRoleUseCase(): DetectUserRoleUseCase {
            return DetectUserRoleUseCase()
        }

        @Provides
        @Singleton
        fun provideManageActivityRecognitionUseCase(
            activityRepository: ActivityRepository
        ): ManageActivityRecognitionUseCase {
            return ManageActivityRecognitionUseCase(activityRepository)
        }

        @Provides
        @Singleton
        fun provideIntegrateTripActivityUseCase(
            manageActivityRecognitionUseCase: ManageActivityRecognitionUseCase
        ): IntegrateTripActivityUseCase {
            return IntegrateTripActivityUseCase(manageActivityRecognitionUseCase)
        }
    }
}
