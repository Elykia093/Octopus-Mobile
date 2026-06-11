package com.elykia.octopus.feature.site

import com.elykia.octopus.core.data.model.AllApiHubImportResult
import com.elykia.octopus.core.data.model.MetApiImportResult
import com.elykia.octopus.core.data.model.SiteBatchAction
import com.elykia.octopus.core.data.model.SiteBatchActionResult
import com.elykia.octopus.core.data.model.SiteCheckinResult
import com.elykia.octopus.core.data.model.SiteSyncResult

internal fun SiteSyncResult.toSiteSyncOperationMessage(): String {
    val label = siteOperationStatusLabel(status)
    val body = message.ifBlank { label }
    return "同步$label：$body（分组 $groupCount，Key $tokenCount，模型 $modelCount）"
}

internal fun SiteCheckinResult.toSiteCheckinOperationMessage(): String {
    val label = siteOperationStatusLabel(status)
    val body = message.ifBlank { label }
    val rewardSuffix = reward?.takeIf { it.isNotBlank() }?.let { "，奖励：$it" }.orEmpty()
    return "签到$label：$body$rewardSuffix"
}

internal fun String.isFailedSiteOperationStatus(): Boolean =
    equals("failed", ignoreCase = true)

internal fun AllApiHubImportResult.toAllApiHubImportOperationMessage(): String =
    "All API Hub import completed: created sites $createdSites, reused sites $reusedSites, " +
        "created accounts $createdAccounts, updated accounts $updatedAccounts, skipped accounts $skippedAccounts, " +
        "scheduled sync $scheduledSyncAccounts."

internal fun MetApiImportResult.toMetApiImportOperationMessage(): String =
    "MetAPI import completed: created sites $createdSites, reused sites $reusedSites, " +
        "created accounts $createdAccounts, updated accounts $updatedAccounts, skipped accounts $skippedAccounts, " +
        "imported tokens $importedTokens, groups $importedGroups, models $importedModels."

internal fun siteBatchActionLabel(action: String): String = when (action) {
    SiteBatchAction.Enable -> "enable"
    SiteBatchAction.Disable -> "disable"
    SiteBatchAction.Delete -> "delete"
    else -> "operation"
}

internal fun SiteBatchActionResult.toSiteBatchOperationMessage(actionLabel: String): String {
    val failedCount = failedItems.size
    if (failedCount == 0) {
        return "Batch $actionLabel completed: ${successIds.size} succeeded."
    }
    val failedSummary = failedItems.take(3).joinToString("; ") { item ->
        "#${item.id} ${item.message.ifBlank { "Unknown error" }}"
    }
    val suffix = if (failedCount > 3) "; ${failedCount - 3} more failed" else ""
    return "Batch $actionLabel completed: ${successIds.size} succeeded, $failedCount failed: $failedSummary$suffix."
}

private fun siteOperationStatusLabel(status: String): String = when (status) {
    "success" -> "成功"
    "partial" -> "部分成功"
    "failed" -> "失败"
    "skipped" -> "跳过"
    else -> "完成"
}
