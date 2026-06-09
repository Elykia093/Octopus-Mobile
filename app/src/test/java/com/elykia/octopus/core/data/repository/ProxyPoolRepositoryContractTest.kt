package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.ProxyConfigurationCreateRequest
import com.elykia.octopus.core.data.model.ProxyConfigurationUpdateRequest
import com.elykia.octopus.core.data.model.ProxyTestRequest
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.ProxyPoolApiService
import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import retrofit2.Retrofit

class ProxyPoolRepositoryContractTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun listTreatsNullDataAsEmptyList() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"code":200,"message":"success","data":null}"""),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.proxyConfigurations()

            assertThat(result).isEqualTo(AppResult.Success(emptyList<Any>()))
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/proxy-pool/list")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun listAndReferencesUseProxyPoolEndpoints() = runBlocking {
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
                              "id": 7,
                              "name": "Main proxy",
                              "url": "socks5://user:pass@proxy.example.com:1080",
                              "enabled": true,
                              "remark": "primary",
                              "reference_count": 2,
                              "created_at": "2026-06-01T00:00:00Z",
                              "updated_at": "2026-06-02T00:00:00Z"
                            }
                          ]
                        }
                        """.trimIndent(),
                    ),
            )
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
                              "type": "site_account",
                              "site_id": 1,
                              "site_name": "Upstream",
                              "site_account_id": 2,
                              "site_account_name": "Primary"
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

            val listResult = repository.proxyConfigurations()
            val referencesResult = repository.references(7)

            assertThat(listResult).isInstanceOf(AppResult.Success::class.java)
            val proxy = (listResult as AppResult.Success).data.single()
            assertThat(proxy.id).isEqualTo(7)
            assertThat(proxy.referenceCount).isEqualTo(2)
            assertThat(proxy.url).contains("user:pass")
            assertThat(referencesResult).isInstanceOf(AppResult.Success::class.java)
            val reference = (referencesResult as AppResult.Success).data.single()
            assertThat(reference.type).isEqualTo("site_account")
            assertThat(reference.siteName).isEqualTo("Upstream")
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/proxy-pool/list")
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/proxy-pool/references/7")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun createUpdateDeleteAndTestUseWebProxyPoolRoutes() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(proxyResponse(id = 1, name = "Created"))
            enqueue(proxyResponse(id = 1, name = "Updated"))
            enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        {
                          "code": 200,
                          "message": "success",
                          "data": {
                            "success": true,
                            "status_code": 200,
                            "duration_ms": 123,
                            "message": "ok"
                          }
                        }
                        """.trimIndent(),
                    ),
            )
            enqueue(MockResponse().setResponseCode(200).setBody("""{"code":200,"message":"success","data":null}"""))
            start()
        }

        try {
            val repository = repositoryFor(server)

            val createResult = repository.createProxyConfiguration(
                ProxyConfigurationCreateRequest(
                    name = "Created",
                    url = "socks5://127.0.0.1:1080",
                    enabled = true,
                    remark = "local",
                ),
            )
            val updateResult = repository.updateProxyConfiguration(
                ProxyConfigurationUpdateRequest(
                    id = 1,
                    name = "Updated",
                    url = "https://proxy.example.com",
                    enabled = false,
                    remark = "remote",
                ),
            )
            val testResult = repository.testProxyConfiguration(
                ProxyTestRequest(proxyConfigId = 1, url = "https://api.openai.com/v1/models"),
            )
            val deleteResult = repository.deleteProxyConfiguration(1)

            assertThat(createResult).isInstanceOf(AppResult.Success::class.java)
            assertThat(updateResult).isInstanceOf(AppResult.Success::class.java)
            assertThat(testResult).isInstanceOf(AppResult.Success::class.java)
            assertThat(deleteResult).isEqualTo(AppResult.Success(null))

            val createRequest = server.takeRequest()
            assertThat(createRequest.path).isEqualTo("/api/v1/proxy-pool/create")
            assertThat(createRequest.body.readUtf8()).contains(""""url":"socks5://127.0.0.1:1080"""")

            val updateRequest = server.takeRequest()
            assertThat(updateRequest.path).isEqualTo("/api/v1/proxy-pool/update")
            assertThat(updateRequest.body.readUtf8()).contains(""""enabled":false""")

            val testRequest = server.takeRequest()
            assertThat(testRequest.path).isEqualTo("/api/v1/proxy-pool/test")
            assertThat(testRequest.body.readUtf8()).contains(""""proxy_config_id":1""")

            val deleteRequest = server.takeRequest()
            assertThat(deleteRequest.method).isEqualTo("DELETE")
            assertThat(deleteRequest.path).isEqualTo("/api/v1/proxy-pool/delete/1")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun testResultMessageMasksProxyCredentials() = runBlocking {
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
                            "success": false,
                            "status_code": 0,
                            "duration_ms": 5,
                            "message": "failed socks5://user:secret@proxy.example.com:1080"
                          }
                        }
                        """.trimIndent(),
                    ),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.testProxyConfiguration(
                ProxyTestRequest(proxyUrl = "socks5://user:secret@proxy.example.com:1080"),
            )

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val message = (result as AppResult.Success).data.message
            assertThat(message).doesNotContain("secret")
            assertThat(message).contains("socks5://user:***@proxy.example.com:1080")
        } finally {
            server.shutdown()
        }
    }

    private fun proxyResponse(id: Int, name: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setBody(
                """
                {
                  "code": 200,
                  "message": "success",
                  "data": {
                    "id": $id,
                    "name": "$name",
                    "url": "socks5://127.0.0.1:1080",
                    "enabled": true,
                    "remark": "",
                    "reference_count": 0,
                    "created_at": "2026-06-01T00:00:00Z",
                    "updated_at": "2026-06-01T00:00:00Z"
                  }
                }
                """.trimIndent(),
            )

    private fun repositoryFor(server: MockWebServer): ProxyPoolRepository {
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ProxyPoolApiService::class.java)

        return ProxyPoolRepository(
            apiService = service,
            executor = NetworkExecutor(json),
            dispatchers = DispatchersProvider(),
        )
    }
}
