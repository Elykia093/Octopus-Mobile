package com.elykia.octopus.feature.site

import com.elykia.octopus.core.data.model.CustomHeader
import com.elykia.octopus.core.data.model.Site
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.model.SiteCredentialType
import com.elykia.octopus.core.data.model.SitePlatform
import com.google.common.truth.Truth.assertThat
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
}
