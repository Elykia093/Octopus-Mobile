package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LatestInfo(
    @SerialName("tag_name") val tagName: String,
    @SerialName("published_at") val publishedAt: String,
    val body: String = "",
    val message: String = "",
)
