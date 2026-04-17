package com.elykia.octopus.core.di

import com.elykia.octopus.core.data.remote.OctopusApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideOctopusApiService(retrofit: Retrofit): OctopusApiService {
        return retrofit.create(OctopusApiService::class.java)
    }
}
