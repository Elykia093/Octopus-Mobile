package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.OctopusApiService
import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import retrofit2.Retrofit

class ApiKeyRepositoryContractTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Test
    fun updateApiKeyDoesNotSendBlankMaskedKey() = runBlocking {
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
                            "id": 7,
                            "name": "Mobile",
                            "api_key": "",
                            "enabled": false,
                            "expire_at": 0,
                            "max_cost": 0.0,
                            "supported_models": ""
                          }
                        }
                        """.trimIndent(),
                    ),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)
            val result = repository.updateApiKey(
                ApiKeyItem(
                    id = 7,
                    name = "Mobile",
                    apiKey = "",
                    enabled = false,
                    expireAt = null,
                    maxCost = null,
                    supportedModels = null,
                ),
            )

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val body = server.takeRequest().body.readUtf8()
            assertThat(body).contains(""""id":7""")
            assertThat(body).contains(""""name":"Mobile"""")
            assertThat(body).contains(""""enabled":false""")
            assertThat(body).doesNotContain("api_key")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun updateApiKeySendsNonBlankReplacementKey() = runBlocking {
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
                            "id": 7,
                            "name": "Mobile",
                            "api_key": "sk-new-secret",
                            "enabled": true,
                            "expire_at": 100,
                            "max_cost": 2.5,
                            "supported_models": "gpt-test"
                          }
                        }
                        """.trimIndent(),
                    ),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)
            val result = repository.updateApiKey(
                ApiKeyItem(
                    id = 7,
                    name = "Mobile",
                    apiKey = "sk-new-secret",
                    enabled = true,
                    expireAt = 100L,
                    maxCost = 2.5,
                    supportedModels = "gpt-test",
                ),
            )

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val body = server.takeRequest().body.readUtf8()
            assertThat(body).contains(""""api_key":"sk-new-secret"""")
            assertThat(body).contains(""""expire_at":100""")
            assertThat(body).contains(""""max_cost":2.5""")
            assertThat(body).contains(""""supported_models":"gpt-test"""")
        } finally {
            server.shutdown()
        }
    }

    private fun repositoryFor(server: MockWebServer): ApiKeyRepository {
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OctopusApiService::class.java)

        return ApiKeyRepository(
            apiService = service,
            executor = NetworkExecutor(json),
            dispatchers = DispatchersProvider(),
        )
    }
}
