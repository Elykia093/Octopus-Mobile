package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: Int = 0,
    val name: String = "",
    val mode: Int = GroupMode.ROUND_ROBIN,
    @SerialName("match_regex") val matchRegex: String = "",
    @SerialName("first_token_time_out") val firstTokenTimeOut: Int = 0,
    @SerialName("session_keep_time") val sessionKeepTime: Int = 0,
    @SerialName("retry_enabled") val retryEnabled: Boolean = false,
    @SerialName("max_retries") val maxRetries: Int = 0,
    val pinned: Boolean = false,
    @SerialName("pinned_at") val pinnedAt: String? = null,
    @SerialName("active_preset_id") val activePresetId: Int? = null,
    val items: List<GroupItem> = emptyList(),
)

@Serializable
data class GroupItem(
    val id: Int = 0,
    @SerialName("group_id") val groupId: Int = 0,
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("model_name") val modelName: String = "",
    val priority: Int = 0,
    val weight: Int = 0,
)

@Serializable
data class GroupPinRequest(
    val pinned: Boolean,
)

object GroupMode {
    const val ROUND_ROBIN = 1
    const val RANDOM = 2
    const val FAILOVER = 3
    const val WEIGHTED = 4
}
