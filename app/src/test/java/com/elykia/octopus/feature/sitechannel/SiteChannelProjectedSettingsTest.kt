package com.elykia.octopus.feature.sitechannel

import com.elykia.octopus.core.data.model.SiteProjectedChannelSettings
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SiteChannelProjectedSettingsTest {
    @Test
    fun paramOverrideValidationAcceptsBlankAndJsonObjectsOnly() {
        val items = listOf(
            item(channelId = 1, paramOverride = ""),
            item(channelId = 2, paramOverride = """{"temperature":0.2}"""),
            item(channelId = 3, paramOverride = """["bad"]"""),
            item(channelId = 4, paramOverride = "true"),
            item(channelId = 5, paramOverride = "{bad"),
        )

        val invalidItems = invalidProjectedSettingsParamOverrideItems(items)

        assertThat(invalidItems.map { it.channelId }).containsExactly(3, 4, 5).inOrder()
    }

    @Test
    fun projectedSettingsRequestOnlyIncludesChangedTrimmedItems() {
        val original = listOf(
            SiteProjectedChannelSettings(channelId = 1, autoGroup = 1, paramOverride = """{"a":1}"""),
            SiteProjectedChannelSettings(channelId = 2, autoGroup = 0, paramOverride = ""),
        )
        val items = listOf(
            item(channelId = 1, autoGroup = 1, paramOverride = """ {"a":1} """),
            item(channelId = 2, autoGroup = 2, paramOverride = """ {"limit":10} """),
        )

        val request = buildProjectedSettingsRequest(original, items)

        assertThat(request).hasSize(1)
        assertThat(request.single().channelId).isEqualTo(2)
        assertThat(request.single().autoGroup).isEqualTo(2)
        assertThat(request.single().paramOverride).isEqualTo("""{"limit":10}""")
    }

    private fun item(
        channelId: Int,
        autoGroup: Int = 0,
        paramOverride: String,
    ): ProjectedSettingsEditorItem = ProjectedSettingsEditorItem(
        channelId = channelId,
        channelName = "Channel $channelId",
        autoGroup = autoGroup,
        paramOverride = paramOverride,
    )
}
