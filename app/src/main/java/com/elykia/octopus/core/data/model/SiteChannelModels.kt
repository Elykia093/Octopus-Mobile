package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

object SiteModelRouteType {
    const val Unknown = "unknown"
    const val OpenAiChat = "openai_chat"
    const val OpenAiResponse = "openai_response"
    const val Anthropic = "anthropic"
    const val Gemini = "gemini"
    const val Volcengine = "volcengine"
    const val OpenAiEmbedding = "openai_embedding"
}

@Serializable
data class SiteRouteSummary(
    @SerialName("route_type") val routeType: String = SiteModelRouteType.Unknown,
    val count: Int = 0,
)

@Serializable
data class SiteModelHistoryBucket(
    val time: Long = 0,
    val success: Int = 0,
    val failure: Int = 0,
)

@Serializable
data class SiteModelHistorySummary(
    @SerialName("success_count") val successCount: Int = 0,
    @SerialName("failure_count") val failureCount: Int = 0,
    @SerialName("last_request_at") val lastRequestAt: Long? = null,
    @SerialName("bucket_span") val bucketSpan: Int = 0,
    val buckets: List<SiteModelHistoryBucket> = emptyList(),
)

@Serializable
data class SiteChannelModel(
    @SerialName("model_name") val modelName: String = "",
    val source: String = "",
    @SerialName("route_type") val routeType: String = SiteModelRouteType.Unknown,
    @SerialName("route_source") val routeSource: String = "",
    @SerialName("manual_override") val manualOverride: Boolean = false,
    val disabled: Boolean = false,
    @SerialName("projected_channel_id") val projectedChannelId: Int? = null,
    @SerialName("route_metadata") val routeMetadata: JsonElement? = null,
    val history: SiteModelHistorySummary? = null,
)

@Serializable
data class SiteModelRouteUpdateRequest(
    @SerialName("group_key") val groupKey: String,
    @SerialName("model_name") val modelName: String,
    @SerialName("route_type") val routeType: String,
    @SerialName("route_raw_payload") val routeRawPayload: String? = null,
)

@Serializable
data class SiteModelDisableUpdateRequest(
    @SerialName("group_key") val groupKey: String,
    @SerialName("model_name") val modelName: String,
    val disabled: Boolean,
)

@Serializable
data class SiteProjectedChannelSettings(
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("channel_name") val channelName: String = "",
    @SerialName("route_type") val routeType: String = SiteModelRouteType.Unknown,
    @SerialName("auto_group") val autoGroup: Int = 0,
    @SerialName("effective_auto_group") val effectiveAutoGroup: Int = 0,
    @SerialName("param_override") val paramOverride: String = "",
    @SerialName("global_override") val globalOverride: Boolean = false,
)

@Serializable
data class SiteProjectedChannelSettingsUpdateRequest(
    @SerialName("channel_id") val channelId: Int,
    @SerialName("auto_group") val autoGroup: Int,
    @SerialName("param_override") val paramOverride: String,
)

@Serializable
data class SiteSourceKey(
    val id: Int = 0,
    val enabled: Boolean = true,
    val token: String = "",
    @SerialName("token_masked") val tokenMasked: String = "",
    val name: String = "",
    @SerialName("group_key") val groupKey: String = "",
    @SerialName("group_name") val groupName: String = "",
    @SerialName("value_status") val valueStatus: String = "ready",
    @SerialName("last_sync_at") val lastSyncAt: Long? = null,
)

@Serializable
data class SiteChannelKeyCreateRequest(
    @SerialName("group_key") val groupKey: String,
    val name: String? = null,
)

@Serializable
data class SiteSourceKeyAddRequest(
    val enabled: Boolean,
    val token: String,
    val name: String? = null,
)

@Serializable
data class SiteSourceKeyUpdateItem(
    val id: Int,
    val enabled: Boolean? = null,
    val token: String? = null,
    val name: String? = null,
)

@Serializable
data class SiteSourceKeyUpdateRequest(
    @SerialName("group_key") val groupKey: String,
    @SerialName("keys_to_add") val keysToAdd: List<SiteSourceKeyAddRequest>? = null,
    @SerialName("keys_to_update") val keysToUpdate: List<SiteSourceKeyUpdateItem>? = null,
    @SerialName("keys_to_delete") val keysToDelete: List<Int>? = null,
)

@Serializable
data class SiteProjectedKey(
    val id: Int = 0,
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("channel_name") val channelName: String = "",
    val enabled: Boolean = true,
    @SerialName("channel_key") val channelKey: String = "",
    @SerialName("channel_key_masked") val channelKeyMasked: String = "",
    val remark: String = "",
    @SerialName("status_code") val statusCode: Int = 0,
    @SerialName("last_use_time_stamp") val lastUseTimeStamp: Long = 0,
    @SerialName("total_cost") val totalCost: Double = 0.0,
)

@Serializable
data class SiteChannelGroup(
    @SerialName("group_key") val groupKey: String = "",
    @SerialName("group_name") val groupName: String = "",
    @SerialName("projection_disabled") val projectionDisabled: Boolean = false,
    @SerialName("projection_suspended") val projectionSuspended: Boolean = false,
    @SerialName("projection_suspend_reason") val projectionSuspendReason: String? = null,
    @SerialName("projection_suspended_at") val projectionSuspendedAt: Long? = null,
    @SerialName("model_sync_status") val modelSyncStatus: String = "idle",
    @SerialName("model_sync_message") val modelSyncMessage: String? = null,
    @SerialName("model_sync_authoritative") val modelSyncAuthoritative: Boolean = false,
    @SerialName("model_sync_model_count") val modelSyncModelCount: Int = 0,
    @SerialName("last_model_sync_at") val lastModelSyncAt: Long? = null,
    @SerialName("last_model_sync_success_at") val lastModelSyncSuccessAt: Long? = null,
    @SerialName("model_sync_failure_count") val modelSyncFailureCount: Int = 0,
    @SerialName("key_count") val keyCount: Int = 0,
    @SerialName("enabled_key_count") val enabledKeyCount: Int = 0,
    @SerialName("masked_pending_key_count") val maskedPendingKeyCount: Int = 0,
    @SerialName("has_keys") val hasKeys: Boolean = false,
    @SerialName("has_projected_channel") val hasProjectedChannel: Boolean = false,
    @SerialName("projected_channel_ids") val projectedChannelIds: List<Int> = emptyList(),
    @SerialName("projected_channels") val projectedChannels: List<SiteProjectedChannelSettings> = emptyList(),
    @SerialName("source_keys") val sourceKeys: List<SiteSourceKey> = emptyList(),
    @SerialName("projected_keys") val projectedKeys: List<SiteProjectedKey> = emptyList(),
    val models: List<SiteChannelModel> = emptyList(),
)

@Serializable
data class SiteManualModelAddEntry(
    @SerialName("model_name") val modelName: String,
    @SerialName("route_type") val routeType: String,
)

@Serializable
data class SiteManualModelAddRequest(
    @SerialName("group_key") val groupKey: String,
    val models: List<SiteManualModelAddEntry>,
)

@Serializable
data class SiteManualModelDeleteRequest(
    @SerialName("group_key") val groupKey: String,
    @SerialName("model_name") val modelName: String,
)

@Serializable
data class SiteGroupProjectionUpdateRequest(
    @SerialName("group_key") val groupKey: String,
    @SerialName("projection_disabled") val projectionDisabled: Boolean,
)

@Serializable
data class SiteChannelAccount(
    @SerialName("site_id") val siteId: Int = 0,
    @SerialName("account_id") val accountId: Int = 0,
    @SerialName("account_name") val accountName: String = "",
    val enabled: Boolean = true,
    @SerialName("auto_sync") val autoSync: Boolean = false,
    @SerialName("group_count") val groupCount: Int = 0,
    @SerialName("model_count") val modelCount: Int = 0,
    val groups: List<SiteChannelGroup> = emptyList(),
    @SerialName("route_summaries") val routeSummaries: List<SiteRouteSummary> = emptyList(),
)

@Serializable
data class SiteChannelCard(
    @SerialName("site_id") val siteId: Int = 0,
    @SerialName("site_name") val siteName: String = "",
    @SerialName("base_url") val baseUrl: String = "",
    val platform: String = SitePlatform.NewApi,
    val enabled: Boolean = true,
    @SerialName("account_count") val accountCount: Int = 0,
    val accounts: List<SiteChannelAccount> = emptyList(),
)
