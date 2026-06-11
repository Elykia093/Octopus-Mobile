package com.elykia.octopus.feature.apikey

internal fun apiKeyBatchProgressMessage(
    action: ApiKeyBatchProgressAction,
    current: Int,
    total: Int,
): String = when (action) {
    ApiKeyBatchProgressAction.Delete -> "Deleting $current/$total..."
    ApiKeyBatchProgressAction.Enable -> "Enabling $current/$total..."
    ApiKeyBatchProgressAction.Disable -> "Disabling $current/$total..."
}

internal fun apiKeyBatchResultMessage(successCount: Int, failCount: Int): String =
    "$successCount succeeded, $failCount failed"

internal enum class ApiKeyBatchProgressAction {
    Delete,
    Enable,
    Disable,
}
