package com.elykia.octopus.feature.channel

internal fun channelBatchProgressMessage(
    action: ChannelBatchProgressAction,
    current: Int,
    total: Int,
): String = when (action) {
    ChannelBatchProgressAction.Delete -> "Deleting $current/$total..."
    ChannelBatchProgressAction.Enable -> "Enabling $current/$total..."
    ChannelBatchProgressAction.Disable -> "Disabling $current/$total..."
}

internal fun channelBatchResultMessage(successCount: Int, failCount: Int): String =
    "$successCount succeeded, $failCount failed"

internal enum class ChannelBatchProgressAction {
    Delete,
    Enable,
    Disable,
}
