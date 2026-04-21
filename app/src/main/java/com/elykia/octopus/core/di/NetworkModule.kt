package com.elykia.octopus.core.di

import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.ServerConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.JavaNetCookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideBaseUrlInterceptor(preferenceStore: PreferenceStore): BaseUrlInterceptor {
        return BaseUrlInterceptor(preferenceStore)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(preferenceStore: PreferenceStore): AuthInterceptor {
        return AuthInterceptor(preferenceStore)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        baseUrlInterceptor: BaseUrlInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val cookieManager = CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }
        return OkHttpClient.Builder()
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://localhost/") 
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}

class BaseUrlInterceptor(private val preferenceStore: PreferenceStore) : Interceptor {
    @Volatile
    private var cachedConfig = ServerConfig()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            preferenceStore.serverConfig.collect { cachedConfig = it }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val base = normalizeBaseUrl(cachedConfig.baseUrl)
        if (base.isBlank()) return chain.proceed(original)

        return try {
            val baseHttpUrl = base.toHttpUrlOrNull() ?: return chain.proceed(original)
            val newUrl = original.url.newBuilder()
                .scheme(baseHttpUrl.scheme)
                .host(baseHttpUrl.host)
                .port(baseHttpUrl.port)
                .apply {
                    val basePathSegments = baseHttpUrl.pathSegments.filter { it.isNotEmpty() }
                    var originalPathSegments = original.url.pathSegments.filter { it.isNotEmpty() }
                    
                    if (basePathSegments.isNotEmpty() && originalPathSegments.isNotEmpty()) {
                        var overlapCount = 0
                        val maxOverlap = minOf(basePathSegments.size, originalPathSegments.size)
                        for (i in 1..maxOverlap) {
                            val baseSuffix = basePathSegments.takeLast(i)
                            val origPrefix = originalPathSegments.take(i)
                            if (baseSuffix == origPrefix) {
                                overlapCount = i
                            }
                        }
                        if (overlapCount > 0) {
                            originalPathSegments = originalPathSegments.drop(overlapCount)
                        }
                    }

                    for (i in 0 until original.url.pathSize) {
                        removePathSegment(0)
                    }
                    
                    basePathSegments.forEach { addPathSegment(it) }
                    originalPathSegments.forEach { addPathSegment(it) }
                }
                .build()

            chain.proceed(original.newBuilder().url(newUrl).build())
        } catch (e: Exception) {
            chain.proceed(original)
        }
    }

    private fun normalizeBaseUrl(rawBaseUrl: String): String {
        if (rawBaseUrl.isBlank()) return ""
        val trimmed = rawBaseUrl.trim().trimEnd('/')
        return if (trimmed.endsWith("/api", ignoreCase = true)) trimmed else "$trimmed/api"
    }
}

class AuthInterceptor(private val preferenceStore: PreferenceStore) : Interceptor {
    @Volatile
    private var cachedAuth = AuthState()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            preferenceStore.authState.collect { cachedAuth = it }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (cachedAuth.token.isBlank()) return chain.proceed(original)

        val headerValue = if (cachedAuth.token.startsWith("Bearer ", ignoreCase = true)) {
            cachedAuth.token
        } else {
            "Bearer ${cachedAuth.token}"
        }
        val request = original.newBuilder()
            .header("Authorization", headerValue)
            .build()
        return chain.proceed(request)
    }
}
