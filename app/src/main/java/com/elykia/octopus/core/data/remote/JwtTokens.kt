package com.elykia.octopus.core.data.remote

import java.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

internal object JwtTokens {
    private val json = Json { ignoreUnknownKeys = true }

    fun authorizationHeader(token: String): String {
        val normalized = token.trim()
        return if (normalized.startsWith("Bearer ", ignoreCase = true)) {
            normalized
        } else {
            "Bearer $normalized"
        }
    }

    fun isExpired(token: String, nowEpochSeconds: Long = System.currentTimeMillis() / 1000L): Boolean {
        val rawToken = token.trim().removeBearerPrefix()
        val payload = rawToken.split('.').getOrNull(1) ?: return false
        val decodedPayload = decodeUrlBase64(payload) ?: return false
        val exp = runCatching {
            json.parseToJsonElement(decodedPayload)
                .jsonObject["exp"]
                ?.jsonPrimitive
                ?.longOrNull
        }.getOrNull() ?: return false

        return exp <= nowEpochSeconds
    }

    private fun String.removeBearerPrefix(): String {
        return if (startsWith("Bearer ", ignoreCase = true)) {
            substringAfter(' ').trim()
        } else {
            this
        }
    }

    private fun decodeUrlBase64(value: String): String? {
        val padded = when (value.length % 4) {
            0 -> value
            2 -> "$value=="
            3 -> "$value="
            else -> return null
        }

        return runCatching {
            Base64.getUrlDecoder().decode(padded).toString(Charsets.UTF_8)
        }.getOrNull()
    }
}
