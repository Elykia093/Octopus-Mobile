package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.LoginRequest
import com.elykia.octopus.core.data.remote.OctopusApiService
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: OctopusApiService,
    private val preferenceStore: PreferenceStore,
) {
    suspend fun loginAsAdmin(request: LoginRequest): Result<Unit> {
        return try {
            val response = apiService.loginUser(request)
            if (!response.success) {
                Result.failure(Exception(response.message.ifBlank { "登录失败" }))
            } else {
                val tokenResponse = apiService.generateAccessToken()
                val token = tokenResponse.data.orEmpty()
                if (token.isBlank()) {
                    Result.failure(Exception(tokenResponse.message.ifBlank { "未获取到访问令牌" }))
                } else {
                    preferenceStore.updateAuthState(
                        AuthState(
                            token = token,
                            isApiKeyMode = false,
                            role = response.data?.role ?: 0,
                        )
                    )
                    Result.success(Unit)
                }
            }
        } catch (e: HttpException) {
            val url = e.response()?.raw()?.request?.url?.toString() ?: "Unknown URL"
            Result.failure(Exception("HTTP ${e.code()} Error\nRequest URL: $url"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithApiKey(apiKey: String): Result<Unit> {
        return try {
            preferenceStore.updateAuthState(AuthState(token = apiKey, isApiKeyMode = true))
            val response = apiService.getSelf()
            if (response.success && response.data != null) {
                preferenceStore.updateAuthState(
                    AuthState(
                        token = apiKey,
                        isApiKeyMode = true,
                        role = response.data.role,
                    )
                )
                Result.success(Unit)
            } else {
                preferenceStore.clearAuthState()
                Result.failure(Exception(response.message.ifBlank { "API Key 校验失败" }))
            }
        } catch (e: HttpException) {
            preferenceStore.clearAuthState()
            val url = e.response()?.raw()?.request?.url?.toString() ?: "Unknown URL"
            Result.failure(Exception("HTTP ${e.code()} Error\nRequest URL: $url"))
        } catch (e: Exception) {
            preferenceStore.clearAuthState()
            Result.failure(e)
        }
    }

    suspend fun logout() {
        preferenceStore.clearAuthState()
    }
}
