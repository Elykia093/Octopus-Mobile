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
    val pinned: Boolean = false,
    @SerialName("pinned_at") val pinnedAt: String? = null,
    @SerialName("active_preset_id") val activePresetId: Int? = null,
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

@Serializable
data class GroupPinRequest(
    val pinned: Boolean,
)

@Serializable
data class GroupPresetItem(
    @SerialName("channel_id") val channelId: Int,
    @SerialName("model_name") val modelName: String,
    val priority: Int = 0,
    val weight: Int = 0,
)

@Serializable
data class GroupPreset(
    val id: Int = 0,
    @SerialName("group_id") val groupId: Int = 0,
    val name: String,
    val mode: Int = 1,
    @SerialName("match_regex") val matchRegex: String = "",
    @SerialName("first_token_time_out") val firstTokenTimeOut: Int = 0,
    @SerialName("session_keep_time") val sessionKeepTime: Int = 0,
    @SerialName("retry_enabled") val retryEnabled: Boolean = false,
    @SerialName("max_retries") val maxRetries: Int = 3,
    val items: List<GroupPresetItem> = emptyList(),
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
)

@Serializable
data class GroupPresetNameRequest(
    val name: String,
)

@Serializable
data class GroupPresetUpdateRequest(
    val name: String? = null,
    val mode: Int? = null,
    @SerialName("match_regex") val matchRegex: String? = null,
    @SerialName("first_token_time_out") val firstTokenTimeOut: Int? = null,
    @SerialName("session_keep_time") val sessionKeepTime: Int? = null,
    @SerialName("retry_enabled") val retryEnabled: Boolean? = null,
    @SerialName("max_retries") val maxRetries: Int? = null,
    val items: List<GroupPresetItem>? = null,
)

@Serializable
data class GroupAutoGroupSource(
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("channel_name") val channelName: String = "",
    val enabled: Boolean = true,
    val managed: Boolean = false,
    @SerialName("auto_group") val autoGroup: Int = 0,
    @SerialName("effective_auto_group") val effectiveAutoGroup: Int = 0,
    @SerialName("global_override") val globalOverride: Boolean = false,
    @SerialName("model_count") val modelCount: Int = 0,
    val models: List<String> = emptyList(),
    @SerialName("site_id") val siteId: Int? = null,
    @SerialName("site_name") val siteName: String? = null,
    @SerialName("site_account_id") val siteAccountId: Int? = null,
    @SerialName("site_account_name") val siteAccountName: String? = null,
    @SerialName("site_group_key") val siteGroupKey: String? = null,
    @SerialName("site_group_name") val siteGroupName: String? = null,
    @SerialName("endpoint_type") val endpointType: String? = null,
)

@Serializable
data class GroupAutoGroupConfig(
    @SerialName("projected_global_auto_group") val projectedGlobalAutoGroup: Int = 0,
    val sources: List<GroupAutoGroupSource> = emptyList(),
)

@Serializable
data class GroupAutoGroupSourceUpdateRequest(
    @SerialName("channel_id") val channelId: Int,
    @SerialName("auto_group") val autoGroup: Int,
)

@Serializable
data class GroupAutoGroupConfigUpdateRequest(
    @SerialName("projected_global_auto_group") val projectedGlobalAutoGroup: Int? = null,
    val items: List<GroupAutoGroupSourceUpdateRequest> = emptyList(),
    @SerialName("run_now") val runNow: Boolean = false,
)

@Serializable
data class GroupAutoGroupRunRequest(
    @SerialName("channel_ids") val channelIds: List<Int> = emptyList(),
)

@Serializable
class EmptyRequest

object GroupHealthProbeMode {
    const val Standard = "standard"
    const val Full = "full"
}

@Serializable
data class GroupHealthAttempt(
    val id: Int = 0,
    @SerialName("snapshot_id") val snapshotId: Int = 0,
    @SerialName("group_item_id") val groupItemId: Int = 0,
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("channel_name") val channelName: String = "",
    @SerialName("channel_key_id") val channelKeyId: Int = 0,
    @SerialName("key_remark") val keyRemark: String = "",
    @SerialName("model_name") val modelName: String = "",
    val priority: Int = 0,
    val weight: Int = 0,
    val status: String = "failed",
    @SerialName("http_status") val httpStatus: Int = 0,
    @SerialName("duration_ms") val durationMs: Int = 0,
    @SerialName("error_message") val errorMessage: String = "",
)

@Serializable
data class GroupHealthSnapshot(
    val id: Int = 0,
    @SerialName("group_id") val groupId: Int = 0,
    @SerialName("group_name") val groupName: String = "",
    @SerialName("group_mode") val groupMode: Int = 1,
    @SerialName("probe_mode") val probeMode: String = GroupHealthProbeMode.Standard,
    @SerialName("request_model") val requestModel: String = "",
    val status: String = "failed",
    @SerialName("started_at") val startedAt: String = "",
    @SerialName("finished_at") val finishedAt: String? = null,
    @SerialName("duration_ms") val durationMs: Int = 0,
    @SerialName("successful_channel_id") val successfulChannelId: Int? = null,
    val message: String = "",
    val attempts: List<GroupHealthAttempt> = emptyList(),
)

@Serializable
data class GroupHealthGroupView(
    @SerialName("group_id") val groupId: Int = 0,
    @SerialName("group_name") val groupName: String = "",
    @SerialName("group_mode") val groupMode: Int = 1,
    val latest: GroupHealthSnapshot? = null,
)

@Serializable
data class RunGroupHealthRequest(
    @SerialName("probe_mode") val probeMode: String? = null,
)

@Serializable
data class RunGroupHealthAccepted(
    @SerialName("group_id") val groupId: Int? = null,
    @SerialName("all_groups") val allGroups: Boolean = false,
    @SerialName("probe_mode") val probeMode: String? = null,
)
