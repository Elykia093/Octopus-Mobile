package com.elykia.octopus.feature.sitechannel

import com.elykia.octopus.core.data.model.SiteChannelAccount
import com.elykia.octopus.core.data.model.SiteChannelCard
import com.elykia.octopus.core.data.model.SiteChannelGroup
import com.elykia.octopus.core.data.model.SiteChannelModel
import com.elykia.octopus.core.data.model.SiteModelHistoryBucket
import com.elykia.octopus.core.data.model.SiteModelHistorySummary
import com.elykia.octopus.core.data.model.SiteModelRouteType
import com.elykia.octopus.core.data.model.SiteProjectedChannelSettings
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SiteChannelFiltersTest {
    @Test
    fun attentionFilterFindsMissingKeysAndUnsupportedRoutes() {
        val healthy = card(
            siteId = 1,
            siteName = "Healthy",
            group = group(
                hasKeys = true,
                models = listOf(model("gpt-4o", projectedChannelId = 9)),
            ),
        )
        val needsKeys = card(
            siteId = 2,
            siteName = "Needs Keys",
            group = group(
                hasKeys = false,
                models = listOf(model("claude")),
            ),
        )
        val unknownRoute = card(
            siteId = 3,
            siteName = "Unknown Route",
            group = group(
                hasKeys = true,
                models = listOf(model("legacy", routeType = SiteModelRouteType.Unknown, projectedChannelId = 7)),
            ),
        )

        val result = filterAndSortSiteChannelCards(
            cards = listOf(healthy, needsKeys, unknownRoute),
            query = "",
            filter = SiteChannelFilter.Attention,
            sort = SiteChannelSort.NameAsc,
        )

        assertThat(result.map { it.siteName }).containsExactly("Needs Keys", "Unknown Route").inOrder()
    }

    @Test
    fun withHistoryFilterUsesLoadedHistorySummary() {
        val withoutHistory = card(
            siteId = 1,
            siteName = "Cold",
            group = group(hasKeys = true, models = listOf(model("cold", projectedChannelId = 1))),
        )
        val withHistory = card(
            siteId = 2,
            siteName = "Warm",
            group = group(
                hasKeys = true,
                models = listOf(
                    model(
                        name = "warm",
                        projectedChannelId = 1,
                        history = SiteModelHistorySummary(
                            successCount = 1,
                            buckets = listOf(SiteModelHistoryBucket(time = 1, success = 1)),
                        ),
                    ),
                ),
            ),
        )

        val result = filterAndSortSiteChannelCards(
            cards = listOf(withoutHistory, withHistory),
            query = "",
            filter = SiteChannelFilter.WithHistory,
            sort = SiteChannelSort.NameAsc,
        )

        assertThat(result.map { it.siteName }).containsExactly("Warm")
    }

    @Test
    fun queryMatchesNestedGroupsModelsAndProjectedChannels() {
        val card = card(
            siteId = 1,
            siteName = "Hub",
            group = group(
                groupName = "Default",
                projectedChannels = listOf(SiteProjectedChannelSettings(channelId = 7, channelName = "Projected Claude")),
                models = listOf(model("claude-3-5-sonnet", projectedChannelId = 7)),
            ),
        )

        assertThat(card.matchesSiteChannelQuery("sonnet")).isTrue()
        assertThat(card.matchesSiteChannelQuery("Projected Claude")).isTrue()
        assertThat(card.matchesSiteChannelQuery("missing")).isFalse()
    }

    @Test
    fun sortCanPrioritizeModelCountOrAttention() {
        val large = card(
            siteId = 1,
            siteName = "Large",
            group = group(
                hasKeys = true,
                models = listOf(
                    model("a", projectedChannelId = 1),
                    model("b", projectedChannelId = 1),
                    model("c", projectedChannelId = 1),
                ),
            ),
        )
        val attention = card(
            siteId = 2,
            siteName = "Attention",
            group = group(
                hasKeys = false,
                models = listOf(model("z")),
            ),
        )

        assertThat(
            filterAndSortSiteChannelCards(
                cards = listOf(attention, large),
                query = "",
                filter = SiteChannelFilter.All,
                sort = SiteChannelSort.ModelsDesc,
            ).map { it.siteName },
        ).containsExactly("Large", "Attention").inOrder()

        assertThat(
            filterAndSortSiteChannelCards(
                cards = listOf(large, attention),
                query = "",
                filter = SiteChannelFilter.All,
                sort = SiteChannelSort.Attention,
            ).map { it.siteName },
        ).containsExactly("Attention", "Large").inOrder()
    }

    @Test
    fun accountGroupScopeAndModelSearchTrimVisibleModels() {
        val account = account(
            groups = listOf(
                group(
                    groupName = "Default",
                    models = listOf(
                        model("gpt-4o"),
                        model("claude-3-5-sonnet", routeType = SiteModelRouteType.Anthropic),
                    ),
                ),
                group(
                    groupName = "Team Gemini",
                    models = listOf(model("gemini-pro", routeType = SiteModelRouteType.Gemini)),
                ),
            ),
        )

        val modelResult = filterSiteChannelAccountGroups(
            account = account,
            groupScope = SITE_CHANNEL_GROUP_SCOPE_ALL,
            modelQuery = "sonnet",
        )
        val groupResult = filterSiteChannelAccountGroups(
            account = account,
            groupScope = "team gemini",
            modelQuery = "",
        )
        val routeResult = filterSiteChannelAccountGroups(
            account = account,
            groupScope = SITE_CHANNEL_GROUP_SCOPE_ALL,
            modelQuery = "gemini",
        )

        assertThat(modelResult.map { it.group.groupKey }).containsExactly("default")
        assertThat(modelResult.single().models.map { it.modelName }).containsExactly("claude-3-5-sonnet")
        assertThat(groupResult.single().group.groupKey).isEqualTo("team gemini")
        assertThat(groupResult.single().models.map { it.modelName }).containsExactly("gemini-pro")
        assertThat(routeResult.map { it.group.groupKey }).containsExactly("team gemini")
    }

    @Test
    fun accountModelSearchDropsEmptyGroups() {
        val account = account(
            groups = listOf(
                group(groupName = "Default", models = listOf(model("gpt-4o"))),
                group(groupName = "Archive", models = listOf(model("claude"))),
            ),
        )

        assertThat(
            filterSiteChannelAccountGroups(
                account = account,
                groupScope = SITE_CHANNEL_GROUP_SCOPE_ALL,
                modelQuery = "missing",
            ),
        ).isEmpty()
    }

    private fun account(
        groups: List<SiteChannelGroup>,
    ): SiteChannelAccount = SiteChannelAccount(
        accountId = 1,
        accountName = "Account",
        groups = groups,
    )

    private fun card(
        siteId: Int,
        siteName: String,
        group: SiteChannelGroup,
    ): SiteChannelCard = SiteChannelCard(
        siteId = siteId,
        siteName = siteName,
        accounts = listOf(
            SiteChannelAccount(
                accountId = siteId * 10,
                accountName = "$siteName account",
                groups = listOf(group),
            ),
        ),
    )

    private fun group(
        groupName: String = "default",
        hasKeys: Boolean = true,
        projectedChannels: List<SiteProjectedChannelSettings> = emptyList(),
        models: List<SiteChannelModel> = emptyList(),
    ): SiteChannelGroup = SiteChannelGroup(
        groupKey = groupName.lowercase(),
        groupName = groupName,
        hasKeys = hasKeys,
        projectedChannelIds = projectedChannels.map { it.channelId },
        projectedChannels = projectedChannels,
        models = models,
    )

    private fun model(
        name: String,
        routeType: String = SiteModelRouteType.OpenAiChat,
        projectedChannelId: Int? = null,
        history: SiteModelHistorySummary? = null,
    ): SiteChannelModel = SiteChannelModel(
        modelName = name,
        routeType = routeType,
        projectedChannelId = projectedChannelId,
        history = history,
    )
}
