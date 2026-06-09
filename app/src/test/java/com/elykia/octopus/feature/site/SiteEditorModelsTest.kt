package com.elykia.octopus.feature.site

import com.elykia.octopus.core.data.model.CustomHeader
import com.elykia.octopus.core.data.model.ProxyMode
import com.elykia.octopus.core.data.model.Site
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.model.SiteCredentialType
import com.elykia.octopus.core.data.model.SitePlatform
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.Json
import org.junit.Test

class SiteEditorModelsTest {
    @Test
    fun siteBaseUrlAcceptsHttpHttpsWithoutSensitiveParts() {
        assertThat(hasValidSiteUrl("https://api.example.com/v1")).isTrue()
        assertThat(hasValidSiteUrl("http://localhost:3000")).isTrue()
        assertThat(hasValidSiteUrl("ftp://api.example.com")).isFalse()
        assertThat(hasValidSiteUrl("https://user:pass@api.example.com")).isFalse()
        assertThat(hasValidSiteUrl("https://api.example.com?token=secret")).isFalse()
        assertThat(hasValidSiteUrl("https://api.example.com#secret")).isFalse()
    }

    @Test
    fun newApiAccessTokenAccountRequiresPlatformUserId() {
        val values = SiteAccountEditorValues(
            name = "Main",
            credentialType = SiteCredentialType.AccessToken,
            accessToken = "access-token",
        )

        assertThat(canSubmitSiteAccount(values, original = null, submitting = false, sitePlatform = SitePlatform.NewApi)).isFalse()
        assertThat(
            canSubmitSiteAccount(
                values.copy(platformUserId = "42"),
                original = null,
                submitting = false,
                sitePlatform = SitePlatform.NewApi,
            )
        ).isTrue()
        assertThat(canSubmitSiteAccount(values, original = null, submitting = false, sitePlatform = SitePlatform.OpenAi)).isTrue()
    }

    @Test
    fun existingAccountCanSaveWithoutReplacingHiddenSecret() {
        val account = SiteAccount(
            id = 8,
            siteId = 3,
            name = "Main",
            credentialType = SiteCredentialType.AccessToken,
            accessToken = "",
            tokenExpiresAt = 1234L,
            platformUserId = 42,
        )
        val values = account.toSiteAccountEditorValues()

        assertThat(canSubmitSiteAccount(values, original = account, submitting = false, sitePlatform = SitePlatform.NewApi)).isTrue()

        val request = values.toUpdateRequest(account)
        assertThat(request.accessToken).isNull()
        assertThat(request.refreshToken).isNull()
        assertThat(request.apiKey).isNull()
        assertThat(request.password).isNull()
        assertThat(request.tokenExpiresAt).isNull()
        assertThat(request.platformUserId).isNull()
    }

    @Test
    fun changingCredentialTypeClearsOldCredentialFieldsAndRequiresReplacement() {
        val account = SiteAccount(
            id = 8,
            siteId = 3,
            name = "Main",
            credentialType = SiteCredentialType.AccessToken,
            accessToken = "",
            platformUserId = 42,
            tokenExpiresAt = 1234L,
        )
        val blankApiKeyValues = SiteAccountEditorValues(
            name = "Main",
            credentialType = SiteCredentialType.ApiKey,
        )

        assertThat(canSubmitSiteAccount(blankApiKeyValues, original = account, submitting = false)).isFalse()

        val request = blankApiKeyValues.copy(apiKey = "sk-new").toUpdateRequest(account)
        assertThat(request.credentialType).isEqualTo(SiteCredentialType.ApiKey)
        assertThat(request.apiKey).isEqualTo("sk-new")
        assertThat(request.username).isEqualTo("")
        assertThat(request.password).isEqualTo("")
        assertThat(request.accessToken).isEqualTo("")
        assertThat(request.refreshToken).isEqualTo("")
        assertThat(request.tokenExpiresAt).isEqualTo(0L)
        assertThat(request.platformUserId).isEqualTo(0)
    }

    @Test
    fun siteUpdateCanClearExternalCheckinUrlAndHeaders() {
        val site = Site(
            id = 2,
            name = "Main",
            platform = SitePlatform.NewApi,
            baseUrl = "https://api.example.com",
            externalCheckinUrl = "https://api.example.com/checkin",
            customHeader = listOf(CustomHeader(headerKey = "X-Test", headerValue = "secret")),
        )
        val request = site.toSiteEditorValues()
            .copy(
                externalCheckinUrl = " ",
                customHeader = listOf(CustomHeader(headerKey = "", headerValue = "")),
            )
            .toUpdateRequest(site)

        assertThat(request.externalCheckinUrl).isEqualTo("")
        assertThat(request.customHeader).isEmpty()
    }

    @Test
    fun sitePoolProxyRequiresConfigAndSendsProxyConfigId() {
        val values = SiteEditorValues(
            name = "Main",
            baseUrl = "https://api.example.com",
            proxyMode = ProxyMode.Pool,
        )

        assertThat(canSubmitSite(values, submitting = false)).isFalse()
        assertThat(canSubmitSite(values.copy(proxyConfigId = 5), submitting = false)).isTrue()

        val create = values.copy(proxyConfigId = 5).toCreateRequest()
        val update = values.copy(proxyConfigId = 5).toUpdateRequest(
            Site(
                id = 1,
                name = "Main",
                baseUrl = "https://api.example.com",
                proxyMode = ProxyMode.Direct,
            ),
        )

        assertThat(create.proxyMode).isEqualTo(ProxyMode.Pool)
        assertThat(create.proxyConfigId).isEqualTo(5)
        assertThat(update.proxyMode).isEqualTo(ProxyMode.Pool)
        assertThat(update.proxyConfigId).isEqualTo(JsonPrimitive(5))
    }

    @Test
    fun accountPoolProxyRequiresConfigAndSendsProxyConfigId() {
        val account = SiteAccount(
            id = 8,
            siteId = 3,
            name = "Main",
            credentialType = SiteCredentialType.AccessToken,
            proxyMode = ProxyMode.Direct,
        )
        val values = account.toSiteAccountEditorValues().copy(proxyMode = ProxyMode.Pool)

        assertThat(canSubmitSiteAccount(values, original = account, submitting = false)).isFalse()
        assertThat(canSubmitSiteAccount(values.copy(proxyConfigId = 9), original = account, submitting = false)).isTrue()

        val create = SiteAccountEditorValues(
            name = "New",
            credentialType = SiteCredentialType.AccessToken,
            accessToken = "access-token",
            proxyMode = ProxyMode.Pool,
            proxyConfigId = 9,
        ).toCreateRequest(siteId = 3)
        val update = values.copy(proxyConfigId = 9).toUpdateRequest(account)

        assertThat(create.proxyMode).isEqualTo(ProxyMode.Pool)
        assertThat(create.proxyConfigId).isEqualTo(9)
        assertThat(update.proxyMode).isEqualTo(ProxyMode.Pool)
        assertThat(update.proxyConfigId).isEqualTo(JsonPrimitive(9))
    }

    @Test
    fun updateClearsProxyConfigIdWhenLeavingPoolMode() {
        val site = Site(
            id = 1,
            name = "Main",
            baseUrl = "https://api.example.com",
            proxyMode = ProxyMode.Pool,
            proxyConfigId = 5,
        )
        val account = SiteAccount(
            id = 8,
            siteId = 3,
            name = "Main",
            credentialType = SiteCredentialType.AccessToken,
            proxyMode = ProxyMode.Pool,
            proxyConfigId = 9,
        )

        val siteRequest = site.toSiteEditorValues()
            .copy(proxyMode = ProxyMode.Direct, proxyConfigId = null)
            .toUpdateRequest(site)
        val accountRequest = account.toSiteAccountEditorValues()
            .copy(proxyMode = ProxyMode.Inherit, proxyConfigId = null)
            .toUpdateRequest(account)

        assertThat(siteRequest.proxyMode).isEqualTo(ProxyMode.Direct)
        assertThat(siteRequest.proxyConfigId).isEqualTo(JsonNull)
        assertThat(accountRequest.proxyMode).isEqualTo(ProxyMode.Inherit)
        assertThat(accountRequest.proxyConfigId).isEqualTo(JsonNull)

        val json = Json { explicitNulls = false }
        assertThat(json.encodeToString(siteRequest)).contains(""""proxy_config_id":null""")
        assertThat(json.encodeToString(accountRequest)).contains(""""proxy_config_id":null""")
    }
}
