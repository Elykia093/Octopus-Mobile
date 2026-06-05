package com.elykia.octopus.feature.group

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.LlmChannel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GroupStateTest {
    @Test
    fun pageErrorOnlyShowsWhenGroupRefreshFailsWithoutCachedGroups() {
        assertThat(GroupUiState(error = "groups failed").shouldShowPageError()).isTrue()

        assertThat(
            GroupUiState(
                groups = listOf(Group(id = 1, name = "old", mode = 1)),
                error = "groups failed",
            ).shouldShowPageError()
        ).isFalse()
    }

    @Test
    fun partialRefreshFailureKeepsPreviousCandidatesAndExposesErrors() {
        val previousChannels = listOf(Channel(id = 1, name = "OpenAI", type = 1))
        val previousModelChannels = listOf(
            LlmChannel(name = "gpt-test", enabled = true, channelId = 1, channelName = "OpenAI"),
        )
        val previous = GroupUiState(
            groups = listOf(Group(id = 1, name = "old", mode = 1)),
            channels = previousChannels,
            modelChannels = previousModelChannels,
        )

        val state = buildGroupRefreshState(
            previous = previous,
            groupsResult = AppResult.Success(listOf(Group(id = 2, name = "new", mode = 1))),
            channelsResult = AppResult.Error("channels failed"),
            modelChannelsResult = AppResult.Error("models failed"),
        )

        assertThat(state.loading).isFalse()
        assertThat(state.groups).containsExactly(Group(id = 2, name = "new", mode = 1))
        assertThat(state.channels).isEqualTo(previousChannels)
        assertThat(state.modelChannels).isEqualTo(previousModelChannels)
        assertThat(state.channelListError).isEqualTo("channels failed")
        assertThat(state.modelChannelError).isEqualTo("models failed")
    }

    @Test
    fun groupFailureKeepsPageLevelError() {
        val state = buildGroupRefreshState(
            previous = GroupUiState(groups = listOf(Group(id = 1, name = "old", mode = 1))),
            groupsResult = AppResult.Error("groups failed"),
            channelsResult = AppResult.Success(emptyList()),
            modelChannelsResult = AppResult.Success(emptyList()),
        )

        assertThat(state.loading).isFalse()
        assertThat(state.error).isEqualTo("groups failed")
        assertThat(state.groups).containsExactly(Group(id = 1, name = "old", mode = 1))
    }

    @Test
    fun groupFailureStillUpdatesRecoveredCandidatesAndClearsPartialErrors() {
        val recoveredChannels = listOf(Channel(id = 2, name = "Recovered", type = 1))
        val recoveredModelChannels = listOf(
            LlmChannel(name = "gpt-recovered", enabled = true, channelId = 2, channelName = "Recovered"),
        )
        val previous = GroupUiState(
            groups = listOf(Group(id = 1, name = "old", mode = 1)),
            channels = listOf(Channel(id = 1, name = "Stale", type = 1)),
            modelChannels = listOf(
                LlmChannel(name = "gpt-stale", enabled = true, channelId = 1, channelName = "Stale"),
            ),
            channelListError = "old channels failed",
            modelChannelError = "old models failed",
        )

        val state = buildGroupRefreshState(
            previous = previous,
            groupsResult = AppResult.Error("groups failed"),
            channelsResult = AppResult.Success(recoveredChannels),
            modelChannelsResult = AppResult.Success(recoveredModelChannels),
        )

        assertThat(state.loading).isFalse()
        assertThat(state.error).isEqualTo("groups failed")
        assertThat(state.groups).isEqualTo(previous.groups)
        assertThat(state.channels).isEqualTo(recoveredChannels)
        assertThat(state.modelChannels).isEqualTo(recoveredModelChannels)
        assertThat(state.channelListError).isNull()
        assertThat(state.modelChannelError).isNull()
    }
}
