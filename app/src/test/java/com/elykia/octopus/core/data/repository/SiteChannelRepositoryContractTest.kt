package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
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
}
