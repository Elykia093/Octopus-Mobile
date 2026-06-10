package com.elykia.octopus.feature.sitechannel

import com.elykia.octopus.core.data.model.SiteChannelAccount
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

internal enum class SiteChannelAccountModelSort {
    ModelName,
    GroupName,
    RouteType,
    LastRequest,
}

internal const val SITE_CHANNEL_GROUP_SCOPE_ALL = "__site_channel_group_scope_all__"

internal data class SiteChannelVisibleGroup(
    val group: SiteChannelGroup,
    val models: List<SiteChannelModel>,
)

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

internal fun filterSiteChannelAccountGroups(
    account: SiteChannelAccount,
    groupScope: String,
    modelQuery: String,
    modelSort: SiteChannelAccountModelSort = SiteChannelAccountModelSort.ModelName,
): List<SiteChannelVisibleGroup> {
    val normalizedGroupScope = groupScope.trim()
    val normalizedQuery = modelQuery.trim()
    val visibleGroups = account.groups
        .asSequence()
        .filter { group -> normalizedGroupScope == SITE_CHANNEL_GROUP_SCOPE_ALL || group.groupKey == normalizedGroupScope }
        .mapNotNull { group ->
            val filteredModels = if (normalizedQuery.isBlank()) {
                group.models
            } else {
                group.models.filter { model -> model.matchesAccountModelQuery(group, normalizedQuery) }
            }
            if (filteredModels.isEmpty() && normalizedQuery.isNotBlank()) {
                null
            } else {
                SiteChannelVisibleGroup(
                    group = group,
                    models = filteredModels.sortedWith(siteChannelAccountModelComparator(modelSort)),
                )
            }
        }
        .toList()
    return if (modelSort == SiteChannelAccountModelSort.GroupName) {
        visibleGroups.sortedWith(compareBy<SiteChannelVisibleGroup> { it.group.groupDisplayName().lowercase() }.thenBy { it.group.groupKey })
    } else {
        visibleGroups
    }
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

private fun SiteChannelGroup.groupDisplayName(): String =
    groupName.ifBlank { groupKey.ifBlank { "#" } }

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

private fun siteChannelAccountModelComparator(sort: SiteChannelAccountModelSort): Comparator<SiteChannelModel> = when (sort) {
    SiteChannelAccountModelSort.ModelName,
    SiteChannelAccountModelSort.GroupName -> compareBy<SiteChannelModel> { it.modelName.lowercase() }.thenBy { it.routeType }
    SiteChannelAccountModelSort.RouteType -> compareBy<SiteChannelModel> { it.routeType }.thenBy { it.modelName.lowercase() }
    SiteChannelAccountModelSort.LastRequest -> compareByDescending<SiteChannelModel> { it.history?.lastRequestAt ?: Long.MIN_VALUE }
        .thenBy { it.modelName.lowercase() }
}

private fun SiteChannelModel.matchesAccountModelQuery(group: SiteChannelGroup, query: String): Boolean =
    modelName.containsQuery(query) ||
        source.containsQuery(query) ||
        routeType.containsQuery(query) ||
        routeSource.containsQuery(query) ||
        group.groupName.containsQuery(query) ||
        group.groupKey.containsQuery(query)

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
