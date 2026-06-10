package com.elykia.octopus.feature.sitechannel

import com.elykia.octopus.core.data.model.SiteChannelCard
import com.elykia.octopus.core.data.model.SiteChannelGroup
import com.elykia.octopus.core.data.model.SiteChannelModel
import com.elykia.octopus.core.data.model.SiteModelRouteType

internal enum class SiteChannelFilter {
    All,
    Attention,
    WithHistory,
    Disabled,
}

internal enum class SiteChannelSort {
    NameAsc,
    NameDesc,
    ModelsDesc,
    Attention,
}

internal fun filterAndSortSiteChannelCards(
    cards: List<SiteChannelCard>,
    query: String,
    filter: SiteChannelFilter,
    sort: SiteChannelSort,
): List<SiteChannelCard> {
    val normalizedQuery = query.trim()
    return cards
        .filter { card -> normalizedQuery.isBlank() || card.matchesSiteChannelQuery(normalizedQuery) }
        .filter { card -> card.matchesSiteChannelFilter(filter) }
        .sortedWith(siteChannelCardComparator(sort))
}

internal fun SiteChannelCard.matchesSiteChannelQuery(query: String): Boolean {
    if (siteName.containsQuery(query) || baseUrl.containsQuery(query) || platform.containsQuery(query)) return true
    return accounts.any { account ->
        account.accountName.containsQuery(query) ||
            account.groups.any { group ->
                group.groupName.containsQuery(query) ||
                    group.groupKey.containsQuery(query) ||
                    group.modelSyncStatus.containsQuery(query) ||
                    group.projectedChannels.any { it.channelName.containsQuery(query) || it.routeType.containsQuery(query) } ||
                    group.sourceKeys.any {
                        it.name.containsQuery(query) ||
                            it.tokenMasked.containsQuery(query) ||
                            it.groupName.containsQuery(query) ||
                            it.valueStatus.containsQuery(query)
                    } ||
                    group.projectedKeys.any {
                        it.channelName.containsQuery(query) ||
                            it.channelKeyMasked.containsQuery(query) ||
                            it.remark.containsQuery(query)
                    } ||
                    group.models.any {
                        it.modelName.containsQuery(query) ||
                            it.source.containsQuery(query) ||
                            it.routeType.containsQuery(query) ||
                            it.routeSource.containsQuery(query)
                    }
            }
    }
}

private fun SiteChannelCard.matchesSiteChannelFilter(filter: SiteChannelFilter): Boolean = when (filter) {
    SiteChannelFilter.All -> true
    SiteChannelFilter.Attention -> attentionScore() > 0
    SiteChannelFilter.WithHistory -> accounts.any { account ->
        account.groups.any { group -> group.models.any { model -> model.hasHistory() } }
    }
    SiteChannelFilter.Disabled -> accounts.any { account ->
        account.groups.any { group -> group.models.any { model -> model.disabled } }
    }
}

private fun siteChannelCardComparator(sort: SiteChannelSort): Comparator<SiteChannelCard> = when (sort) {
    SiteChannelSort.NameAsc -> compareBy<SiteChannelCard> { it.siteDisplayName().lowercase() }.thenBy { it.siteId }
    SiteChannelSort.NameDesc -> compareByDescending<SiteChannelCard> { it.siteDisplayName().lowercase() }.thenBy { it.siteId }
    SiteChannelSort.ModelsDesc -> compareByDescending<SiteChannelCard> { it.siteChannelModelCount() }
        .thenBy { it.siteDisplayName().lowercase() }
        .thenBy { it.siteId }
    SiteChannelSort.Attention -> compareByDescending<SiteChannelCard> { it.attentionScore() }
        .thenBy { it.siteDisplayName().lowercase() }
        .thenBy { it.siteId }
}

private fun SiteChannelCard.siteDisplayName(): String =
    siteName.ifBlank { baseUrl.ifBlank { "#$siteId" } }

private fun SiteChannelCard.siteChannelModelCount(): Int {
    val nestedCount = accounts.sumOf { account -> account.groups.sumOf { group -> group.models.size } }
    if (nestedCount > 0) return nestedCount
    return accounts.sumOf { it.modelCount }
}

private fun SiteChannelCard.attentionScore(): Int =
    accounts.sumOf { account ->
        account.groups.sumOf { group ->
            group.groupAttentionScore() + group.models.count { model -> group.modelNeedsAttention(model) }
        }
    }

private fun SiteChannelGroup.groupAttentionScore(): Int {
    var score = 0
    if (projectionSuspended) score += 4
    if (!hasKeys) score += 3
    if (modelSyncStatus in STALE_MODEL_SYNC_STATUSES) score += 2
    if (maskedPendingKeyCount > 0 && enabledKeyCount == 0) score += 3
    return score
}

private fun SiteChannelGroup.modelNeedsAttention(model: SiteChannelModel): Boolean =
    !hasKeys ||
        (model.projectedChannelId ?: 0) <= 0 ||
        !isSupportedSiteRouteType(model.routeType)

private fun SiteChannelModel.hasHistory(): Boolean {
    val summary = history ?: return false
    return summary.successCount > 0 ||
        summary.failureCount > 0 ||
        summary.lastRequestAt != null ||
        summary.buckets.any { bucket -> bucket.success > 0 || bucket.failure > 0 }
}

private fun isSupportedSiteRouteType(routeType: String): Boolean = routeType in SUPPORTED_ROUTE_TYPES

private fun String.containsQuery(query: String): Boolean =
    contains(query, ignoreCase = true)

private val SUPPORTED_ROUTE_TYPES = setOf(
    SiteModelRouteType.OpenAiChat,
    SiteModelRouteType.OpenAiResponse,
    SiteModelRouteType.Anthropic,
    SiteModelRouteType.Gemini,
    SiteModelRouteType.Volcengine,
    SiteModelRouteType.OpenAiEmbedding,
)

private val STALE_MODEL_SYNC_STATUSES = setOf("stale", "failed", "unresolved")
