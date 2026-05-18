package com.elykia.octopus.core.di

import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.remote.JwtTokens
import com.elykia.octopus.core.data.remote.ServerUrlResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.JavaNetCookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.converter.kotlinx.serialization.asConverterFactory

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
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
        }
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
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val config = runBlocking { preferenceStore.serverConfig.first() }
        val baseUrl = ServerUrlResolver.normalize(config.baseUrl) ?: return chain.proceed(original)
        val newUrl = ServerUrlResolver.merge(baseUrl, original.url)

        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}

class AuthInterceptor(private val preferenceStore: PreferenceStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // 直接同步读取最新的 auth state，避免竞态条件
        val authState = try {
            kotlinx.coroutines.runBlocking {
                preferenceStore.authState.first()
            }
        } catch (e: Exception) {
            AuthState()
        }

        if (authState.token.isBlank()) return chain.proceed(original)

        if (!authState.isApiKeyMode && JwtTokens.isExpired(authState.token)) {
            runBlocking { preferenceStore.clearAuthState() }
            return chain.proceed(original)
        }

        val request = original.newBuilder()
            .header("Authorization", JwtTokens.authorizationHeader(authState.token))
            .build()
        val response = chain.proceed(request)

        if (response.code == 401) {
            runBlocking { preferenceStore.clearAuthState() }
        }

        return response
    }
}
