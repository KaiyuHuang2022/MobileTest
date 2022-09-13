package com.kaiyu.mobilechallenge.di

import android.content.Context
import com.kaiyu.mobilechallenge.MobileChallengeApplication
import com.kaiyu.mobilechallenge.domain.repository.Repository
import com.kaiyu.mobilechallenge.data.repository.TypicodeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context) : MobileChallengeApplication {
        return app as MobileChallengeApplication
    }

    @Singleton
    @Provides
    fun provideRepository() : Repository {
        return TypicodeRepository()
    }

}