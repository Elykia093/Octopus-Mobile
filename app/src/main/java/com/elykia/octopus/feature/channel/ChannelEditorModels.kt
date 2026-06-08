package com.elykia.octopus.feature.channel

import com.elykia.octopus.core.data.model.BaseUrl
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.CustomHeader

data class ChannelKeyEditorItem(
    val id: Int? = null,
    val enabled: Boolean = true,
    val channelKey: String = "",
    val remark: String = "",
)

data class ChannelEditorValues(
    val name: String = "",
    val type: Int = 0,
    val enabled: Boolean = true,
    val baseUrls: List<BaseUrl> = listOf(BaseUrl(url = "")),
    val keys: List<ChannelKeyEditorItem> = listOf(ChannelKeyEditorItem()),
    val model: String = "",
    val customModel: String = "",
    val proxy: Boolean = false,
    val autoSync: Boolean = false,
    val autoGroup: Int = 0,
    val customHeader: List<CustomHeader> = listOf(CustomHeader(headerKey = "", headerValue = "")),
    val channelProxy: String = "",
    val paramOverride: String = "",
    val matchRegex: String = "",
)

fun Channel?.toEditorValues(): ChannelEditorValues {
    val channel = this ?: return ChannelEditorValues()
    return ChannelEditorValues(
        name = channel.name,
        type = channel.type,
        enabled = channel.enabled,
        baseUrls = channel.baseUrls.takeIf { it.isNotEmpty() } ?: listOf(BaseUrl(url = "")),
        keys = channel.keys
            .map { key ->
                ChannelKeyEditorItem(
                    id = key.id,
                    enabled = key.enabled,
                    channelKey = "",
                    remark = key.remark,
                )
            }
            .takeIf { it.isNotEmpty() }
            ?: listOf(ChannelKeyEditorItem()),
        model = channel.model,
        customModel = channel.customModel,
        proxy = channel.proxy,
        autoSync = channel.autoSync,
        autoGroup = channel.autoGroup,
        customHeader = channel.customHeader.takeIf { it.isNotEmpty() } ?: listOf(CustomHeader(headerKey = "", headerValue = "")),
        channelProxy = channel.channelProxy.orEmpty(),
        paramOverride = channel.paramOverride.orEmpty(),
        matchRegex = channel.matchRegex.orEmpty(),
    )
}
