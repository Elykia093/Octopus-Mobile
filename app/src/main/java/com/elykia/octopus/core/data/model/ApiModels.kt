package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val code: Int? = null,
    val message: String? = null,
    val data: T? = null,
)

@Serializable
data class UserLoginRequest(
    val username: String,
    val password: String,
    val expire: Int,
)

@Serializable
data class UserLoginResponse(
    val token: String,
    @SerialName("expire_at") val expireAt: String,
)

@Serializable
data class SettingItem(
    val key: String,
    val value: String,
)

@Serializable
data class StatsTotal(
    val id: Int = 0,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class StatsDaily(
    val date: String,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class StatsHourly(
    val hour: Int,
    val date: String,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class StatsChannel(
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class BaseUrl(
    val url: String,
    val delay: Int = 0,
)

@Serializable
data class ChannelKey(
    val id: Int = 0,
    @SerialName("channel_id") val channelId: Int = 0,
    val enabled: Boolean = true,
    @SerialName("channel_key") val channelKey: String,
    @SerialName("status_code") val statusCode: Int = 0,
    @SerialName("last_use_time_stamp") val lastUseTimeStamp: Long = 0,
    @SerialName("total_cost") val totalCost: Double = 0.0,
    val remark: String = "",
)

@Serializable
data class CustomHeader(
    @SerialName("header_key") val headerKey: String,
    @SerialName("header_value") val headerValue: String,
)

@Serializable
data class Channel(
    val id: Int = 0,
    val name: String,
    val type: Int,
    val enabled: Boolean = true,
    @SerialName("base_urls") val baseUrls: List<BaseUrl> = emptyList(),
    val keys: List<ChannelKey> = emptyList(),
    val model: String = "",
    @SerialName("custom_model") val customModel: String = "",
    val proxy: Boolean = false,
    @SerialName("auto_sync") val autoSync: Boolean = false,
    @SerialName("auto_group") val autoGroup: Int = 0,
    @SerialName("custom_header") val customHeader: List<CustomHeader> = emptyList(),
    @SerialName("param_override") val paramOverride: String? = null,
    @SerialName("channel_proxy") val channelProxy: String? = null,
    @SerialName("match_regex") val matchRegex: String? = null,
    val stats: StatsChannel? = null,
)

@Serializable
data class GroupItem(
    val id: Int = 0,
    @SerialName("group_id") val groupId: Int = 0,
    @SerialName("channel_id") val channelId: Int,
    @SerialName("model_name") val modelName: String,
    val priority: Int = 0,
    val weight: Int = 0,
)

@Serializable
data class Group(
    val id: Int = 0,
    val name: String,
    val mode: Int,
    @SerialName("match_regex") val matchRegex: String = "",
    @SerialName("first_token_time_out") val firstTokenTimeOut: Int = 0,
    @SerialName("session_keep_time") val sessionKeepTime: Int = 0,
    val items: List<GroupItem> = emptyList(),
)

@Serializable
data class LlmInfo(
    val name: String,
    val input: Double = 0.0,
    val output: Double = 0.0,
    @SerialName("cache_read") val cacheRead: Double = 0.0,
    @SerialName("cache_write") val cacheWrite: Double = 0.0,
)

@Serializable
data class LlmChannel(
    val name: String,
    val enabled: Boolean,
    @SerialName("channel_id") val channelId: Int,
    @SerialName("channel_name") val channelName: String,
)

@Serializable
data class ChannelAttempt(
    @SerialName("channel_id") val channelId: Int,
    @SerialName("channel_key_id") val channelKeyId: Int? = null,
    @SerialName("channel_name") val channelName: String,
    @SerialName("model_name") val modelName: String,
    @SerialName("attempt_num") val attemptNum: Int,
    val status: String,
    val duration: Int,
    val sticky: Boolean? = null,
    val msg: String? = null,
)

@Serializable
data class RelayLog(
    val id: Long,
    val time: Long,
    @SerialName("request_model_name") val requestModelName: String,
    @SerialName("request_api_key_name") val requestApiKeyName: String? = null,
    @SerialName("channel") val channelId: Int,
    @SerialName("channel_name") val channelName: String,
    @SerialName("actual_model_name") val actualModelName: String,
    @SerialName("input_tokens") val inputTokens: Int,
    @SerialName("output_tokens") val outputTokens: Int,
    val ftut: Int,
    @SerialName("use_time") val useTime: Int,
    val cost: Double,
    @SerialName("request_content") val requestContent: String,
    @SerialName("response_content") val responseContent: String,
    val error: String,
    val attempts: List<ChannelAttempt> = emptyList(),
    @SerialName("total_attempts") val totalAttempts: Int = 0,
)

@Serializable
data class ApiKeyItem(
    val id: Int,
    val name: String,
    @SerialName("api_key") val apiKey: String,
    val enabled: Boolean = true,
    @SerialName("expire_at") val expireAt: Long? = null,
    @SerialName("max_cost") val maxCost: Double? = null,
    @SerialName("supported_models") val supportedModels: String? = null,
)

@Serializable
data class ApiKeyStats(
    @SerialName("api_key_id") val apiKeyId: Int,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class ApiKeyDashboard(
    val stats: ApiKeyStats,
    val info: ApiKeyItem,
)

@Serializable
data class LatestInfo(
    @SerialName("tag_name") val tagName: String,
    @SerialName("published_at") val publishedAt: String,
    val body: String = "",
    val message: String = "",
)

@Serializable
data class ImportResult(
    @SerialName("rows_affected") val rowsAffected: Map<String, Int> = emptyMap(),
)

@Serializable
data class ThemePreference(
    val mode: Int = 0,
)

@Serializable
data class ServerConfig(
    val baseUrl: String = "",
    val language: String = "system",
    val themeMode: Int = 0,
)

@Serializable
data class AuthState(
    val token: String = "",
    val expireAt: String? = null,
    val username: String = "",
    val apiKeyMode: Boolean = false,
)
