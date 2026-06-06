package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.RelayLog
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

class LogRepositoryContractTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Test
    fun logsTreatsSuccessfulNullDataAsEmptyList() = runBlocking {
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

            assertThat(result).isEqualTo(AppResult.Success(emptyList<RelayLog>()))
            assertThat(server.takeRequest().path).isEqualTo("/api/v1/log/list?page=2&page_size=20")
        } finally {
            server.shutdown()
        }
    }

    private fun repositoryFor(server: MockWebServer): LogRepository {
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OctopusApiService::class.java)

        return LogRepository(
            apiService = service,
            executor = NetworkExecutor(json),
            dispatchers = DispatchersProvider(),
        )
    }
}
