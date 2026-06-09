package com.elykia.octopus.feature.proxypool

import com.elykia.octopus.core.data.model.ProxyConfiguration
import com.elykia.octopus.core.data.model.ProxyConfigurationCreateRequest
import com.elykia.octopus.core.data.model.ProxyConfigurationUpdateRequest
import java.net.URI

data class ProxyPoolEditorValues(
    val name: String = "",
    val url: String = "",
    val enabled: Boolean = true,
    val remark: String = "",
)

fun ProxyConfiguration?.toEditorValues(): ProxyPoolEditorValues =
    if (this == null) {
        ProxyPoolEditorValues()
    } else {
        ProxyPoolEditorValues(
            name = name,
            url = url,
            enabled = enabled,
            remark = remark,
        )
    }

fun canSubmitProxyConfiguration(values: ProxyPoolEditorValues, submitting: Boolean): Boolean =
    !submitting && values.name.trim().isNotEmpty() && values.url.isValidProxyUrl()

fun ProxyPoolEditorValues.toCreateRequest(): ProxyConfigurationCreateRequest =
    ProxyConfigurationCreateRequest(
        name = name.trim(),
        url = url.trim(),
        enabled = enabled,
        remark = remark.trim(),
    )

fun ProxyPoolEditorValues.toUpdateRequest(proxy: ProxyConfiguration): ProxyConfigurationUpdateRequest =
    ProxyConfigurationUpdateRequest(
        id = proxy.id,
        name = name.trim(),
        url = url.trim(),
        enabled = enabled,
        remark = remark.trim(),
    )

fun String.isValidProxyUrl(): Boolean {
    val value = trim()
    if (value.isBlank()) return false
    val uri = runCatching { URI(value) }.getOrNull() ?: return false
    val scheme = uri.scheme?.lowercase() ?: return false
    if (scheme !in VALID_PROXY_SCHEMES) return false
    return !uri.host.isNullOrBlank()
}

fun maskProxyUrl(value: String): String {
    val uri = runCatching { URI(value) }.getOrNull() ?: return value
    val userInfo = uri.userInfo ?: return value
    val maskedUserInfo = if (":" in userInfo) {
        "${userInfo.substringBefore(':')}:***"
    } else {
        "***"
    }
    return runCatching {
        URI(uri.scheme, maskedUserInfo, uri.host, uri.port, uri.path, uri.query, uri.fragment).toString()
    }.getOrDefault(value)
}

const val DEFAULT_PROXY_TEST_URL = "https://api.openai.com/v1/models"

private val VALID_PROXY_SCHEMES = setOf("http", "https", "socks4", "socks5")
