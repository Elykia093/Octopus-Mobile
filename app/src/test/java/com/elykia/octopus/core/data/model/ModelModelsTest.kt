package com.elykia.octopus.core.data.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelModelsTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun modelListDecodesGlobalPriceFields() {
        val response = json.decodeFromString<ApiResponse<List<LlmInfo>>>(
            """
            {
              "code": 200,
              "message": "success",
              "data": [
                {
                  "name": "gpt-4o-mini",
                  "input": 0.00015,
                  "output": 0.0006,
                  "cache_read": 0.000075,
                  "cache_write": 0.0003
                }
              ]
            }
            """.trimIndent()
        )

        val model = response.data!!.single()
        assertTrue(response.isSuccessful)
        assertEquals("gpt-4o-mini", model.name)
        assertEquals(0.00015, model.input, 0.0)
        assertEquals(0.000075, model.cacheRead, 0.0)
        assertEquals(0.0003, model.cacheWrite, 0.0)
    }

    @Test
    fun modelChannelDecodesSiteMappingFields() {
        val response = json.decodeFromString<ApiResponse<List<LlmChannel>>>(
            """
            {
              "code": 200,
              "message": "success",
              "data": [
                {
                  "name": "gpt-4o-mini",
                  "enabled": true,
                  "channel_id": 7,
                  "channel_name": "OpenAI",
                  "site_id": 1,
                  "site_account_id": 2,
                  "site_group_key": "default",
                  "site_group_name": "Default",
                  "site_name": "Aggregator",
                  "site_account_name": "main",
                  "endpoint_type": "openai_chat"
                }
              ]
            }
            """.trimIndent()
        )

        val mapping = response.data!!.single()
        assertEquals(7, mapping.channelId)
        assertEquals("default", mapping.siteGroupKey)
        assertEquals("openai_chat", mapping.endpointType)
    }
}
