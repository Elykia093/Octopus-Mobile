package com.elykia.octopus.feature.channel

import com.elykia.octopus.core.data.model.BaseUrl
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.ChannelKey
import com.elykia.octopus.core.data.model.CustomHeader
import com.elykia.octopus.core.data.model.ProxyMode
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class ChannelOperationStateTest {
    private val json = Json { explicitNulls = false }

    @Test
    fun pageErrorOnlyShowsWhenRefreshFailsWithoutCachedChannels() {
        assertThat(
            ChannelUiState(error = "channels failed", channels = emptyList())
                .shouldShowPageError(),
        ).isTrue()

        assertThat(
            ChannelUiState(
                error = "channels failed",
                channels = listOf(Channel(id = 1, name = "Cached", type = 0)),
            ).shouldShowPageError(),
        ).isFalse()
    }

    @Test
    fun operationFailureKeepsVisibleErrorAndStopsSubmitting() {
        val state = ChannelUiState(submitting = true)
            .channelOperationFailed("channel failed")

        assertThat(state.submitting).isFalse()
        assertThat(state.operationError).isEqualTo("channel failed")
    }

    @Test
    fun operationStartAndSuccessClearPreviousError() {
        val started = ChannelUiState(operationError = "old error")
            .channelOperationStarted()

        assertThat(started.submitting).isTrue()
        assertThat(started.operationError).isNull()

        val succeeded = started.channelOperationSucceeded()

        assertThat(succeeded.submitting).isFalse()
        assertThat(succeeded.operationError).isNull()
    }

    @Test
    fun channelEditorValuesPreserveAdvancedFields() {
        val values = Channel(
            id = 7,
            name = "advanced",
            type = 3,
            enabled = false,
            baseUrls = listOf(BaseUrl("https://a.example.com"), BaseUrl("https://b.example.com")),
            keys = listOf(ChannelKey(id = 9, channelKey = "", enabled = false, remark = "primary")),
            customHeader = listOf(CustomHeader(headerKey = "X-Test", headerValue = "1")),
            proxyMode = ProxyMode.Pool,
            proxyConfigId = 12,
            channelProxy = "https://proxy.example.com",
            matchRegex = "gpt.*",
            paramOverride = """{"temperature":0.2}""",
        ).toEditorValues()

        assertThat(values.name).isEqualTo("advanced")
        assertThat(values.baseUrls).hasSize(2)
        assertThat(values.keys.single().id).isEqualTo(9)
        assertThat(values.keys.single().channelKey).isEmpty()
        assertThat(values.keys.single().remark).isEqualTo("primary")
        assertThat(values.customHeader).containsExactly(CustomHeader(headerKey = "X-Test", headerValue = "1"))
        assertThat(values.proxyMode).isEqualTo(ProxyMode.Pool)
        assertThat(values.proxyConfigId).isEqualTo(12)
        assertThat(values.channelProxy).isEqualTo("https://proxy.example.com")
        assertThat(values.matchRegex).isEqualTo("gpt.*")
        assertThat(values.paramOverride).isEqualTo("""{"temperature":0.2}""")
    }

    @Test
    fun updateRequestDiffsKeysAndAdvancedFields() {
        val channel = Channel(
            id = 1,
            name = "old",
            type = 0,
            baseUrls = listOf(BaseUrl("https://old.example.com")),
            keys = listOf(
                ChannelKey(id = 10, channelKey = "", enabled = true, remark = "old"),
                ChannelKey(id = 11, channelKey = "", enabled = true, remark = "delete"),
            ),
        )

        val request = buildChannelUpdateRequest(
            channel = channel,
            values = ChannelEditorValues(
                name = "new",
                type = 2,
                baseUrls = listOf(BaseUrl("https://new.example.com")),
                keys = listOf(
                    ChannelKeyEditorItem(id = 10, enabled = false, channelKey = "", remark = "changed"),
                    ChannelKeyEditorItem(enabled = true, channelKey = "sk-new", remark = "added"),
                ),
                customHeader = listOf(CustomHeader(headerKey = "X-Test", headerValue = "1")),
                proxyMode = ProxyMode.Pool,
                proxyConfigId = 5,
                channelProxy = "https://proxy.example.com",
                matchRegex = "claude.*",
                paramOverride = """{"max_tokens":1024}""",
            ),
        )

        assertThat(request.name).isEqualTo("new")
        assertThat(request.type).isEqualTo(2)
        assertThat(request.baseUrls).containsExactly(BaseUrl("https://new.example.com"))
        assertThat(request.keysToDelete).containsExactly(11)
        assertThat(request.keysToAdd).hasSize(1)
        assertThat(request.keysToAdd.single().channelKey).isEqualTo("sk-new")
        assertThat(request.keysToUpdate).hasSize(1)
        assertThat(request.keysToUpdate.single().id).isEqualTo(10)
        assertThat(request.keysToUpdate.single().enabled).isFalse()
        assertThat(request.keysToUpdate.single().channelKey).isNull()
        assertThat(request.keysToUpdate.single().remark).isEqualTo("changed")
        assertThat(request.customHeader).containsExactly(CustomHeader(headerKey = "X-Test", headerValue = "1"))
        assertThat(request.proxy).isTrue()
        assertThat(request.proxyMode).isEqualTo(ProxyMode.Pool)
        assertThat(request.proxyConfigId).isEqualTo(JsonPrimitive(5))
        assertThat(request.channelProxy).isEqualTo("https://proxy.example.com")
        assertThat(request.matchRegex).isEqualTo("claude.*")
        assertThat(request.paramOverride).isEqualTo("""{"max_tokens":1024}""")
    }

    @Test
    fun channelPoolProxyRequiresConfig() {
        val values = ChannelEditorValues(
            name = "new",
            baseUrls = listOf(BaseUrl("https://api.example.com")),
            keys = listOf(ChannelKeyEditorItem(channelKey = "sk-new")),
            proxyMode = ProxyMode.Pool,
        )

        assertThat(canSubmitChannelEditor(values, submitting = false)).isFalse()
        assertThat(canSubmitChannelEditor(values.copy(proxyConfigId = 5), submitting = false)).isTrue()
    }

    @Test
    fun channelUpdateClearsProxyConfigIdWhenLeavingPoolMode() {
        val channel = Channel(
            id = 1,
            name = "old",
            type = 0,
            baseUrls = listOf(BaseUrl("https://old.example.com")),
            keys = listOf(ChannelKey(id = 10, channelKey = "", enabled = true, remark = "old")),
            proxy = true,
            proxyMode = ProxyMode.Pool,
            proxyConfigId = 5,
        )

        val request = buildChannelUpdateRequest(
            channel = channel,
            values = channel.toEditorValues().copy(proxyMode = ProxyMode.Direct, proxyConfigId = null),
        )

        assertThat(request.proxy).isFalse()
        assertThat(request.proxyMode).isEqualTo(ProxyMode.Direct)
        assertThat(request.proxyConfigId).isEqualTo(JsonNull)
        assertThat(json.encodeToString(request)).contains(""""proxy_config_id":null""")
    }
}
