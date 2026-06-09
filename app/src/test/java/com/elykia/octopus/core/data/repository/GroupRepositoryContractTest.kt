package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.GroupAutoGroupConfigUpdateRequest
import com.elykia.octopus.core.data.model.GroupAutoGroupSourceUpdateRequest
import com.elykia.octopus.core.data.model.GroupHealthProbeMode
import com.elykia.octopus.core.data.model.GroupPresetItem
import com.elykia.octopus.core.data.model.GroupPresetUpdateRequest
import com.elykia.octopus.core.data.remote.GroupApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import retrofit2.Retrofit

class GroupRepositoryContractTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun groupsReadPinnedAndActivePresetFields() = runBlocking {
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
                              "id": 9,
                              "name": "Pinned",
                              "mode": 4,
                              "match_regex": "gpt",
                              "pinned": true,
                              "pinned_at": "2026-06-09T01:00:00Z",
                              "active_preset_id": 12,
                              "items": []
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

            val result = repository.groups()

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val group = (result as AppResult.Success).data.single()
            assertThat(group.pinned).isTrue()
            assertThat(group.pinnedAt).isEqualTo("2026-06-09T01:00:00Z")
            assertThat(group.activePresetId).isEqualTo(12)
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/group/list")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun pinGroupPostsPinnedFlag() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(200).setBody("""{"code":200,"message":"success","data":"ok"}"""))
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.pinGroup(9, true)

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val request = server.takeRequest()
            assertThat(request.method).isEqualTo("POST")
            assertThat(request.path).isEqualTo("/api/v1/group/pin/9")
            assertThat(request.body.readUtf8()).contains(""""pinned":true""")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun groupPresetsReadSnapshotItems() = runBlocking {
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
                              "id": 12,
                              "group_id": 9,
                              "name": "Fast",
                              "mode": 4,
                              "match_regex": "gpt",
                              "first_token_time_out": 12,
                              "session_keep_time": 60,
                              "retry_enabled": true,
                              "max_retries": 5,
                              "items": [
                                {"channel_id": 3, "model_name": "gpt-4o", "priority": 1, "weight": 2}
                              ],
                              "created_at": "2026-06-09T01:00:00Z",
                              "updated_at": "2026-06-09T01:01:00Z"
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

            val result = repository.groupPresets(9)

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val preset = (result as AppResult.Success).data.single()
            assertThat(preset.id).isEqualTo(12)
            assertThat(preset.items.single().modelName).isEqualTo("gpt-4o")
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/group/preset/list/9")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun updateGroupPresetPutsFullSnapshot() = runBlocking {
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
                            "id": 12,
                            "group_id": 9,
                            "name": "Fast",
                            "mode": 4,
                            "items": []
                          }
                        }
                        """.trimIndent(),
                    ),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.updateGroupPreset(
                12,
                GroupPresetUpdateRequest(
                    name = "Fast",
                    mode = 4,
                    items = listOf(GroupPresetItem(channelId = 3, modelName = "gpt-4o", priority = 1, weight = 2)),
                ),
            )

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val request = server.takeRequest()
            val body = request.body.readUtf8()
            assertThat(request.method).isEqualTo("PUT")
            assertThat(request.path).isEqualTo("/api/v1/group/preset/update/12")
            assertThat(body).contains(""""name":"Fast"""")
            assertThat(body).contains(""""channel_id":3""")
            assertThat(body).contains(""""model_name":"gpt-4o"""")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun autoGroupConfigUpdatePutsGlobalModeAndItems() = runBlocking {
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
                            "projected_global_auto_group": 2,
                            "sources": [
                              {
                                "channel_id": 3,
                                "channel_name": "Projected",
                                "enabled": true,
                                "managed": true,
                                "auto_group": 2,
                                "effective_auto_group": 2,
                                "global_override": false,
                                "model_count": 1,
                                "models": ["gpt-4o"]
                              }
                            ]
                          }
                        }
                        """.trimIndent(),
                    ),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.updateGroupAutoGroupConfig(
                GroupAutoGroupConfigUpdateRequest(
                    projectedGlobalAutoGroup = 2,
                    items = listOf(GroupAutoGroupSourceUpdateRequest(channelId = 3, autoGroup = 2)),
                    runNow = true,
                ),
            )

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val config = (result as AppResult.Success).data
            assertThat(config.projectedGlobalAutoGroup).isEqualTo(2)
            assertThat(config.sources.single().models).containsExactly("gpt-4o")
            val request = server.takeRequest()
            val body = request.body.readUtf8()
            assertThat(request.method).isEqualTo("PUT")
            assertThat(request.path).isEqualTo("/api/v1/group/auto-group/config")
            assertThat(body).contains(""""projected_global_auto_group":2""")
            assertThat(body).contains(""""auto_group":2""")
            assertThat(body).contains(""""run_now":true""")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun runAutoGroupPostsChannelIds() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(200).setBody("""{"code":200,"message":"success","data":"ok"}"""))
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.runGroupAutoGroup(listOf(3, 4))

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val request = server.takeRequest()
            assertThat(request.method).isEqualTo("POST")
            assertThat(request.path).isEqualTo("/api/v1/group/auto-group/run")
            assertThat(request.body.readUtf8()).contains(""""channel_ids":[3,4]""")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun groupHealthListReadsLatestSnapshot() = runBlocking {
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
                              "group_id": 7,
                              "group_name": "Default",
                              "group_mode": 3,
                              "latest": {
                                "id": 11,
                                "group_id": 7,
                                "group_name": "Default",
                                "group_mode": 3,
                                "probe_mode": "full",
                                "request_model": "gpt-4o",
                                "status": "partial",
                                "started_at": "2026-06-09T00:00:00Z",
                                "duration_ms": 120,
                                "message": "1 failed",
                                "attempts": [
                                  {
                                    "id": 1,
                                    "snapshot_id": 11,
                                    "group_item_id": 2,
                                    "channel_id": 3,
                                    "channel_name": "Projected",
                                    "channel_key_id": 4,
                                    "key_remark": "default",
                                    "model_name": "gpt-4o",
                                    "priority": 1,
                                    "weight": 1,
                                    "status": "failed",
                                    "http_status": 500,
                                    "duration_ms": 120,
                                    "error_message": "upstream failed"
                                  }
                                ]
                              }
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

            val result = repository.groupHealthList()

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val view = (result as AppResult.Success).data.single()
            assertThat(view.groupId).isEqualTo(7)
            assertThat(view.latest?.probeMode).isEqualTo(GroupHealthProbeMode.Full)
            assertThat(view.latest?.attempts?.single()?.channelName).isEqualTo("Projected")
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/group/health/list")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun runGroupHealthPostsProbeMode() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"code":200,"message":"success","data":{"group_id":7,"probe_mode":"full"}}"""),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.runGroupHealth(7, GroupHealthProbeMode.Full)

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val request = server.takeRequest()
            assertThat(request.method).isEqualTo("POST")
            assertThat(request.path).isEqualTo("/api/v1/group/health/7/run")
            assertThat(request.body.readUtf8()).contains(""""probe_mode":"full"""")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun runAllGroupHealthPostsFullProbeMode() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"code":200,"message":"success","data":{"all_groups":true,"probe_mode":"full"}}"""),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.runAllGroupHealth(GroupHealthProbeMode.Full)

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val data = (result as AppResult.Success).data
            assertThat(data.allGroups).isTrue()
            assertThat(data.probeMode).isEqualTo(GroupHealthProbeMode.Full)
            val request = server.takeRequest()
            assertThat(request.method).isEqualTo("POST")
            assertThat(request.path).isEqualTo("/api/v1/group/health/run-all")
            assertThat(request.body.readUtf8()).contains(""""probe_mode":"full"""")
        } finally {
            server.shutdown()
        }
    }

    private fun repositoryFor(server: MockWebServer): GroupRepository {
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GroupApiService::class.java)

        return GroupRepository(
            apiService = service,
            executor = NetworkExecutor(json),
            dispatchers = DispatchersProvider(),
        )
    }
}
