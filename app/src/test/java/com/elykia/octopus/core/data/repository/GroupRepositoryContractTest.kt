package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.GroupHealthProbeMode
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
