package com.elykia.octopus.feature.channel

import com.elykia.octopus.core.data.model.BaseUrl
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChannelSecurityTest {
    @Test
    fun channelBaseUrlMustBeHttpsWhenPresent() {
        assertThat(hasValidChannelBaseUrl("")).isTrue()
        assertThat(hasValidChannelBaseUrl(" https://api.example.com/v1 ")).isTrue()
        assertThat(hasValidChannelBaseUrl("http://api.example.com/v1")).isFalse()
    }

    @Test
    fun channelBaseUrlRejectsCredentialsQueryAndFragment() {
        assertThat(hasValidChannelBaseUrl("https://user:pass@api.example.com/v1")).isFalse()
        assertThat(hasValidChannelBaseUrl("https://api.example.com/v1?token=secret")).isFalse()
        assertThat(hasValidChannelBaseUrl("https://api.example.com/v1#secret")).isFalse()
    }

    @Test
    fun canSubmitChannelEditorRequiresValidNameUrlAndEditableState() {
        assertThat(
            canSubmitChannelEditor(
                values = ChannelEditorValues(
                    name = "OpenAI",
                    baseUrls = listOf(BaseUrl("https://api.example.com/v1")),
                    keys = listOf(ChannelKeyEditorItem(channelKey = "sk-test")),
                ),
                submitting = false,
            )
        ).isTrue()

        assertThat(
            canSubmitChannelEditor(
                values = ChannelEditorValues(
                    name = " ",
                    baseUrls = listOf(BaseUrl("https://api.example.com/v1")),
                    keys = listOf(ChannelKeyEditorItem(channelKey = "sk-test")),
                ),
                submitting = false,
            )
        ).isFalse()
        assertThat(
            canSubmitChannelEditor(
                values = ChannelEditorValues(
                    name = "OpenAI",
                    baseUrls = listOf(BaseUrl("http://api.example.com/v1")),
                    keys = listOf(ChannelKeyEditorItem(channelKey = "sk-test")),
                ),
                submitting = false,
            )
        ).isFalse()
        assertThat(
            canSubmitChannelEditor(
                values = ChannelEditorValues(
                    name = "OpenAI",
                    baseUrls = listOf(BaseUrl("https://api.example.com/v1?token=secret")),
                    keys = listOf(ChannelKeyEditorItem(channelKey = "sk-test")),
                ),
                submitting = false,
            )
        ).isFalse()
        assertThat(
            canSubmitChannelEditor(
                values = ChannelEditorValues(
                    name = "OpenAI",
                    baseUrls = listOf(BaseUrl("https://api.example.com/v1")),
                    keys = listOf(ChannelKeyEditorItem(channelKey = "sk-test")),
                ),
                submitting = true,
            )
        ).isFalse()
        assertThat(
            canSubmitChannelEditor(
                values = ChannelEditorValues(
                    name = "OpenAI",
                    baseUrls = listOf(BaseUrl("https://api.example.com/v1")),
                    keys = emptyList(),
                ),
                submitting = false,
            )
        ).isFalse()
    }
}
