package com.elykia.octopus.core.data.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResponseTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun isSuccessfulAcceptsServerCodeSuccess() {
        assertTrue(ApiResponse<String>(code = 200, data = "ok").isSuccessful)
    }

    @Test
    fun isSuccessfulAcceptsLegacySuccessFlag() {
        assertTrue(ApiResponse<String>(success = true, data = "ok").isSuccessful)
    }

    @Test
    fun isSuccessfulRejectsServerErrorCode() {
        assertFalse(ApiResponse<String>(code = 401, message = "Authentication failed").isSuccessful)
    }

    @Test
    fun parsesHureruErrorFields() {
        val response = json.decodeFromString<ApiResponse<String>>(
            """
            {
              "code": 401,
              "error_code": "site.sub2api.api_key_required",
              "message": "API key is missing",
              "params": {
                "site_id": 12
              }
            }
            """.trimIndent()
        )

        assertEquals(401, response.code)
        assertEquals("site.sub2api.api_key_required", response.errorCode)
        assertEquals("API key is missing", response.message)
        assertEquals(12, response.params?.get("site_id")?.toString()?.toInt())
        assertFalse(response.isSuccessful)
    }
}
