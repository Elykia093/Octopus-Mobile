package com.elykia.octopus.feature.sitechannel

import com.elykia.octopus.core.data.model.SiteChannelModel
import com.elykia.octopus.core.data.model.SiteModelRouteType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SiteChannelBatchModelsTest {
    @Test
    fun bulkRouteRequestsSkipDisabledUnchangedBlankAndDuplicateModels() {
        val requests = buildBulkModelRouteRequests(
            groupKey = " default ",
            routeType = SiteModelRouteType.OpenAiResponse,
            models = listOf(
                model(" gpt-4o ", routeType = SiteModelRouteType.OpenAiChat),
                model("gpt-4o", routeType = SiteModelRouteType.OpenAiChat),
                model("same", routeType = SiteModelRouteType.OpenAiResponse),
                model("disabled", routeType = SiteModelRouteType.OpenAiChat, disabled = true),
                model(""),
            ),
        )

        assertThat(requests).hasSize(1)
        assertThat(requests.single().groupKey).isEqualTo("default")
        assertThat(requests.single().modelName).isEqualTo("gpt-4o")
        assertThat(requests.single().routeType).isEqualTo(SiteModelRouteType.OpenAiResponse)
    }

    @Test
    fun bulkRouteRequestsRequireGroupAndRoute() {
        assertThat(
            buildBulkModelRouteRequests(
                groupKey = "",
                routeType = SiteModelRouteType.OpenAiChat,
                models = listOf(model("gpt-4o")),
            ),
        ).isEmpty()

        assertThat(
            buildBulkModelRouteRequests(
                groupKey = "default",
                routeType = "",
                models = listOf(model("gpt-4o")),
            ),
        ).isEmpty()
    }

    @Test
    fun bulkDisabledRequestsOnlyIncludeModelsThatChangeState() {
        val disableRequests = buildBulkModelDisabledRequests(
            groupKey = "default",
            disabled = true,
            models = listOf(
                model(" enabled-a "),
                model("enabled-a"),
                model("already-disabled", disabled = true),
                model(""),
            ),
        )
        val enableRequests = buildBulkModelDisabledRequests(
            groupKey = "default",
            disabled = false,
            models = listOf(
                model("disabled-a", disabled = true),
                model("already-enabled", disabled = false),
            ),
        )

        assertThat(disableRequests.map { it.modelName }).containsExactly("enabled-a")
        assertThat(disableRequests.single().disabled).isTrue()
        assertThat(enableRequests.map { it.modelName }).containsExactly("disabled-a")
        assertThat(enableRequests.single().disabled).isFalse()
    }

    private fun model(
        name: String,
        routeType: String = SiteModelRouteType.OpenAiChat,
        disabled: Boolean = false,
    ): SiteChannelModel = SiteChannelModel(
        modelName = name,
        routeType = routeType,
        disabled = disabled,
    )
}
