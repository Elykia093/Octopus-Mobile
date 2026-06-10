package com.elykia.octopus.feature.site

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

private fun siteOperationStatusLabel(status: String): String = when (status) {
    "success" -> "成功"
    "partial" -> "部分成功"
    "failed" -> "失败"
    "skipped" -> "跳过"
    else -> "完成"
}
