package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("retry_enabled") val retryEnabled: Boolean = false,
    @SerialName("max_retries") val maxRetries: Int = 3,
    val items: List<GroupItem> = emptyList(),
)

@Serializable
data class GroupItemAddRequest(
    @SerialName("channel_id") val channelId: Int,
    @SerialName("model_name") val modelName: String,
    val priority: Int = 0,
    val weight: Int = 0,
)

@Serializable
data class GroupItemUpdateRequest(
    val id: Int,
    val priority: Int = 0,
    val weight: Int = 0,
)

@Serializable
data class GroupUpdateRequest(
    val id: Int,
    val name: String? = null,
    val mode: Int? = null,
    @SerialName("match_regex") val matchRegex: String? = null,
    @SerialName("first_token_time_out") val firstTokenTimeOut: Int? = null,
    @SerialName("session_keep_time") val sessionKeepTime: Int? = null,
    @SerialName("retry_enabled") val retryEnabled: Boolean? = null,
    @SerialName("max_retries") val maxRetries: Int? = null,
    @SerialName("items_to_add") val itemsToAdd: List<GroupItemAddRequest> = emptyList(),
    @SerialName("items_to_update") val itemsToUpdate: List<GroupItemUpdateRequest> = emptyList(),
    @SerialName("items_to_delete") val itemsToDelete: List<Int> = emptyList(),
)
