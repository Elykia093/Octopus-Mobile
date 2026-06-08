package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object SitePlatform {
    const val NewApi = "new-api"
    const val AnyRouter = "anyrouter"
    const val OneApi = "one-api"
    const val OneHub = "one-hub"
    const val DoneHub = "done-hub"
    const val Sub2Api = "sub2api"
    const val OpenAi = "openai"
    const val Claude = "claude"
    const val Gemini = "gemini"

    val entries: List<String> = listOf(NewApi, AnyRouter, OneApi, OneHub, DoneHub, Sub2Api, OpenAi, Claude, Gemini)
}

object SiteCredentialType {
    const val UsernamePassword = "username_password"
    const val AccessToken = "access_token"
    const val ApiKey = "api_key"

    val entries: List<String> = listOf(AccessToken, UsernamePassword, ApiKey)
}

object ProxyMode {
    const val Direct = "direct"
    const val System = "system"
    const val Pool = "pool"
    const val Inherit = "inherit"
}

@Serializable
data class SiteToken(
    val id: Int = 0,
    @SerialName("site_account_id") val siteAccountId: Int = 0,
    val name: String = "",
    val token: String = "",
    @SerialName("group_key") val groupKey: String = "",
    @SerialName("group_name") val groupName: String = "",
    val enabled: Boolean = true,
    val source: String = "",
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("last_sync_at") val lastSyncAt: String? = null,
)

@Serializable
data class SiteUserGroup(
    val id: Int = 0,
    @SerialName("site_account_id") val siteAccountId: Int = 0,
    @SerialName("group_key") val groupKey: String = "",
    val name: String = "",
    @SerialName("model_sync_status") val modelSyncStatus: String = "idle",
    @SerialName("model_sync_message") val modelSyncMessage: String? = null,
    @SerialName("model_sync_model_count") val modelSyncModelCount: Int = 0,
)

@Serializable
data class SiteModel(
    val id: Int = 0,
    @SerialName("site_account_id") val siteAccountId: Int = 0,
    @SerialName("model_name") val modelName: String = "",
    val source: String = "",
)

@Serializable
data class SiteChannelBinding(
    val id: Int = 0,
    @SerialName("site_id") val siteId: Int = 0,
    @SerialName("site_account_id") val siteAccountId: Int = 0,
    @SerialName("site_user_group_id") val siteUserGroupId: Int? = null,
    @SerialName("group_key") val groupKey: String = "",
    @SerialName("channel_id") val channelId: Int = 0,
)

@Serializable
data class SiteAccount(
    val id: Int = 0,
    @SerialName("site_id") val siteId: Int = 0,
    val name: String = "",
    @SerialName("credential_type") val credentialType: String = SiteCredentialType.AccessToken,
    val username: String = "",
    val password: String = "",
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("api_key") val apiKey: String = "",
    @SerialName("refresh_token") val refreshToken: String = "",
    @SerialName("token_expires_at") val tokenExpiresAt: Long = 0,
    @SerialName("platform_user_id") val platformUserId: Int? = null,
    @SerialName("proxy_mode") val proxyMode: String = ProxyMode.Inherit,
    @SerialName("proxy_config_id") val proxyConfigId: Int? = null,
    val enabled: Boolean = true,
    @SerialName("auto_sync") val autoSync: Boolean = true,
    @SerialName("auto_checkin") val autoCheckin: Boolean = true,
    @SerialName("random_checkin") val randomCheckin: Boolean = false,
    @SerialName("checkin_interval_hours") val checkinIntervalHours: Int = 24,
    @SerialName("checkin_random_window_minutes") val checkinRandomWindowMinutes: Int = 120,
    @SerialName("next_auto_checkin_at") val nextAutoCheckinAt: String? = null,
    @SerialName("last_sync_at") val lastSyncAt: String? = null,
    @SerialName("last_checkin_at") val lastCheckinAt: String? = null,
    @SerialName("last_sync_status") val lastSyncStatus: String = "idle",
    @SerialName("last_checkin_status") val lastCheckinStatus: String = "idle",
    @SerialName("last_sync_message") val lastSyncMessage: String = "",
    @SerialName("last_checkin_message") val lastCheckinMessage: String = "",
    val balance: Double = 0.0,
    @SerialName("balance_used") val balanceUsed: Double = 0.0,
    @SerialName("today_income") val todayIncome: Double = 0.0,
    val tokens: List<SiteToken>? = null,
    @SerialName("user_groups") val userGroups: List<SiteUserGroup>? = null,
    val models: List<SiteModel>? = null,
    @SerialName("channel_bindings") val channelBindings: List<SiteChannelBinding>? = null,
)

@Serializable
data class Site(
    val id: Int = 0,
    val name: String = "",
    val platform: String = SitePlatform.NewApi,
    @SerialName("base_url") val baseUrl: String = "",
    val enabled: Boolean = true,
    @SerialName("proxy_mode") val proxyMode: String = ProxyMode.Direct,
    @SerialName("proxy_config_id") val proxyConfigId: Int? = null,
    @SerialName("external_checkin_url") val externalCheckinUrl: String? = null,
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("global_weight") val globalWeight: Double = 1.0,
    @SerialName("custom_header") val customHeader: List<CustomHeader>? = null,
    val archived: Boolean = false,
    @SerialName("archived_at") val archivedAt: String? = null,
    val accounts: List<SiteAccount>? = null,
)

@Serializable
data class SiteCreateRequest(
    val name: String,
    val platform: String,
    @SerialName("base_url") val baseUrl: String,
    val enabled: Boolean = true,
    @SerialName("proxy_mode") val proxyMode: String = ProxyMode.Direct,
    @SerialName("proxy_config_id") val proxyConfigId: Int? = null,
    @SerialName("external_checkin_url") val externalCheckinUrl: String? = null,
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("global_weight") val globalWeight: Double = 1.0,
    @SerialName("custom_header") val customHeader: List<CustomHeader> = emptyList(),
)

@Serializable
data class SiteUpdateRequest(
    val id: Int,
    val name: String? = null,
    val platform: String? = null,
    @SerialName("base_url") val baseUrl: String? = null,
    val enabled: Boolean? = null,
    @SerialName("proxy_mode") val proxyMode: String? = null,
    @SerialName("proxy_config_id") val proxyConfigId: Int? = null,
    @SerialName("external_checkin_url") val externalCheckinUrl: String? = null,
    @SerialName("is_pinned") val isPinned: Boolean? = null,
    @SerialName("sort_order") val sortOrder: Int? = null,
    @SerialName("global_weight") val globalWeight: Double? = null,
    @SerialName("custom_header") val customHeader: List<CustomHeader>? = null,
)

@Serializable
data class SiteAccountCreateRequest(
    @SerialName("site_id") val siteId: Int,
    val name: String,
    @SerialName("credential_type") val credentialType: String,
    val username: String = "",
    val password: String = "",
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("api_key") val apiKey: String = "",
    @SerialName("refresh_token") val refreshToken: String = "",
    @SerialName("token_expires_at") val tokenExpiresAt: Long = 0,
    @SerialName("platform_user_id") val platformUserId: Int? = null,
    @SerialName("proxy_mode") val proxyMode: String = ProxyMode.Inherit,
    @SerialName("proxy_config_id") val proxyConfigId: Int? = null,
    val enabled: Boolean = true,
    @SerialName("auto_sync") val autoSync: Boolean = true,
    @SerialName("auto_checkin") val autoCheckin: Boolean = true,
    @SerialName("random_checkin") val randomCheckin: Boolean = false,
    @SerialName("checkin_interval_hours") val checkinIntervalHours: Int = 24,
    @SerialName("checkin_random_window_minutes") val checkinRandomWindowMinutes: Int = 120,
)

@Serializable
data class SiteAccountUpdateRequest(
    val id: Int,
    @SerialName("site_id") val siteId: Int? = null,
    val name: String? = null,
    @SerialName("credential_type") val credentialType: String? = null,
    val username: String? = null,
    val password: String? = null,
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("api_key") val apiKey: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_expires_at") val tokenExpiresAt: Long? = null,
    @SerialName("platform_user_id") val platformUserId: Int? = null,
    @SerialName("proxy_mode") val proxyMode: String? = null,
    @SerialName("proxy_config_id") val proxyConfigId: Int? = null,
    val enabled: Boolean? = null,
    @SerialName("auto_sync") val autoSync: Boolean? = null,
    @SerialName("auto_checkin") val autoCheckin: Boolean? = null,
    @SerialName("random_checkin") val randomCheckin: Boolean? = null,
    @SerialName("checkin_interval_hours") val checkinIntervalHours: Int? = null,
    @SerialName("checkin_random_window_minutes") val checkinRandomWindowMinutes: Int? = null,
)

@Serializable
data class EntityEnableRequest(
    val id: Int,
    val enabled: Boolean,
)

@Serializable
data class SiteSyncResult(
    @SerialName("account_id") val accountId: Int = 0,
    @SerialName("site_id") val siteId: Int = 0,
    val status: String = "",
    val message: String = "",
    @SerialName("channel_count") val channelCount: Int = 0,
    @SerialName("group_count") val groupCount: Int = 0,
    @SerialName("token_count") val tokenCount: Int = 0,
    @SerialName("model_count") val modelCount: Int = 0,
)

@Serializable
data class SiteCheckinResult(
    @SerialName("account_id") val accountId: Int = 0,
    @SerialName("site_id") val siteId: Int = 0,
    val status: String = "",
    val message: String = "",
    val reward: String? = null,
)

@Serializable
data class AllApiHubImportResult(
    @SerialName("created_sites") val createdSites: Int = 0,
    @SerialName("reused_sites") val reusedSites: Int = 0,
    @SerialName("created_accounts") val createdAccounts: Int = 0,
    @SerialName("updated_accounts") val updatedAccounts: Int = 0,
    @SerialName("skipped_accounts") val skippedAccounts: Int = 0,
    @SerialName("scheduled_sync_accounts") val scheduledSyncAccounts: Int = 0,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class MetApiImportResult(
    @SerialName("created_sites") val createdSites: Int = 0,
    @SerialName("reused_sites") val reusedSites: Int = 0,
    @SerialName("created_accounts") val createdAccounts: Int = 0,
    @SerialName("updated_accounts") val updatedAccounts: Int = 0,
    @SerialName("skipped_accounts") val skippedAccounts: Int = 0,
    @SerialName("imported_tokens") val importedTokens: Int = 0,
    @SerialName("imported_groups") val importedGroups: Int = 0,
    @SerialName("imported_models") val importedModels: Int = 0,
    @SerialName("disabled_models") val disabledModels: Int = 0,
    val warnings: List<String> = emptyList(),
)
