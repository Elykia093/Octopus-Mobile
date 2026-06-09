package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.SiteChannelKeyCreateRequest
import com.elykia.octopus.core.data.model.SiteGroupProjectionUpdateRequest
import com.elykia.octopus.core.data.model.SiteManualModelAddEntry
import com.elykia.octopus.core.data.model.SiteManualModelAddRequest
import com.elykia.octopus.core.data.model.SiteManualModelDeleteRequest
import com.elykia.octopus.core.data.model.SiteModelDisableUpdateRequest
import com.elykia.octopus.core.data.model.SiteModelRouteUpdateRequest
import com.elykia.octopus.core.data.model.SiteProjectedChannelSettingsUpdateRequest
import com.elykia.octopus.core.data.model.SiteSourceKeyAddRequest
import com.elykia.octopus.core.data.model.SiteSourceKeyUpdateItem
import com.elykia.octopus.core.data.model.SiteSourceKeyUpdateRequest
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.SiteChannelApiService
import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import retrofit2.Retrofit

class SiteChannelRepositoryContractTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun siteChannelsTreatsNullDataAsEmptyList() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(200).setBody("""{"code":200,"message":"success","data":null}"""))
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.siteChannels()

            assertThat(result).isEqualTo(AppResult.Success(emptyList<Any>()))
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/site-channel/list?include_history=true")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun siteChannelsReadsProjectionSummary() = runBlocking {
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
                              "site_id": 1,
                              "site_name": "OpenAI Hub",
                              "base_url": "https://hub.example.com",
                              "platform": "new-api",
                              "enabled": true,
                              "account_count": 1,
                              "accounts": [
                                {
                                  "site_id": 1,
                                  "account_id": 2,
                                  "account_name": "Primary",
                                  "enabled": true,
                                  "auto_sync": true,
                                  "group_count": 1,
                                  "model_count": 2,
                                  "route_summaries": [
                                    { "route_type": "openai_chat", "count": 2 }
                                  ],
                                  "groups": [
                                    {
                                      "group_key": "default",
                                      "group_name": "Default",
                                      "projection_disabled": false,
                                      "projection_suspended": false,
                                      "model_sync_status": "synced",
                                      "model_sync_authoritative": true,
                                      "model_sync_model_count": 2,
                                      "key_count": 1,
                                      "enabled_key_count": 1,
                                      "masked_pending_key_count": 0,
                                      "has_keys": true,
                                      "has_projected_channel": true,
                                      "projected_channel_ids": [3],
                                      "projected_channels": [
                                        {
                                          "channel_id": 3,
                                          "channel_name": "Projected OpenAI",
                                          "route_type": "openai_chat",
                                          "auto_group": 1,
                                          "effective_auto_group": 1,
                                          "param_override": "",
                                          "global_override": false
                                        }
                                      ],
                                      "source_keys": [
                                        {
                                          "id": 4,
                                          "enabled": true,
                                          "token": "sk-real-source-key",
                                          "token_masked": "sk-***",
                                          "name": "main",
                                          "group_key": "default",
                                          "group_name": "Default",
                                          "value_status": "ready"
                                        }
                                      ],
                                      "projected_keys": [
                                        {
                                          "id": 5,
                                          "channel_id": 3,
                                          "channel_name": "Projected OpenAI",
                                          "enabled": true,
                                          "channel_key": "sk-real-projected-key",
                                          "channel_key_masked": "sk-proj-***",
                                          "remark": "primary",
                                          "status_code": 1,
                                          "last_use_time_stamp": 0,
                                          "total_cost": 0
                                        }
                                      ],
                                      "models": [
                                        {
                                          "model_name": "gpt-4o",
                                          "source": "sync",
                                          "route_type": "openai_chat",
                                          "route_source": "sync_inferred",
                                          "manual_override": false,
                                          "disabled": false,
                                          "projected_channel_id": 3
                                        }
                                      ]
                                    }
                                  ]
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

            val result = repository.siteChannels(includeHistory = false)

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val card = (result as AppResult.Success).data.single()
            assertThat(card.siteName).isEqualTo("OpenAI Hub")
            assertThat(card.accounts.single().routeSummaries.single().routeType).isEqualTo("openai_chat")
            val group = card.accounts.single().groups.single()
            assertThat(group.projectedChannels.single().channelName).isEqualTo("Projected OpenAI")
            assertThat(group.sourceKeys.single().token).isEmpty()
            assertThat(group.sourceKeys.single().tokenMasked).isEqualTo("sk-***")
            assertThat(group.projectedKeys.single().channelKey).isEmpty()
            assertThat(group.projectedKeys.single().channelKeyMasked).isEqualTo("sk-proj-***")
            assertThat(group.models.single().modelName).isEqualTo("gpt-4o")
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/site-channel/list?include_history=false")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun mutationsUseWebCompatiblePathsAndSanitizeReturnedAccount() = runBlocking {
        val server = MockWebServer().apply {
            repeat(9) {
                enqueue(MockResponse().setResponseCode(200).setBody(accountEnvelope()))
            }
            start()
        }

        try {
            val repository = repositoryFor(server)

            val routeResult = repository.updateModelRoutes(
                siteId = 7,
                accountId = 8,
                request = listOf(
                    SiteModelRouteUpdateRequest(
                        groupKey = "default",
                        modelName = "gpt-4o",
                        routeType = "openai_chat",
                    ),
                ),
            )
            assertThat(routeResult).isInstanceOf(AppResult.Success::class.java)
            val routeAccount = (routeResult as AppResult.Success).data
            assertThat(routeAccount.groups.single().sourceKeys.single().token).isEmpty()
            assertThat(routeAccount.groups.single().projectedKeys.single().channelKey).isEmpty()
            server.assertRequest(
                method = "PUT",
                path = "/api/v1/site-channel/7/account/8/model-routes",
                bodyContains = listOf(""""group_key":"default"""", """"model_name":"gpt-4o"""", """"route_type":"openai_chat""""),
            )

            repository.resetModelRoutes(siteId = 7, accountId = 8)
            server.assertRequest(
                method = "POST",
                path = "/api/v1/site-channel/7/account/8/model-routes/reset",
            )

            repository.createKey(
                siteId = 7,
                accountId = 8,
                request = SiteChannelKeyCreateRequest(groupKey = "default", name = "primary"),
            )
            server.assertRequest(
                method = "POST",
                path = "/api/v1/site-channel/7/account/8/keys",
                bodyContains = listOf(""""group_key":"default"""", """"name":"primary""""),
            )

            repository.updateSourceKeys(
                siteId = 7,
                accountId = 8,
                request = SiteSourceKeyUpdateRequest(
                    groupKey = "default",
                    keysToAdd = listOf(SiteSourceKeyAddRequest(enabled = true, token = "sk-added", name = "added")),
                    keysToUpdate = listOf(SiteSourceKeyUpdateItem(id = 4, enabled = false, token = "sk-updated", name = "updated")),
                    keysToDelete = listOf(5),
                ),
            )
            server.assertRequest(
                method = "PUT",
                path = "/api/v1/site-channel/7/account/8/source-keys",
                bodyContains = listOf(""""keys_to_add"""", """"keys_to_update"""", """"keys_to_delete":[5]"""),
            )

            repository.updateProjectedChannelSettings(
                siteId = 7,
                accountId = 8,
                request = listOf(
                    SiteProjectedChannelSettingsUpdateRequest(
                        channelId = 3,
                        autoGroup = 2,
                        paramOverride = """{"stream":true}""",
                    ),
                ),
            )
            server.assertRequest(
                method = "PUT",
                path = "/api/v1/site-channel/7/account/8/projected-channel-settings",
                bodyContains = listOf(""""channel_id":3""", """"auto_group":2""", """"param_override":"{\"stream\":true}""""),
            )

            repository.addManualModels(
                siteId = 7,
                accountId = 8,
                request = SiteManualModelAddRequest(
                    groupKey = "default",
                    models = listOf(SiteManualModelAddEntry(modelName = "custom-model", routeType = "openai_chat")),
                ),
            )
            server.assertRequest(
                method = "POST",
                path = "/api/v1/site-channel/7/account/8/manual-models",
                bodyContains = listOf(""""model_name":"custom-model"""", """"route_type":"openai_chat""""),
            )

            repository.deleteManualModel(
                siteId = 7,
                accountId = 8,
                request = SiteManualModelDeleteRequest(groupKey = "default", modelName = "custom-model"),
            )
            server.assertRequest(
                method = "POST",
                path = "/api/v1/site-channel/7/account/8/manual-models/delete",
                bodyContains = listOf(""""model_name":"custom-model""""),
            )

            repository.updateModelDisabled(
                siteId = 7,
                accountId = 8,
                request = listOf(SiteModelDisableUpdateRequest(groupKey = "default", modelName = "gpt-4o", disabled = true)),
            )
            server.assertRequest(
                method = "PUT",
                path = "/api/v1/site-channel/7/account/8/model-disabled",
                bodyContains = listOf(""""disabled":true"""),
            )

            repository.updateGroupProjection(
                siteId = 7,
                accountId = 8,
                request = SiteGroupProjectionUpdateRequest(groupKey = "default", projectionDisabled = true),
            )
            server.assertRequest(
                method = "PUT",
                path = "/api/v1/site-channel/7/account/8/group-projection",
                bodyContains = listOf(""""projection_disabled":true"""),
            )
        } finally {
            server.shutdown()
        }
    }

    private fun repositoryFor(server: MockWebServer): SiteChannelRepository {
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SiteChannelApiService::class.java)

        return SiteChannelRepository(
            apiService = service,
            executor = NetworkExecutor(json),
            dispatchers = DispatchersProvider(),
        )
    }

    private fun MockWebServer.assertRequest(
        method: String,
        path: String,
        bodyContains: List<String> = emptyList(),
    ) {
        val request = takeRequest()
        assertThat(request.method).isEqualTo(method)
        assertThat(request.path).isEqualTo(path)
        val body = request.body.readUtf8()
        bodyContains.forEach { expected -> assertThat(body).contains(expected) }
    }

    private fun accountEnvelope(): String =
        """
        {
          "code": 200,
          "message": "success",
          "data": {
            "site_id": 7,
            "account_id": 8,
            "account_name": "Primary",
            "groups": [
              {
                "group_key": "default",
                "group_name": "Default",
                "source_keys": [
                  { "id": 4, "enabled": true, "token": "sk-real-source", "token_masked": "sk-***" }
                ],
                "projected_keys": [
                  { "id": 5, "channel_id": 3, "enabled": true, "channel_key": "sk-real-projected", "channel_key_masked": "sk-proj-***" }
                ]
              }
            ]
          }
        }
        """.trimIndent()
}
