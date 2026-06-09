package com.elykia.octopus.feature.channel

import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.ChannelKeyAddRequest
import com.elykia.octopus.core.data.model.ChannelKeyUpdateRequest
import com.elykia.octopus.core.data.model.ChannelUpdateRequest
import com.elykia.octopus.core.data.model.CustomHeader
import com.elykia.octopus.core.data.model.ProxyMode
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
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
 * 检查一组 Channel 基础 URL 是否可提交。
 */
fun hasValidChannelBaseUrls(baseUrls: List<com.elykia.octopus.core.data.model.BaseUrl>): Boolean {
    val visibleUrls = baseUrls.map { it.url.trim() }.filter { it.isNotBlank() }
    return visibleUrls.isNotEmpty() && visibleUrls.all(::hasValidChannelBaseUrl)
}

/**
 * 检查编辑器中是否至少保留一个可用 Key。
 * 已有 Key 的真实值在移动端会被隐藏，因此只要存在 id 就视为保留。
 */
fun hasValidChannelKeys(keys: List<ChannelKeyEditorItem>): Boolean =
    keys.any { it.id != null || it.channelKey.isNotBlank() }

/**
 * 检查是否可以提交 Channel 编辑器。
 */
fun canSubmitChannelEditor(
    values: ChannelEditorValues,
    submitting: Boolean,
): Boolean =
    !submitting &&
        values.name.isNotBlank() &&
        hasValidChannelBaseUrls(values.baseUrls) &&
        hasValidChannelKeys(values.keys) &&
        hasValidChannelProxySelection(values.proxyMode, values.proxyConfigId)

fun hasValidChannelProxySelection(proxyMode: String, proxyConfigId: Int?): Boolean =
    proxyMode != ProxyMode.Pool || proxyConfigId != null

internal fun ChannelEditorValues.normalizedBaseUrls(): List<com.elykia.octopus.core.data.model.BaseUrl> =
    baseUrls
        .map { it.copy(url = it.url.trim(), delay = it.delay.coerceAtLeast(0)) }
        .filter { it.url.isNotBlank() }

internal fun ChannelEditorValues.normalizedHeaders(): List<CustomHeader> =
    customHeader
        .map { CustomHeader(headerKey = it.headerKey.trim(), headerValue = it.headerValue) }
        .filter { it.headerKey.isNotBlank() && it.headerValue.isNotBlank() }

internal fun ChannelEditorValues.trimmedChannelProxy(): String =
    channelProxy.trim()

internal fun ChannelEditorValues.trimmedParamOverride(): String =
    paramOverride.trim()

internal fun ChannelEditorValues.trimmedMatchRegex(): String =
    matchRegex.trim()

internal fun buildChannelUpdateKeys(
    channel: Channel,
    values: ChannelEditorValues,
): Triple<List<ChannelKeyAddRequest>, List<ChannelKeyUpdateRequest>, List<Int>> {
    val originalById = channel.keys.associateBy { it.id }
    val nextIds = values.keys.mapNotNull { it.id }.toSet()
    val keysToDelete = channel.keys.filter { it.id !in nextIds }.map { it.id }
    val keysToAdd = values.keys
        .filter { it.id == null && it.channelKey.trim().isNotBlank() }
        .map {
            ChannelKeyAddRequest(
                enabled = it.enabled,
                channelKey = it.channelKey.trim(),
                remark = it.remark.trim(),
            )
        }
    val keysToUpdate = values.keys
        .mapNotNull { key ->
            val id = key.id ?: return@mapNotNull null
            val original = originalById[id] ?: return@mapNotNull null
            val nextKey = key.channelKey.trim()
            val enabledChanged = key.enabled != original.enabled
            val remarkChanged = key.remark.trim() != original.remark
            if (!enabledChanged && !remarkChanged && nextKey.isBlank()) {
                return@mapNotNull null
            }
            ChannelKeyUpdateRequest(
                id = id,
                enabled = key.enabled.takeIf { enabledChanged },
                channelKey = nextKey.takeIf { it.isNotBlank() },
                remark = key.remark.trim().takeIf { remarkChanged },
            )
        }
    return Triple(keysToAdd, keysToUpdate, keysToDelete)
}

internal fun buildChannelUpdateRequest(
    channel: Channel,
    values: ChannelEditorValues,
): ChannelUpdateRequest {
    val baseUrls = values.normalizedBaseUrls()
    val headers = values.normalizedHeaders()
    val channelProxy = values.trimmedChannelProxy()
    val paramOverride = values.trimmedParamOverride()
    val matchRegex = values.trimmedMatchRegex()
    val (keysToAdd, keysToUpdate, keysToDelete) = buildChannelUpdateKeys(channel, values)

    return ChannelUpdateRequest(
        id = channel.id,
        name = values.name.trim().takeIf { it != channel.name },
        type = values.type.takeIf { it != channel.type },
        enabled = values.enabled.takeIf { it != channel.enabled },
        baseUrls = baseUrls.takeIf { it != channel.baseUrls },
        model = values.model.trim().takeIf { it != channel.model },
        customModel = values.customModel.trim().takeIf { it != channel.customModel },
        proxy = (values.proxyMode != ProxyMode.Direct).takeIf { it != channel.proxy },
        proxyMode = values.proxyMode.takeIf { it != channel.proxyMode },
        proxyConfigId = buildProxyConfigIdPatch(
            proxyMode = values.proxyMode,
            proxyConfigId = values.proxyConfigId,
            originalProxyMode = channel.proxyMode,
            originalProxyConfigId = channel.proxyConfigId,
        ),
        autoSync = values.autoSync.takeIf { it != channel.autoSync },
        autoGroup = values.autoGroup.takeIf { it != channel.autoGroup },
        customHeader = headers.takeIf { it != channel.customHeader },
        channelProxy = channelProxy.takeIf { it != channel.channelProxy.orEmpty() },
        paramOverride = paramOverride.takeIf { it != channel.paramOverride.orEmpty() },
        matchRegex = matchRegex.takeIf { it != channel.matchRegex.orEmpty() },
        keysToAdd = keysToAdd,
        keysToUpdate = keysToUpdate,
        keysToDelete = keysToDelete,
    )
}

private fun buildProxyConfigIdPatch(
    proxyMode: String,
    proxyConfigId: Int?,
    originalProxyMode: String,
    originalProxyConfigId: Int?,
): JsonElement? = when {
    proxyMode == ProxyMode.Pool && (proxyConfigId != originalProxyConfigId || proxyMode != originalProxyMode) ->
        JsonPrimitive(proxyConfigId ?: return null)
    proxyMode != ProxyMode.Pool && (originalProxyMode == ProxyMode.Pool || originalProxyConfigId != null) ->
        JsonNull
    else -> null
}
