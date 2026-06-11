package com.elykia.octopus.feature.group

internal fun groupBatchProgressMessage(current: Int, total: Int): String =
    "Deleting $current/$total..."

internal fun groupBatchResultMessage(successCount: Int, failCount: Int): String =
    "$successCount succeeded, $failCount failed"
