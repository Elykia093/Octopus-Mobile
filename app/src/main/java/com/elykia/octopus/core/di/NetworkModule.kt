package com.elykia.octopus.core.di

import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.local.SessionManager
import com.elykia.octopus.core.data.model.ServerConfig
import com.elykia.octopus.core.data.remote.OctopusApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private class BaseUrlInterceptor(
    private val preferenceStore: PreferenceStore,
) : Interceptor {
    private var currentConfig: ServerConfig? = null

    init {
        preferenceStore.serverConfig
            .onEach { currentConfig = it }
            .launchIn(CoroutineScope(Dispatchers.IO))
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val config = currentConfig ?: runBlocking { preferenceStore.serverConfig.first() }
        val base = normalizeBaseUrl(config)
        val newUrl = request.url.newBuilder()
            .scheme(base.scheme)
            .host(base.host)
            .port(base.port)
            .encodedPath(base.encodedPath.trimEnd('/') + request.url.encodedPath)
            .build()
        return chain.proceed(request.newBuilder().url(newUrl).build())
    }

    private fun normalizeBaseUrl(config: ServerConfig): HttpUrl {
        val rawBaseUrl = if (config.baseUrl.isBlank()) "http://127.0.0.1:8080" else config.baseUrl
        return rawBaseUrl.trimEnd('/').plus("/").toHttpUrlOrNull()
            ?: "http://127.0.0.1:8080/".toHttpUrl()
    }
}

private class AuthInterceptor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val token = sessionManager.currentToken()
        if (token.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        val response = chain.proceed(requestBuilder.build())
        if (response.code == 401 && token.isNotBlank()) {
            sessionManager.clear()
        }
        return response
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        preferenceStore: PreferenceStore,
        sessionManager: SessionManager,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(BaseUrlInterceptor(preferenceStore))
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        json: Json,
        client: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8080/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): OctopusApiService = retrofit.create(OctopusApiService::class.java)
}
