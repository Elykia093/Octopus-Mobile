package com.elykia.octopus.feature.log

import com.elykia.octopus.core.data.model.ChannelAttempt
import com.elykia.octopus.core.data.model.RelayLog

internal data class LogAttemptSummary(
    val channelId: Int,
    val channelKeyId: Int?,
    val channelName: String,
    val modelName: String,
    val status: String,
    val durationMs: Int,
    val message: String?,
    val firstAttemptNum: Int,
    val lastAttemptNum: Int,
    val repeat: Int,
    val sticky: Boolean,
)

internal fun RelayLog.displayAttemptCount(): Int =
    totalAttempts.takeIf { it > 0 } ?: attempts.size

internal fun RelayLog.hasInputTokenDetails(): Boolean =
    transportInputTokens != null ||
        billInputTokens != null ||
        cacheReadTokens != null ||
        cacheWriteTokens != null

internal fun RelayLog.headlineInputTokens(): Int =
    if (cacheReadTokens != null || cacheWriteTokens != null) {
        inputTokens + (cacheWriteTokens ?: 0)
    } else {
        inputTokens
    }

internal fun mergeAdjacentAttempts(attempts: List<ChannelAttempt>): List<LogAttemptSummary> {
    val merged = mutableListOf<LogAttemptSummary>()
    attempts.forEach { attempt ->
        val last = merged.lastOrNull()
        if (
            last != null &&
            last.channelId == attempt.channelId &&
            last.channelKeyId == attempt.channelKeyId &&
            last.channelName == attempt.channelName &&
            last.modelName == attempt.modelName &&
            last.status == attempt.status &&
            last.message.orEmpty() == attempt.msg.orEmpty()
        ) {
            merged[merged.lastIndex] = last.copy(
                durationMs = last.durationMs + attempt.duration,
                lastAttemptNum = attempt.attemptNum,
                repeat = last.repeat + 1,
                sticky = last.sticky || attempt.sticky == true,
            )
        } else {
            merged += LogAttemptSummary(
                channelId = attempt.channelId,
                channelKeyId = attempt.channelKeyId,
                channelName = attempt.channelName,
                modelName = attempt.modelName,
                status = attempt.status,
                durationMs = attempt.duration,
                message = attempt.msg,
                firstAttemptNum = attempt.attemptNum,
                lastAttemptNum = attempt.attemptNum,
                repeat = 1,
                sticky = attempt.sticky == true,
            )
        }
    }
    return merged
}
