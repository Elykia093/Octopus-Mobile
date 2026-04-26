package com.elykia.octopus.core.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerUrlResolverTest {
    @Test
    fun normalizeReturnsNullForBlankBaseUrl() {
        assertNull(ServerUrlResolver.normalize("  "))
    }

    @Test
    fun normalizeAppendsApiV1PathWhenMissing() {
        val result = ServerUrlResolver.normalize("https://example.com/octopus/")

        assertEquals("https://example.com/octopus/api/v1/", result.toString())
    }

    @Test
    fun normalizeDoesNotDuplicateApiV1Path() {
        val result = ServerUrlResolver.normalize("https://example.com/api/v1/")

        assertEquals("https://example.com/api/v1/", result.toString())
    }

    @Test
    fun mergePreservesBasePathAndRequestQuery() {
        val baseUrl = "https://example.com/octopus/api/v1/".toHttpUrl()
        val requestUrl = "https://localhost/api/v1/channel/list?page=1".toHttpUrl()

        val result = ServerUrlResolver.merge(baseUrl, requestUrl)

        assertEquals("https://example.com/octopus/api/v1/channel/list?page=1", result.toString())
    }
}
