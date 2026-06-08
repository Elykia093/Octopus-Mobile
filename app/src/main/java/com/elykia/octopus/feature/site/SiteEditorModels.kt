package com.elykia.octopus.feature.site

import com.elykia.octopus.core.data.model.CustomHeader
import com.elykia.octopus.core.data.model.ProxyMode
import com.elykia.octopus.core.data.model.Site
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.model.SiteAccountCreateRequest
import com.elykia.octopus.core.data.model.SiteAccountUpdateRequest
import com.elykia.octopus.core.data.model.SiteCreateRequest
import com.elykia.octopus.core.data.model.SiteCredentialType
import com.elykia.octopus.core.data.model.SitePlatform
import com.elykia.octopus.core.data.model.SiteUpdateRequest
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class SiteEditorValues(
    val name: String = "",
    val platform: String = SitePlatform.NewApi,
    val baseUrl: String = "",
    val enabled: Boolean = true,
    val proxyMode: String = ProxyMode.Direct,
    val externalCheckinUrl: String = "",
    val isPinned: Boolean = false,
    val sortOrder: String = "0",
    val globalWeight: String = "1",
    val customHeader: List<CustomHeader> = listOf(CustomHeader(headerKey = "", headerValue = "")),
)

data class SiteAccountEditorValues(
    val name: String = "",
    val credentialType: String = SiteCredentialType.AccessToken,
    val username: String = "",
    val password: String = "",
    val accessToken: String = "",
    val apiKey: String = "",
    val refreshToken: String = "",
    val tokenExpiresAt: String = "",
    val platformUserId: String = "",
    val proxyMode: String = ProxyMode.Inherit,
    val enabled: Boolean = true,
    val autoSync: Boolean = true,
    val autoCheckin: Boolean = true,
    val randomCheckin: Boolean = false,
    val checkinIntervalHours: String = "24",
    val checkinRandomWindowMinutes: String = "120",
)

enum class SiteImportSource {
    AllApiHub,
    MetApi,
}

fun Site?.toSiteEditorValues(): SiteEditorValues {
    val site = this ?: return SiteEditorValues()
    return SiteEditorValues(
        name = site.name,
        platform = site.platform,
        baseUrl = site.baseUrl,
        enabled = site.enabled,
        proxyMode = site.proxyMode,
        externalCheckinUrl = site.externalCheckinUrl.orEmpty(),
        isPinned = site.isPinned,
        sortOrder = site.sortOrder.toString(),
        globalWeight = site.globalWeight.toString(),
        customHeader = site.customHeader?.takeIf { it.isNotEmpty() } ?: listOf(CustomHeader(headerKey = "", headerValue = "")),
    )
}

fun SiteAccount?.toSiteAccountEditorValues(): SiteAccountEditorValues {
    val account = this ?: return SiteAccountEditorValues()
    return SiteAccountEditorValues(
        name = account.name,
        credentialType = account.credentialType,
        username = account.username,
        password = "",
        accessToken = "",
        apiKey = "",
        refreshToken = "",
        tokenExpiresAt = account.tokenExpiresAt.takeIf { it > 0L }?.toString().orEmpty(),
        platformUserId = account.platformUserId?.toString().orEmpty(),
        proxyMode = account.proxyMode,
        enabled = account.enabled,
        autoSync = account.autoSync,
        autoCheckin = account.autoCheckin,
        randomCheckin = account.randomCheckin,
        checkinIntervalHours = account.checkinIntervalHours.toString(),
        checkinRandomWindowMinutes = account.checkinRandomWindowMinutes.toString(),
    )
}

fun canSubmitSite(values: SiteEditorValues, submitting: Boolean): Boolean =
    !submitting &&
        values.name.isNotBlank() &&
        hasValidSiteUrl(values.baseUrl) &&
        parseSiteSortOrder(values.sortOrder) != null &&
        parseSiteGlobalWeight(values.globalWeight) != null &&
        values.customHeader.all { header ->
            val key = header.headerKey.trim()
            val value = header.headerValue.trim()
            key.isBlank() == value.isBlank()
        }

fun canSubmitSiteAccount(
    values: SiteAccountEditorValues,
    original: SiteAccount?,
    submitting: Boolean,
    sitePlatform: String? = null,
): Boolean {
    if (submitting || values.name.isBlank()) return false
    val sameCredentialType = original?.credentialType == values.credentialType
    val requiresNewSecret = original == null || !sameCredentialType
    val interval = parsePositiveInt(values.checkinIntervalHours)
    val window = parseNonNegativeInt(values.checkinRandomWindowMinutes)
    if (interval == null || interval !in 1..720 || window == null || window !in 0..1440) return false
    if (
        sitePlatform == SitePlatform.NewApi &&
        values.credentialType == SiteCredentialType.AccessToken &&
        parsePositiveInt(values.platformUserId) == null
    ) {
        return false
    }

    return when (values.credentialType) {
        SiteCredentialType.UsernamePassword ->
            values.username.isNotBlank() && (!requiresNewSecret || values.password.isNotBlank())
        SiteCredentialType.AccessToken ->
            !requiresNewSecret || values.accessToken.isNotBlank()
        SiteCredentialType.ApiKey ->
            !requiresNewSecret || values.apiKey.isNotBlank()
        else -> false
    }
}

fun SiteEditorValues.toCreateRequest(): SiteCreateRequest = SiteCreateRequest(
    name = name.trim(),
    platform = platform,
    baseUrl = baseUrl.trim(),
    enabled = enabled,
    proxyMode = proxyMode,
    externalCheckinUrl = externalCheckinUrl.trim().takeIf { it.isNotBlank() },
    isPinned = isPinned,
    sortOrder = parseSiteSortOrder(sortOrder) ?: 0,
    globalWeight = parseSiteGlobalWeight(globalWeight) ?: 1.0,
    customHeader = normalizedSiteHeaders(),
)

fun SiteEditorValues.toUpdateRequest(site: Site): SiteUpdateRequest = SiteUpdateRequest(
    id = site.id,
    name = name.trim().takeIf { it != site.name },
    platform = platform.takeIf { it != site.platform },
    baseUrl = baseUrl.trim().takeIf { it != site.baseUrl },
    enabled = enabled.takeIf { it != site.enabled },
    proxyMode = proxyMode.takeIf { it != site.proxyMode },
    externalCheckinUrl = externalCheckinUrl.trim().takeIf { it != site.externalCheckinUrl.orEmpty() },
    isPinned = isPinned.takeIf { it != site.isPinned },
    sortOrder = (parseSiteSortOrder(sortOrder) ?: 0).takeIf { it != site.sortOrder },
    globalWeight = (parseSiteGlobalWeight(globalWeight) ?: 1.0).takeIf { it != site.globalWeight },
    customHeader = normalizedSiteHeaders().takeIf { it != site.customHeader.orEmpty() },
)

fun SiteAccountEditorValues.toCreateRequest(siteId: Int): SiteAccountCreateRequest = SiteAccountCreateRequest(
    siteId = siteId,
    name = name.trim(),
    credentialType = credentialType,
    username = username.trim().takeIf { credentialType == SiteCredentialType.UsernamePassword } ?: "",
    password = password.trim().takeIf { credentialType == SiteCredentialType.UsernamePassword } ?: "",
    accessToken = accessToken.trim().takeIf { credentialType == SiteCredentialType.AccessToken } ?: "",
    apiKey = apiKey.trim().takeIf { credentialType == SiteCredentialType.ApiKey } ?: "",
    refreshToken = refreshToken.trim().takeIf { credentialType == SiteCredentialType.AccessToken } ?: "",
    tokenExpiresAt = if (credentialType == SiteCredentialType.AccessToken) parseNonNegativeLong(tokenExpiresAt) ?: 0L else 0L,
    platformUserId = if (credentialType == SiteCredentialType.AccessToken) parsePositiveInt(platformUserId) else null,
    proxyMode = proxyMode,
    enabled = enabled,
    autoSync = autoSync,
    autoCheckin = autoCheckin,
    randomCheckin = randomCheckin,
    checkinIntervalHours = parsePositiveInt(checkinIntervalHours) ?: 24,
    checkinRandomWindowMinutes = parseNonNegativeInt(checkinRandomWindowMinutes) ?: 120,
)

fun SiteAccountEditorValues.toUpdateRequest(account: SiteAccount): SiteAccountUpdateRequest {
    val credentialTypeChanged = credentialType != account.credentialType
    val parsedTokenExpiresAt = parseNonNegativeLong(tokenExpiresAt) ?: 0L
    val parsedPlatformUserId = parsePositiveInt(platformUserId)
    return SiteAccountUpdateRequest(
        id = account.id,
        siteId = account.siteId,
        name = name.trim().takeIf { it != account.name },
        credentialType = credentialType.takeIf { credentialTypeChanged },
        username = when {
            credentialType == SiteCredentialType.UsernamePassword ->
                username.trim().takeIf { credentialTypeChanged || it != account.username }
            credentialTypeChanged -> ""
            else -> null
        },
        password = when {
            credentialType == SiteCredentialType.UsernamePassword -> password.trim().takeIf { it.isNotBlank() }
            credentialTypeChanged -> ""
            else -> null
        },
        accessToken = when {
            credentialType == SiteCredentialType.AccessToken -> accessToken.trim().takeIf { it.isNotBlank() }
            credentialTypeChanged -> ""
            else -> null
        },
        apiKey = when {
            credentialType == SiteCredentialType.ApiKey -> apiKey.trim().takeIf { it.isNotBlank() }
            credentialTypeChanged -> ""
            else -> null
        },
        refreshToken = when {
            credentialType == SiteCredentialType.AccessToken -> refreshToken.trim().takeIf { it.isNotBlank() }
            credentialTypeChanged -> ""
            else -> null
        },
        tokenExpiresAt = when {
            credentialType == SiteCredentialType.AccessToken ->
                parsedTokenExpiresAt.takeIf { it != account.tokenExpiresAt }
            credentialTypeChanged -> 0L
            else -> null
        },
        platformUserId = when {
            credentialType == SiteCredentialType.AccessToken && parsedPlatformUserId != null && parsedPlatformUserId != account.platformUserId ->
                parsedPlatformUserId
            credentialType == SiteCredentialType.AccessToken && platformUserId.isBlank() && account.platformUserId != null ->
                0
            credentialTypeChanged -> 0
            else -> null
        },
        proxyMode = proxyMode.takeIf { it != account.proxyMode },
        enabled = enabled.takeIf { it != account.enabled },
        autoSync = autoSync.takeIf { it != account.autoSync },
        autoCheckin = autoCheckin.takeIf { it != account.autoCheckin },
        randomCheckin = randomCheckin.takeIf { it != account.randomCheckin },
        checkinIntervalHours = (parsePositiveInt(checkinIntervalHours) ?: 24).takeIf { it != account.checkinIntervalHours },
        checkinRandomWindowMinutes = (parseNonNegativeInt(checkinRandomWindowMinutes) ?: 120).takeIf { it != account.checkinRandomWindowMinutes },
    )
}

fun hasValidSiteUrl(value: String): Boolean {
    val parsed = value.trim().toHttpUrlOrNull() ?: return false
    return (parsed.scheme == "https" || parsed.scheme == "http") &&
        parsed.encodedUsername.isBlank() &&
        parsed.encodedPassword.isBlank() &&
        parsed.encodedQuery == null &&
        parsed.encodedFragment == null
}

fun SiteEditorValues.normalizedSiteHeaders(): List<CustomHeader> =
    customHeader
        .map { CustomHeader(headerKey = it.headerKey.trim(), headerValue = it.headerValue.trim()) }
        .filter { it.headerKey.isNotBlank() && it.headerValue.isNotBlank() }

fun parseSiteSortOrder(value: String): Int? = value.trim().ifBlank { "0" }.toIntOrNull()

fun parseSiteGlobalWeight(value: String): Double? =
    value.trim().ifBlank { "1" }.toDoubleOrNull()?.takeIf { it > 0.0 && !it.isNaN() && !it.isInfinite() }

fun parsePositiveInt(value: String): Int? = value.trim().takeIf { it.isNotBlank() }?.toIntOrNull()?.takeIf { it > 0 }

fun parseNonNegativeInt(value: String): Int? = value.trim().ifBlank { "0" }.toIntOrNull()?.takeIf { it >= 0 }

fun parseNonNegativeLong(value: String): Long? = value.trim().ifBlank { "0" }.toLongOrNull()?.takeIf { it >= 0L }
