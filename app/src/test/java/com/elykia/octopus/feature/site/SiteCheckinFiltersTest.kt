package com.elykia.octopus.feature.site

import com.elykia.octopus.core.data.model.Site
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.model.SitePlatform
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class SiteCheckinFiltersTest {
    private val today = LocalDate.of(2026, 6, 10)

    @Test
    fun deriveCheckinStatusMatchesWebRules() {
        val enabledSite = site(
            platform = SitePlatform.NewApi,
            enabled = true,
            accounts = emptyList(),
        )

        assertThat(
            deriveSiteCheckinStatus(
                enabledSite,
                account(lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "success"),
                today,
            ),
        ).isEqualTo(SiteCheckinStatus.Success)
        assertThat(
            deriveSiteCheckinStatus(
                enabledSite,
                account(lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "skipped"),
                today,
            ),
        ).isEqualTo(SiteCheckinStatus.Failed)
        assertThat(
            deriveSiteCheckinStatus(
                enabledSite,
                account(lastCheckinAt = "2026-06-09T23:30:00", lastCheckinStatus = "success"),
                today,
            ),
        ).isEqualTo(SiteCheckinStatus.Idle)
        assertThat(
            deriveSiteCheckinStatus(
                enabledSite.copy(enabled = false),
                account(enabled = false, autoCheckin = false),
                today,
            ),
        ).isEqualTo(SiteCheckinStatus.Disabled)
        assertThat(
            deriveSiteCheckinStatus(
                enabledSite.copy(platform = SitePlatform.OpenAi),
                account(autoCheckin = true),
                today,
            ),
        ).isNull()
        assertThat(
            deriveSiteCheckinStatus(
                enabledSite,
                account(autoCheckin = false),
                today,
            ),
        ).isNull()
    }

    @Test
    fun summaryCountsOnlyAccountsThatParticipateInCheckinStatus() {
        val sites = listOf(
            site(
                name = "Main",
                accounts = listOf(
                    account(lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "success"),
                    account(lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "failed"),
                    account(lastCheckinAt = "2026-06-09T08:30:00", lastCheckinStatus = "success"),
                    account(enabled = false),
                    account(autoCheckin = false),
                ),
            ),
            site(
                name = "Unsupported",
                platform = SitePlatform.Gemini,
                accounts = listOf(account(lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "success")),
            ),
        )

        val summary = buildSiteCheckinSummary(sites, today)

        assertThat(summary.total).isEqualTo(4)
        assertThat(summary.success).isEqualTo(1)
        assertThat(summary.failed).isEqualTo(1)
        assertThat(summary.idle).isEqualTo(1)
        assertThat(summary.disabled).isEqualTo(1)
    }

    @Test
    fun filterKeepsOnlyMatchingAccountsForActiveCheckinStatus() {
        val sites = listOf(
            site(
                name = "Main",
                accounts = listOf(
                    account(name = "Good", lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "success"),
                    account(name = "Late", lastCheckinAt = "2026-06-09T08:30:00", lastCheckinStatus = "success"),
                ),
            ),
            site(
                name = "Other",
                accounts = listOf(account(name = "Bad", lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "failed")),
            ),
        )

        val result = filterSitesByQueryAndCheckin(
            sites = sites,
            query = "",
            filter = SiteCheckinFilter.Success,
            today = today,
        )

        assertThat(result.map { it.name }).containsExactly("Main")
        assertThat(result.single().accounts.orEmpty().map { it.name }).containsExactly("Good")
    }

    @Test
    fun accountCheckinEnabledRequiresSupportedPlatformAndAutoCheckin() {
        assertThat(
            accountHasSiteCheckinEnabled(
                site(platform = SitePlatform.NewApi, accounts = emptyList()),
                account(autoCheckin = true),
            ),
        ).isTrue()
        assertThat(
            accountHasSiteCheckinEnabled(
                site(platform = SitePlatform.Gemini, accounts = emptyList()),
                account(autoCheckin = true),
            ),
        ).isFalse()
        assertThat(
            accountHasSiteCheckinEnabled(
                site(platform = SitePlatform.NewApi, accounts = emptyList()),
                account(autoCheckin = false),
            ),
        ).isFalse()
    }

    @Test
    fun searchAndCheckinFilterFollowVisibleAccountScope() {
        val sites = listOf(
            site(
                name = "Main",
                accounts = listOf(
                    account(name = "Alpha", lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "success"),
                    account(name = "Beta", lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "failed"),
                ),
            ),
            site(
                name = "Archive",
                accounts = listOf(account(name = "Alpha Archive", lastCheckinAt = "2026-06-10T08:30:00", lastCheckinStatus = "failed")),
            ),
        )

        val accountQueryResult = filterSitesByQueryAndCheckin(
            sites = sites,
            query = "Alpha",
            filter = SiteCheckinFilter.Failed,
            today = today,
        )
        val siteQueryResult = filterSitesByQueryAndCheckin(
            sites = sites,
            query = "Main",
            filter = SiteCheckinFilter.Failed,
            today = today,
        )

        assertThat(accountQueryResult.map { it.name }).containsExactly("Archive")
        assertThat(accountQueryResult.single().accounts.orEmpty().map { it.name }).containsExactly("Alpha Archive")
        assertThat(siteQueryResult.map { it.name }).containsExactly("Main")
        assertThat(siteQueryResult.single().accounts.orEmpty().map { it.name }).containsExactly("Beta")
    }

    private fun site(
        name: String = "Site",
        platform: String = SitePlatform.NewApi,
        enabled: Boolean = true,
        accounts: List<SiteAccount>,
    ): Site = Site(
        id = name.hashCode(),
        name = name,
        platform = platform,
        baseUrl = "https://${name.lowercase()}.example.com",
        enabled = enabled,
        accounts = accounts,
    )

    private fun account(
        name: String = "Account",
        enabled: Boolean = true,
        autoCheckin: Boolean = true,
        lastCheckinAt: String? = null,
        lastCheckinStatus: String = "idle",
    ): SiteAccount = SiteAccount(
        id = name.hashCode(),
        siteId = 1,
        name = name,
        enabled = enabled,
        autoCheckin = autoCheckin,
        lastCheckinAt = lastCheckinAt,
        lastCheckinStatus = lastCheckinStatus,
    )
}
