package com.elykia.octopus.core.di

import com.elykia.octopus.core.data.remote.ApiKeyApiService
import com.elykia.octopus.core.data.remote.ChannelApiService
import com.elykia.octopus.core.data.remote.GroupApiService
import com.elykia.octopus.core.data.remote.LogApiService
import com.elykia.octopus.core.data.remote.ModelApiService
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

    @Provides
    @Singleton
    fun provideChannelApiService(retrofit: Retrofit): ChannelApiService {
        return retrofit.create(ChannelApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiKeyApiService(retrofit: Retrofit): ApiKeyApiService {
        return retrofit.create(ApiKeyApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDashboardApiService(retrofit: Retrofit): com.elykia.octopus.core.data.remote.DashboardApiService {
        return retrofit.create(com.elykia.octopus.core.data.remote.DashboardApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLogApiService(retrofit: Retrofit): LogApiService {
        return retrofit.create(LogApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGroupApiService(retrofit: Retrofit): GroupApiService {
        return retrofit.create(GroupApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideModelApiService(retrofit: Retrofit): ModelApiService {
        return retrofit.create(ModelApiService::class.java)
    }
}
