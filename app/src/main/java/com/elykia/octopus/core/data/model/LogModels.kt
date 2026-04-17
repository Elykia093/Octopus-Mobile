package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogItem(
    val id: Long,
    val type: Int,
    @SerialName("created_at") val createdAt: Long,
    val content: String = "",
    val model: String = "",
    val prompt_tokens: Int = 0,
    val completion_tokens: Int = 0,
    val use_time: Int = 0,
    val channel_id: Int = 0,
    val token_name: String = "",
    val username: String = "",
)

@Serializable
data class LogPageResponse(
    val list: List<LogItem>,
    val total: Long
)
