package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.remote.LogApiService
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

class LogRepositoryContractTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Test
    fun logsTreatsSuccessfulNullDataAsEmptyPage() = runBlocking {
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

            val result = repository.logs(page = 2, pageSize = 20)

            assertThat(result).isEqualTo(AppResult.Success(LogPage()))
            assertThat(server.takeRequest().path)
                .isEqualTo("/api/v1/log/list?page=2&page_size=20&include_content=false&with_total=false&pagination=page")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun logsReadsObjectPageResponseAndHidesContent() = runBlocking {
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
                            "logs": [
                              {
                                "id": 9,
                                "time": 100,
                                "request_model_name": "gpt-test",
                                "request_api_key_name": "mobile",
                                "channel": 1,
                                "channel_name": "OpenAI",
                                "actual_model_name": "gpt-test",
                                "input_tokens": 10,
                                "output_tokens": 20,
                                "ftut": 30,
                                "use_time": 40,
                                "cost": 0.5,
                                "request_content": "secret request",
                                "response_content": "secret response",
                                "error": "Bearer sk-secret-value"
                              }
                            ],
                            "total": 12,
                            "has_more": true,
                            "next_cursor": { "time": 99, "id": 8 },
                            "warning": "partial"
                          }
                        }
                        """.trimIndent(),
                    ),
            )
            start()
        }

        try {
            val repository = repositoryFor(server)

            val result = repository.logs(page = 1, pageSize = 20)

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val page = (result as AppResult.Success).data
            assertThat(page.total).isEqualTo(12)
            assertThat(page.hasMore).isTrue()
            assertThat(page.logs.single().requestContent).isEmpty()
            assertThat(page.logs.single().responseContent).isEmpty()
            assertThat(page.logs.single().error).doesNotContain("sk-secret-value")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun logDetailLoadsContentAndSanitizesSensitiveText() = runBlocking {
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
                            "id": 9,
                            "time": 100,
                            "request_model_name": "gpt-test",
                            "request_api_key_name": "mobile",
                            "channel": 1,
                            "channel_name": "OpenAI",
                            "actual_model_name": "gpt-test",
                            "input_tokens": 10,
                            "output_tokens": 20,
                            "ftut": 30,
                            "use_time": 40,
                            "cost": 0.5,
                            "request_content": "Authorization: Bearer sk-request-secret",
                            "response_content": "token=sk-response-secret",
                            "error": "Bearer sk-error-secret",
                            "attempts": [
                              {
                                "channel_id": 1,
                                "channel_name": "OpenAI",
                                "model_name": "gpt-test",
                                "attempt_num": 1,
                                "status": "failed",
                                "duration": 40,
                                "msg": "api_key=sk-attempt-secret"
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

            val result = repository.logDetail(9)

            assertThat(result).isInstanceOf(AppResult.Success::class.java)
            val detail = (result as AppResult.Success).data
            assertThat(detail.requestContent).doesNotContain("sk-request-secret")
            assertThat(detail.responseContent).doesNotContain("sk-response-secret")
            assertThat(detail.error).doesNotContain("sk-error-secret")
            assertThat(detail.attempts.single().msg).doesNotContain("sk-attempt-secret")
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/log/9")
        } finally {
            server.shutdown()
        }
    }

    private fun repositoryFor(server: MockWebServer): LogRepository {
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(LogApiService::class.java)

        return LogRepository(
            apiService = service,
            executor = NetworkExecutor(json),
            dispatchers = DispatchersProvider(),
        )
    }
}
