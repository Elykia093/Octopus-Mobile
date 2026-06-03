package com.elykia.octopus.core.di

import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.local.SecureSessionStore
import com.elykia.octopus.core.data.local.SessionManager
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.OctopusApiService
import com.elykia.octopus.core.data.remote.ServerUrlResolver
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private class BaseUrlInterceptor(
    private val preferenceStore: PreferenceStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val baseUrl = runBlocking {
            ServerUrlResolver.normalize(preferenceStore.serverConfig.first())
        }
        val newUrl = ServerUrlResolver.merge(baseUrl, request.url)
        return chain.proceed(request.newBuilder().url(newUrl).build())
    }
}

internal class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val onUnauthorized: () -> Unit,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val auth = sessionManager.currentAuth()
        val token = auth.token
        if (token.isNotBlank() && auth.isBoundTo(request.url)) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        val response = chain.proceed(requestBuilder.build())
        if (response.code == 401 && token.isNotBlank() && auth.isBoundTo(request.url)) {
            onUnauthorized()
        }
        return response
    }
}

private fun com.elykia.octopus.core.data.model.AuthState.isBoundTo(requestUrl: okhttp3.HttpUrl): Boolean {
    val boundUrl = serverUrl.toHttpUrlOrNull() ?: return false
    return token.isNotBlank() &&
        boundUrl.scheme == requestUrl.scheme &&
        boundUrl.host == requestUrl.host &&
        boundUrl.port == requestUrl.port &&
        requestUrl.encodedPath.startsWith(boundUrl.encodedPath.trimEnd('/') + "/")
}

private fun clearSessionAfterUnauthorized(
    secureSessionStore: SecureSessionStore,
    sessionManager: SessionManager,
) {
    val persistedCleared = secureSessionStore.clear()
    sessionManager.clear()
    if (!persistedCleared) {
        sessionManager.markSecurityWarning("安全清除会话失败，请检查设备安全存储后重试。")
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
        secureSessionStore: SecureSessionStore,
        sessionManager: SessionManager,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(BaseUrlInterceptor(preferenceStore))
            .addInterceptor(
                AuthInterceptor(sessionManager) {
                    clearSessionAfterUnauthorized(secureSessionStore, sessionManager)
                }
            )
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideNetworkExecutor(
        json: Json,
        secureSessionStore: SecureSessionStore,
        sessionManager: SessionManager,
    ): NetworkExecutor = NetworkExecutor(json) {
        clearSessionAfterUnauthorized(secureSessionStore, sessionManager)
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        json: Json,
        client: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://127.0.0.1:8080/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): OctopusApiService = retrofit.create(OctopusApiService::class.java)
}
