package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.LoginRequest
import com.elykia.octopus.core.data.remote.toUserMessage
import com.elykia.octopus.core.data.remote.ApiKeyApiService
import com.elykia.octopus.core.data.remote.JwtTokens
import com.elykia.octopus.core.data.remote.OctopusApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: OctopusApiService,
    private val apiKeyApiService: ApiKeyApiService,
    private val preferenceStore: PreferenceStore,
) {
    suspend fun loginAsAdmin(request: LoginRequest): Result<Unit> {
        return try {
            val response = apiService.loginUser(request)
            if (!response.isSuccessful) {
                Result.failure(Exception(response.message.ifBlank { "登录失败" }))
            } else {
                val token = response.data?.token.orEmpty()
                if (token.isBlank()) {
                    Result.failure(Exception("未获取到访问令牌"))
                } else {
                    preferenceStore.updateAuthState(
                        AuthState(
                            token = token,
                            isApiKeyMode = false,
                            role = 0,
                        )
                    )
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage("登录失败"), e))
        }
    }

    suspend fun loginWithApiKey(apiKey: String): Result<Unit> {
        return try {
            val normalizedApiKey = apiKey.trim()
            val response = apiKeyApiService.loginApiKey(JwtTokens.authorizationHeader(normalizedApiKey))
            if (!response.isSuccessful) {
                Result.failure(Exception(response.message.ifBlank { "API Key 登录失败" }))
            } else {
                preferenceStore.updateAuthState(AuthState(token = normalizedApiKey, isApiKeyMode = true))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage("API Key 登录失败"), e))
        }
    }

    suspend fun logout() {
        preferenceStore.clearAuthState()
    }
}
