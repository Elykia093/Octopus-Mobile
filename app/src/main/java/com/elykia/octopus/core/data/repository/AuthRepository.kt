package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.LoginRequest
import com.elykia.octopus.core.data.remote.OctopusApiService
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
            if (response.success || (response.data != null && response.data.token.isNotBlank())) {
                val authState = AuthState(token = response.data?.token ?: "", isApiKeyMode = false)
                preferenceStore.updateAuthState(authState)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message.ifBlank { "Login failed" }))
            }
        } catch (e: retrofit2.HttpException) {
            val url = e.response()?.raw()?.request?.url?.toString() ?: "Unknown URL"
            Result.failure(Exception("HTTP ${e.code()} Not Found\nRequest URL: $url"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithApiKey(apiKey: String): Result<Unit> {
        // Temporarily save the API key into PreferenceStore so the interceptor uses it
        preferenceStore.updateAuthState(AuthState(token = apiKey, isApiKeyMode = true))
        
        return try {
            // Verify if the API key is valid by calling the endpoint
            val response = apiService.loginApiKey()
            if (response.success || response.data != null) {
                // If the response returns a session token (some systems convert key to JWT), save it. 
                // If it doesn't, we keep using the API Key.
                if (response.data != null && response.data.token.isNotBlank()) {
                    preferenceStore.updateAuthState(AuthState(token = response.data.token, isApiKeyMode = true))
                }
                Result.success(Unit)
            } else {
                // Revert auth state if verification fails
                preferenceStore.clearAuthState()
                Result.failure(Exception(response.message.ifBlank { "API Key validation failed" }))
            }
        } catch (e: retrofit2.HttpException) {
            preferenceStore.clearAuthState()
            val url = e.response()?.raw()?.request?.url?.toString() ?: "Unknown URL"
            Result.failure(Exception("HTTP ${e.code()} Not Found\nRequest URL: $url"))
        } catch (e: Exception) {
            preferenceStore.clearAuthState()
            Result.failure(e)
        }
    }

    suspend fun logout() {
        preferenceStore.clearAuthState()
    }
}
