package com.kaiyu.mobilechallenge.di

import android.content.Context
import android.util.Log
import com.kaiyu.mobilechallenge.MobileChallengeApplication
import com.kaiyu.mobilechallenge.data.remote.CustomisedDNS
import com.kaiyu.mobilechallenge.data.remote.TypicodeAPI
import com.kaiyu.mobilechallenge.data.repository.TypicodeRepository
import com.kaiyu.mobilechallenge.common.Constants
import com.kaiyu.mobilechallenge.domain.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context) : MobileChallengeApplication {
        return app as MobileChallengeApplication
    }

    /**
     * The API to access Typicode database
     */
    @Provides
    @Singleton
    fun provideTypicodeAPI() : TypicodeAPI {

        val loggingInterceptor = HttpLoggingInterceptor {
            Log.i("Http Log", it)
        }.setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .eventListenerFactory(LoggingEventListener.Factory())
            .dns(CustomisedDNS())
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.DATABASE_URL_BASE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TypicodeAPI::class.java)
    }


    /**
     * The repository to get product list and details
     */
    @Provides
    @Singleton
    fun provideRepository(api: TypicodeAPI): Repository {
        return TypicodeRepository(api)
    }

}