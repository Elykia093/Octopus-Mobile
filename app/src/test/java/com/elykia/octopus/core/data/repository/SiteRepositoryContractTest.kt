package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.model.SiteCredentialType
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.SiteApiService
import com.elykia.octopus.feature.site.toSiteAccountEditorValues
import com.elykia.octopus.feature.site.toUpdateRequest
import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import retrofit2.Retrofit

class SiteRepositoryContractTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun sitesTreatsSuccessfulNullDataAsEmptyList() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        {
                          "code": 200,
                          "message": "success",
                          "data": null
                        }
                        """.trimIndent(),
                    ),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.sites()

            assertThat(result).isEqualTo(AppResult.Success(emptyList<Any>()))
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/site/list")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun sitesNormalizesNullCollectionsAndHidesSecrets() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        {
                          "code": 200,
                          "message": "success",
                          "data": [
                            {
                              "id": 1,
                              "name": "Main",
                              "platform": "new-api",
                              "base_url": "https://api.example.com",
                              "enabled": true,
                              "proxy_mode": null,
                              "custom_header": null,
                              "accounts": [
                                {
                                  "id": 2,
                                  "site_id": 1,
                                  "name": "Primary",
                                  "credential_type": "access_token",
                                  "password": "plain-password",
                                  "access_token": "sk-secret-access",
                                  "api_key": "sk-secret-api",
                                  "refresh_token": "refresh-secret",
                                  "proxy_mode": null,
                                  "random_checkin": null,
                                  "checkin_interval_hours": null,
                                  "checkin_random_window_minutes": null,
                                  "last_sync_message": "Authorization: Bearer sk-secret-access",
                                  "last_checkin_message": "token=refresh-secret",
                                  "tokens": [
                                    {
                                      "id": 3,
                                      "site_account_id": 2,
                                      "name": "Default",
                                      "token": "sk-token-secret",
                                      "group_key": "default",
                                      "group_name": "default",
                                      "enabled": true,
                                      "source": "sync",
                                      "is_default": true
                                    }
                                  ],
                                  "user_groups": null,
                                  "models": null,
                                  "channel_bindings": null
                                }
                              ]
                            }
                          ]
                        }
                        """.trimIndent(),
                    ),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.sites()

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val site = (result as AppResult.Success).data.single()
            val account = site.accounts.orEmpty().single()
            assertThat(site.proxyMode).isEqualTo("direct")
            assertThat(site.customHeader).isEmpty()
            assertThat(account.proxyMode).isEqualTo("inherit")
            assertThat(account.password).isEmpty()
            assertThat(account.accessToken).isEmpty()
            assertThat(account.apiKey).isEmpty()
            assertThat(account.refreshToken).isEmpty()
            assertThat(account.lastSyncMessage).doesNotContain("sk-secret-access")
            assertThat(account.lastCheckinMessage).doesNotContain("refresh-secret")
            assertThat(account.tokens.orEmpty().single().token).isEmpty()
            assertThat(account.userGroups).isEmpty()
            assertThat(account.models).isEmpty()
            assertThat(account.channelBindings).isEmpty()
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun updateAccountDoesNotSendBlankHiddenSecrets() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        {
                          "code": 200,
                          "message": "success",
                          "data": {
                            "id": 2,
                            "site_id": 1,
                            "name": "Renamed",
                            "credential_type": "access_token",
                            "access_token": "",
                            "enabled": true,
                            "auto_sync": true,
                            "auto_checkin": true,
                            "random_checkin": false,
                            "checkin_interval_hours": 24,
                            "checkin_random_window_minutes": 120
                          }
                        }
                        """.trimIndent(),
                    ),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)
            val account = SiteAccount(
                id = 2,
                siteId = 1,
                name = "Primary",
                credentialType = SiteCredentialType.AccessToken,
                accessToken = "",
                enabled = true,
                autoSync = true,
                autoCheckin = true,
                randomCheckin = false,
                checkinIntervalHours = 24,
                checkinRandomWindowMinutes = 120,
            )
            val request = account.toSiteAccountEditorValues()
                .copy(name = "Renamed")
                .toUpdateRequest(account)

            val result = repository.updateAccount(request)

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val body = server.takeRequest().body.readUtf8()
            assertThat(body).contains(""""id":2""")
            assertThat(body).contains(""""site_id":1""")
            assertThat(body).contains(""""name":"Renamed"""")
            assertThat(body).doesNotContain("access_token")
            assertThat(body).doesNotContain("refresh_token")
            assertThat(body).doesNotContain("api_key")
            assertThat(body).doesNotContain("password")
        } finally {
            server.shutdown()
        }
    }

    private fun repositoryFor(server: MockWebServer): SiteRepository {
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SiteApiService::class.java)

        return SiteRepository(
            apiService = service,
            executor = NetworkExecutor(json),
            dispatchers = DispatchersProvider(),
        )
    }
}
