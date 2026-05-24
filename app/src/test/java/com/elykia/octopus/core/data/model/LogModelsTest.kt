package com.elykia.octopus.core.data.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogModelsTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun logListDecodesHureruWrappedResponse() {
        val response = json.decodeFromString<ApiResponse<LogListResponse>>(
            """
            {
              "code": 200,
              "message": "success",
              "data": {
                "logs": [
                  {
                    "id": 99,
                    "time": 1710000000,
                    "request_model_name": "gpt-4o-mini",
                    "request_api_key_name": "mobile",
                    "channel": 7,
                    "channel_name": "OpenAI",
                    "actual_model_name": "gpt-4o-mini",
                    "input_tokens": 12,
                    "transport_input_tokens": 10,
                    "bill_input_tokens": 12,
                    "cache_read_tokens": 2,
                    "cache_write_tokens": 3,
                    "output_tokens": 34,
                    "ftut": 120,
                    "use_time": 456,
                    "cost": 0.001,
                    "request_content": "",
                    "response_content": "",
                    "error": "",
                    "success": true,
                    "attempts": [
                      {
                        "channel_id": 7,
                        "channel_key_id": 8,
                        "channel_name": "OpenAI",
                        "model_name": "gpt-4o-mini",
                        "attempt_num": 1,
                        "status": "success",
                        "duration": 456,
                        "sticky": true,
                        "msg": ""
                      }
                    ],
                    "total_attempts": 1,
                    "used_ws": true,
                    "ws_mode": "fresh",
                    "ws_exec_mode": "transform",
                    "ws_recovery": null
                  }
                ],
                "total": 123,
                "has_more": true,
                "next_cursor": {"time": 1709999999, "id": 98},
                "search_mode": "default",
                "warning": "partial"
              }
            }
            """.trimIndent()
        )

        val payload = response.data!!
        val log = payload.logs.single()
        assertTrue(response.isSuccessful)
        assertEquals(123L, payload.total)
        assertEquals(true, payload.hasMore)
        assertEquals(98L, payload.nextCursor?.id)
        assertEquals("partial", payload.warning)
        assertFalse(log.hasError)
        assertEquals(2, log.cacheReadTokens)
        assertEquals("success", log.attempts.single().status)
        assertEquals("fresh", log.wsMode)
    }

    @Test
    fun logListTreatsNullLogsAsEmpty() {
        val response = json.decodeFromString<ApiResponse<LogListResponse>>(
            """
            {
              "code": 200,
              "message": "success",
              "data": {
                "logs": null,
                "total": 0,
                "has_more": false
              }
            }
            """.trimIndent()
        )

        val payload = response.data!!
        assertEquals(emptyList<LogItem>(), payload.logs)
        assertEquals(false, payload.hasMore)
    }
}
