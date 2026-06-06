package com.elykia.octopus.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val code: Int? = null,
    val message: String? = null,
    val data: T? = null,
)
