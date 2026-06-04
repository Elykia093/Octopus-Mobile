package com.elykia.octopus.feature.apikey

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiKeyMaskTest {
    @Test
    fun maskApiKeyDoesNotRevealShortKeys() {
        assertThat("sk".maskApiKey()).isEqualTo("****")
        assertThat("sk-short".maskApiKey()).isEqualTo("sk...rt")
    }

    @Test
    fun maskApiKeyKeepsLongKeyRecognizableWithoutFullValue() {
        assertThat("sk-1234567890abcdef".maskApiKey()).isEqualTo("sk-12345...cdef")
    }

    @Test
    fun parseApiKeyEditorValuesTreatsBlankLimitsAsUnlimited() {
        val result = parseApiKeyEditorValues(expireAt = " ", maxCost = "")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().expireAt).isEqualTo(0L)
        assertThat(result.getOrThrow().maxCost).isEqualTo(0.0)
    }

    @Test
    fun parseApiKeyEditorValuesAcceptsNonNegativeNumbers() {
        val result = parseApiKeyEditorValues(expireAt = " 100 ", maxCost = "2.5")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().expireAt).isEqualTo(100L)
        assertThat(result.getOrThrow().maxCost).isEqualTo(2.5)
    }

    @Test
    fun parseApiKeyEditorValuesRejectsInvalidExpireAt() {
        val negative = parseApiKeyEditorValues(expireAt = "-1", maxCost = "0")
        val text = parseApiKeyEditorValues(expireAt = "tomorrow", maxCost = "0")

        assertThat((negative.exceptionOrNull() as ApiKeyEditorValidationException).issue)
            .isEqualTo(ApiKeyEditorValidationIssue.InvalidExpireAt)
        assertThat((text.exceptionOrNull() as ApiKeyEditorValidationException).issue)
            .isEqualTo(ApiKeyEditorValidationIssue.InvalidExpireAt)
    }

    @Test
    fun parseApiKeyEditorValuesRejectsInvalidMaxCost() {
        val negative = parseApiKeyEditorValues(expireAt = "0", maxCost = "-1")
        val text = parseApiKeyEditorValues(expireAt = "0", maxCost = "free")
        val nan = parseApiKeyEditorValues(expireAt = "0", maxCost = "NaN")
        val infinity = parseApiKeyEditorValues(expireAt = "0", maxCost = "Infinity")

        listOf(negative, text, nan, infinity).forEach { result ->
            assertThat((result.exceptionOrNull() as ApiKeyEditorValidationException).issue)
                .isEqualTo(ApiKeyEditorValidationIssue.InvalidMaxCost)
        }
    }
}
