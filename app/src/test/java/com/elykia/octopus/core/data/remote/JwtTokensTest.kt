package com.elykia.octopus.core.data.remote

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JwtTokensTest {
    @Test
    fun authorizationHeaderKeepsExistingBearerPrefix() {
        assertEquals("Bearer abc", JwtTokens.authorizationHeader("Bearer abc"))
    }

    @Test
    fun authorizationHeaderAddsBearerPrefix() {
        assertEquals("Bearer abc", JwtTokens.authorizationHeader(" abc "))
    }

    @Test
    fun isExpiredReturnsTrueWhenExpIsInThePast() {
        val token = jwtWithPayload("""{"exp":100}""")

        assertTrue(JwtTokens.isExpired(token, nowEpochSeconds = 101))
    }

    @Test
    fun isExpiredReturnsFalseWhenExpIsMissingOrUnreadable() {
        assertFalse(JwtTokens.isExpired("not-a-jwt", nowEpochSeconds = 101))
        assertFalse(JwtTokens.isExpired(jwtWithPayload("""{"sub":"admin"}"""), nowEpochSeconds = 101))
    }

    private fun jwtWithPayload(payload: String): String {
        val header = encode("""{"alg":"none"}""")
        return "$header.${encode(payload)}."
    }

    private fun encode(value: String): String {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.toByteArray(Charsets.UTF_8))
    }
}
