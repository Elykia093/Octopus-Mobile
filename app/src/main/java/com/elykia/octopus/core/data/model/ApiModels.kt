package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ApiResponse<T>(
    val code: Int? = null,
    @SerialName("error_code") val errorCode: String = "",
    val success: Boolean = false,
    val message: String = "",
    val params: JsonObject? = null,
    val data: T? = null
) {
    val isSuccessful: Boolean
        get() = success || code in 200..299
}

@Serializable
data class LoginRequest(
    val username: String,
    val password: String? = null, // 仅管理员登录使用
    val expire: Int? = null, // 管理员令牌有效期，单位：分钟
)

@Serializable
data class LoginResponse(
    val token: String = "",
    @SerialName("expire_at") val expireAt: String = "",
)

@Serializable
data class UserProfile(
    val id: Long = 0L,
    val username: String = "",
    @SerialName("display_name") val displayName: String = "",
    val role: Int = 0,
    val status: Int = 0,
    val email: String = "",
    @SerialName("access_token") val accessToken: String = "",
    val quota: Long = 0L,
    @SerialName("used_quota") val usedQuota: Long = 0L,
    @SerialName("request_count") val requestCount: Int = 0,
)
