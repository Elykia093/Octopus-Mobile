package com.elykia.octopus.core.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

private val errorJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun Throwable.toUserMessage(fallback: String = "网络错误"): String {
    if (this is HttpException) {
        val message = response()?.errorBody()?.string().orEmpty().parseServerMessage()
        if (!message.isNullOrBlank()) {
            return message
        }
    }

    return message?.takeIf { it.isNotBlank() } ?: fallback
}

private fun String.parseServerMessage(): String? {
    if (isBlank()) {
        return null
    }

    return runCatching {
        val body = errorJson.parseToJsonElement(this).jsonObject
        body["message"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?: body["error_code"]
                ?.jsonPrimitive
                ?.contentOrNull
                ?.takeIf { it.isNotBlank() }
    }.getOrNull()
}
