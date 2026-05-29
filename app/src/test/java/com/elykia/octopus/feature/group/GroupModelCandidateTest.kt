package com.elykia.octopus.feature.group

import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.data.model.LlmChannel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GroupModelCandidateTest {
    @Test
    fun buildGroupModelCandidatesUsesModelChannelMembersWhenAvailable() {
        val candidates = buildGroupModelCandidates(
            channels = listOf(
                Channel(id = 7, name = "OpenAI", type = 1, model = "fallback-only"),
            ),
            modelChannels = listOf(
                LlmChannel(name = " gpt-4o ", enabled = true, channelId = 7, channelName = ""),
                LlmChannel(name = "gpt-4o-mini", enabled = false, channelId = 7, channelName = "OpenAI mirror"),
                LlmChannel(name = "", enabled = true, channelId = 8, channelName = "Blank"),
            ),
        )

        assertThat(candidates).containsExactly(
            GroupModelCandidate(channelId = 7, channelName = "OpenAI", modelName = "gpt-4o", enabled = true),
            GroupModelCandidate(channelId = 7, channelName = "OpenAI mirror", modelName = "gpt-4o-mini", enabled = false),
        ).inOrder()
    }

    @Test
    fun buildGroupModelCandidatesFallsBackToChannelModelText() {
        val candidates = buildGroupModelCandidates(
            channels = listOf(
                Channel(
                    id = 2,
                    name = "Claude",
                    type = 1,
                    model = "claude-3-5, claude-3-haiku",
                    customModel = "claude-3-5\nclaude-3-opus",
                    enabled = false,
                ),
                Channel(id = 1, name = "Unnamed fallback", type = 1),
            ),
            modelChannels = emptyList(),
        )

        assertThat(candidates).containsExactly(
            GroupModelCandidate(channelId = 1, channelName = "Unnamed fallback", modelName = "Unnamed fallback", enabled = true),
            GroupModelCandidate(channelId = 2, channelName = "Claude", modelName = "claude-3-5", enabled = false),
            GroupModelCandidate(channelId = 2, channelName = "Claude", modelName = "claude-3-haiku", enabled = false),
            GroupModelCandidate(channelId = 2, channelName = "Claude", modelName = "claude-3-opus", enabled = false),
        ).inOrder()
    }

    @Test
    fun nextGroupItemPriorityAppendsAfterHighestPriority() {
        val nextPriority = nextGroupItemPriority(
            listOf(
                GroupItem(channelId = 1, modelName = "a", priority = 1, weight = 1),
                GroupItem(channelId = 1, modelName = "b", priority = 9, weight = 1),
            ),
        )

        assertThat(nextPriority).isEqualTo(10)
    }

    @Test
    fun findMatchingGroupModelCandidatesUsesRegexBeforeName() {
        val candidates = listOf(
            GroupModelCandidate(channelId = 1, channelName = "OpenAI", modelName = "gpt-4o", enabled = true),
            GroupModelCandidate(channelId = 2, channelName = "Anthropic", modelName = "claude-3-5", enabled = true),
            GroupModelCandidate(channelId = 3, channelName = "DeepSeek", modelName = "deepseek-chat", enabled = true),
        )

        val matches = findMatchingGroupModelCandidates(
            candidates = candidates,
            name = "gpt",
            matchRegex = "claude|deepseek",
            selectedKeys = setOf(GroupModelCandidateKey(channelId = 3, modelName = "deepseek-chat")),
        )

        assertThat(matches).containsExactly(
            GroupModelCandidate(channelId = 2, channelName = "Anthropic", modelName = "claude-3-5", enabled = true),
        )
    }

    @Test
    fun findMatchingGroupModelCandidatesFallsBackToGroupNameContains() {
        val candidates = listOf(
            GroupModelCandidate(channelId = 1, channelName = "OpenAI", modelName = "gpt-4o", enabled = true),
            GroupModelCandidate(channelId = 2, channelName = "OpenAI", modelName = "gpt-4o-mini", enabled = true),
            GroupModelCandidate(channelId = 3, channelName = "Anthropic", modelName = "claude-3-5", enabled = true),
        )

        val matches = findMatchingGroupModelCandidates(
            candidates = candidates,
            name = "GPT",
            matchRegex = "",
            selectedKeys = setOf(GroupModelCandidateKey(channelId = 1, modelName = "gpt-4o")),
        )

        assertThat(matches).containsExactly(
            GroupModelCandidate(channelId = 2, channelName = "OpenAI", modelName = "gpt-4o-mini", enabled = true),
        )
    }
}
