package com.elykia.octopus.feature.site

import com.elykia.octopus.core.data.model.Site
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.model.SitePlatform
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal enum class SiteCheckinFilter {
    All,
    Success,
    Failed,
    Idle,
    Disabled,
}

internal enum class SiteCheckinStatus {
    Success,
    Failed,
    Idle,
    Disabled,
}

internal data class SiteCheckinSummary(
    val total: Int = 0,
    val success: Int = 0,
    val failed: Int = 0,
    val idle: Int = 0,
    val disabled: Int = 0,
)

internal fun deriveSiteCheckinStatus(
    site: Site,
    account: SiteAccount,
    today: LocalDate = LocalDate.now(),
): SiteCheckinStatus? {
    if (!site.enabled || !account.enabled) {
        return SiteCheckinStatus.Disabled
    }
    if (!accountHasSiteCheckinEnabled(site, account)) {
        return null
    }
    if (!happenedOnDate(account.lastCheckinAt, today)) {
        return SiteCheckinStatus.Idle
    }
    return when (account.lastCheckinStatus.ifBlank { "idle" }) {
        "success" -> SiteCheckinStatus.Success
        "failed", "skipped" -> SiteCheckinStatus.Failed
        else -> SiteCheckinStatus.Idle
    }
}

internal fun buildSiteCheckinSummary(
    sites: List<Site>,
    today: LocalDate = LocalDate.now(),
): SiteCheckinSummary {
    var summary = SiteCheckinSummary()
    sites.forEach { site ->
        site.accounts.orEmpty().forEach { account ->
            when (deriveSiteCheckinStatus(site, account, today)) {
                SiteCheckinStatus.Success -> summary = summary.copy(total = summary.total + 1, success = summary.success + 1)
                SiteCheckinStatus.Failed -> summary = summary.copy(total = summary.total + 1, failed = summary.failed + 1)
                SiteCheckinStatus.Idle -> summary = summary.copy(total = summary.total + 1, idle = summary.idle + 1)
                SiteCheckinStatus.Disabled -> summary = summary.copy(total = summary.total + 1, disabled = summary.disabled + 1)
                null -> Unit
            }
        }
    }
    return summary
}

internal fun filterSitesByQueryAndCheckin(
    sites: List<Site>,
    query: String,
    filter: SiteCheckinFilter,
    today: LocalDate = LocalDate.now(),
): List<Site> {
    val normalizedQuery = query.trim()
    val hasQuery = normalizedQuery.isNotBlank()
    val hasCheckinFilter = filter != SiteCheckinFilter.All

    return sites.mapNotNull { site ->
        val accounts = site.accounts.orEmpty()
        val accountsAfterCheckin = if (hasCheckinFilter) {
            accounts.filter { account -> accountMatchesSiteCheckinFilter(site, account, filter, today) }
        } else {
            accounts
        }
        val siteMatchesQuery = !hasQuery || site.matchesSiteQuery(normalizedQuery)
        val visibleAccounts = if (hasQuery && !siteMatchesQuery) {
            accountsAfterCheckin.filter { account -> account.name.contains(normalizedQuery, ignoreCase = true) }
        } else {
            accountsAfterCheckin
        }
        val visible = when {
            hasCheckinFilter -> visibleAccounts.isNotEmpty()
            !hasQuery -> true
            siteMatchesQuery -> true
            else -> visibleAccounts.isNotEmpty()
        }
        if (visible) site.copy(accounts = visibleAccounts) else null
    }
}

private fun accountMatchesSiteCheckinFilter(
    site: Site,
    account: SiteAccount,
    filter: SiteCheckinFilter,
    today: LocalDate,
): Boolean {
    if (filter == SiteCheckinFilter.All) return true
    val status = deriveSiteCheckinStatus(site, account, today) ?: return false
    return status == when (filter) {
        SiteCheckinFilter.Success -> SiteCheckinStatus.Success
        SiteCheckinFilter.Failed -> SiteCheckinStatus.Failed
        SiteCheckinFilter.Idle -> SiteCheckinStatus.Idle
        SiteCheckinFilter.Disabled -> SiteCheckinStatus.Disabled
        SiteCheckinFilter.All -> SiteCheckinStatus.Idle
    }
}

internal fun accountHasSiteCheckinEnabled(site: Site, account: SiteAccount): Boolean =
    sitePlatformSupportsCheckin(site.platform) && account.autoCheckin

internal fun sitePlatformSupportsCheckin(platform: String): Boolean = platform !in NO_CHECKIN_PLATFORMS

private fun Site.matchesSiteQuery(query: String): Boolean =
    name.contains(query, ignoreCase = true) ||
        baseUrl.contains(query, ignoreCase = true) ||
        platform.contains(query, ignoreCase = true)

private fun happenedOnDate(value: String?, today: LocalDate): Boolean {
    val date = parseCheckinDate(value) ?: return false
    return date.year > 1 && date == today
}

private fun parseCheckinDate(value: String?): LocalDate? {
    val text = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val zone = ZoneId.systemDefault()
    return runCatching { Instant.parse(text).atZone(zone).toLocalDate() }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(text).atZoneSameInstant(zone).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(text).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDate.parse(text.take(10)) }.getOrNull()
}

private val NO_CHECKIN_PLATFORMS = setOf(
    SitePlatform.DoneHub,
    SitePlatform.Sub2Api,
    SitePlatform.OpenAi,
    SitePlatform.Claude,
    SitePlatform.Gemini,
)
