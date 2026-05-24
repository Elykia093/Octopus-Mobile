package com.elykia.octopus.core.data.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiKeyModelsTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun apiKeyStatsDecodesHureruResponse() {
        val response = json.decodeFromString<ApiResponse<ApiKeyStatsResponse>>(
            """
            {
              "code": 200,
              "message": "success",
              "data": {
                "stats": {
                  "api_key_id": 7,
                  "input_token": 100,
                  "output_token": 50,
                  "input_cost": 0.01,
                  "output_cost": 0.02,
                  "wait_time": 300,
                  "request_success": 9,
                  "request_failed": 1
                },
                "info": {
                  "id": 7,
                  "name": "mobile",
                  "api_key": "sk-octopus-xxx",
                  "enabled": true,
                  "expire_at": 0,
                  "max_cost": 0,
                  "supported_models": ""
                }
              }
            }
            """.trimIndent()
        )

        assertTrue(response.isSuccessful)
        assertEquals("mobile", response.data?.info?.name)
        assertEquals(150L, response.data?.stats?.totalTokens)
        assertEquals(10L, response.data?.stats?.totalRequests)
    }
}
