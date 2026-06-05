package com.elykia.octopus.feature.channel

import com.elykia.octopus.core.data.model.Channel
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * 检查 Channel 基础 URL 是否有效
 * 空白或 HTTPS URL（不包含用户名、密码、查询参数或 fragment）都视为有效
 */
fun hasValidChannelBaseUrl(baseUrl: String): Boolean =
    baseUrl.isBlank() || baseUrl.trim().toHttpUrlOrNull()?.let { url ->
        url.scheme == "https" &&
            url.encodedUsername.isBlank() &&
            url.encodedPassword.isBlank() &&
            url.encodedQuery == null &&
            url.encodedFragment == null
    } == true

/**
 * 检查是否可以提交 Channel 编辑器
 */
fun canSubmitChannelEditor(
    name: String,
    baseUrl: String,
    submitting: Boolean,
    basicEditSupported: Boolean,
): Boolean =
    !submitting &&
        basicEditSupported &&
        name.isNotBlank() &&
        hasValidChannelBaseUrl(baseUrl)

/**
 * 检查 Channel 是否可以使用移动端基础编辑器
 * 基础编辑器只支持单一 baseUrl、单一 key、无高级配置的 Channel
 */
fun Channel.canUseBasicMobileEditor(): Boolean =
    baseUrls.size <= 1 &&
        keys.size <= 1 &&
        customHeader.isEmpty() &&
        channelProxy.isNullOrBlank() &&
        matchRegex.isNullOrBlank() &&
        paramOverride.isNullOrBlank()
