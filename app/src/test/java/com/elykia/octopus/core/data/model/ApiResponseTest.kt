package com.elykia.octopus.core.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResponseTest {
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
}
