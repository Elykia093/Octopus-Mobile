package com.elykia.octopus.feature.group

import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.LlmChannel

internal data class GroupModelCandidateKey(
    val channelId: Int,
    val modelName: String,
)

internal data class GroupModelCandidate(
    val channelId: Int,
    val channelName: String,
    val modelName: String,
    val enabled: Boolean,
) {
    val key: GroupModelCandidateKey
        get() = GroupModelCandidateKey(channelId, modelName)
}

internal fun buildGroupModelCandidates(
    channels: List<Channel>,
    modelChannels: List<LlmChannel>,
): List<GroupModelCandidate> {
    val channelsById = channels.associateBy { it.id }
    val fromModelChannels = modelChannels.mapNotNull { modelChannel ->
        val modelName = modelChannel.name.trim()
        if (modelName.isBlank()) return@mapNotNull null

        GroupModelCandidate(
            channelId = modelChannel.channelId,
            channelName = modelChannel.channelName.ifBlank {
                channelsById[modelChannel.channelId]?.name ?: "Channel ${modelChannel.channelId}"
            },
            modelName = modelName,
            enabled = modelChannel.enabled,
        )
    }

    return (fromModelChannels.ifEmpty {
        channels.flatMap { channel ->
            parseGroupModelNames(channel.model, channel.customModel).ifEmpty { listOf(channel.name) }
                .map { modelName ->
                    GroupModelCandidate(
                        channelId = channel.id,
                        channelName = channel.name,
                        modelName = modelName,
                        enabled = channel.enabled,
                    )
                }
        }
    })
        .distinctBy { it.key }
        .sortedWith(compareBy<GroupModelCandidate> { it.channelId }.thenBy { it.modelName.lowercase() })
}

internal fun findMatchingGroupModelCandidates(
    candidates: List<GroupModelCandidate>,
    name: String,
    matchRegex: String,
    selectedKeys: Set<GroupModelCandidateKey>,
): List<GroupModelCandidate> {
    val availableCandidates = candidates.filterNot { it.key in selectedKeys }
    val regexText = matchRegex.trim()
    if (regexText.isNotBlank()) {
        val regex = runCatching { Regex(regexText) }.getOrNull() ?: return emptyList()
        return availableCandidates.filter { regex.containsMatchIn(it.modelName) }
    }

    val groupKey = name.trim().lowercase()
    if (groupKey.isBlank()) return emptyList()
    return availableCandidates.filter { it.modelName.lowercase().contains(groupKey) }
}

private fun parseGroupModelNames(vararg values: String): List<String> = values
    .flatMap { value -> value.split(Regex("[,;\\r\\n]+")) }
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .distinct()
