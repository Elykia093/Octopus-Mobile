package com.elykia.octopus.core.di

import com.elykia.octopus.core.data.local.SecureSessionStore
import com.elykia.octopus.core.data.local.SessionManager
import com.elykia.octopus.core.data.remote.ApiKeyApiService
import com.elykia.octopus.core.data.remote.AuthApiService
import com.elykia.octopus.core.data.remote.ChannelApiService
import com.elykia.octopus.core.data.remote.DataTransferApiService
import com.elykia.octopus.core.data.remote.GroupApiService
import com.elykia.octopus.core.data.remote.LogApiService
import com.elykia.octopus.core.data.remote.ModelApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.ProxyPoolApiService
import com.elykia.octopus.core.data.remote.ServerUrlResolver
import com.elykia.octopus.core.data.remote.ServerUrlProvider
import com.elykia.octopus.core.data.remote.SettingApiService
import com.elykia.octopus.core.data.remote.SiteChannelApiService
import com.elykia.octopus.core.data.remote.SiteApiService
import com.elykia.octopus.core.data.remote.StatsApiService
import com.elykia.octopus.core.data.remote.UpdateApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

internal class BaseUrlInterceptor(
    private val serverUrlProvider: ServerUrlProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val baseUrl = serverUrlProvider.current()
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
        if (request.header("Authorization").isNullOrBlank() && token.isNotBlank() && auth.isBoundTo(request.url)) {
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
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        serverUrlProvider: ServerUrlProvider,
        secureSessionStore: SecureSessionStore,
        sessionManager: SessionManager,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(BaseUrlInterceptor(serverUrlProvider))
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
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideStatsApiService(retrofit: Retrofit): StatsApiService = retrofit.create(StatsApiService::class.java)

    @Provides
    @Singleton
    fun provideChannelApiService(retrofit: Retrofit): ChannelApiService = retrofit.create(ChannelApiService::class.java)

    @Provides
    @Singleton
    fun provideGroupApiService(retrofit: Retrofit): GroupApiService = retrofit.create(GroupApiService::class.java)

    @Provides
    @Singleton
    fun provideModelApiService(retrofit: Retrofit): ModelApiService = retrofit.create(ModelApiService::class.java)

    @Provides
    @Singleton
    fun provideLogApiService(retrofit: Retrofit): LogApiService = retrofit.create(LogApiService::class.java)

    @Provides
    @Singleton
    fun provideSettingApiService(retrofit: Retrofit): SettingApiService = retrofit.create(SettingApiService::class.java)

    @Provides
    @Singleton
    fun provideApiKeyApiService(retrofit: Retrofit): ApiKeyApiService = retrofit.create(ApiKeyApiService::class.java)

    @Provides
    @Singleton
    fun provideUpdateApiService(retrofit: Retrofit): UpdateApiService = retrofit.create(UpdateApiService::class.java)

    @Provides
    @Singleton
    fun provideDataTransferApiService(retrofit: Retrofit): DataTransferApiService =
        retrofit.create(DataTransferApiService::class.java)

    @Provides
    @Singleton
    fun provideSiteApiService(retrofit: Retrofit): SiteApiService = retrofit.create(SiteApiService::class.java)

    @Provides
    @Singleton
    fun provideSiteChannelApiService(retrofit: Retrofit): SiteChannelApiService =
        retrofit.create(SiteChannelApiService::class.java)

    @Provides
    @Singleton
    fun provideProxyPoolApiService(retrofit: Retrofit): ProxyPoolApiService =
        retrofit.create(ProxyPoolApiService::class.java)
}
