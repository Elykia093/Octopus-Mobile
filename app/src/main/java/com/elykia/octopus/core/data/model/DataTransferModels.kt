package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImportResult(
    @SerialName("rows_affected") val rowsAffected: Map<String, Int> = emptyMap(),
)
