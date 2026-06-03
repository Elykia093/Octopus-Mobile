package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ServerConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private const val DEFAULT_SERVER_URL = "https://127.0.0.1:8080/"

internal object ServerUrlResolver {
    private val defaultUrl = DEFAULT_SERVER_URL.toHttpUrl()

    fun normalize(config: ServerConfig): HttpUrl {
        val rawBaseUrl = config.baseUrl.trim()
        if (rawBaseUrl.isBlank()) {
            return defaultUrl
        }

        return rawBaseUrl.trimEnd('/')
            .plus("/")
            .toHttpUrlOrNull()
            ?.takeIf {
                it.scheme == "https" &&
                    it.encodedUsername.isBlank() &&
                    it.encodedPassword.isBlank() &&
                    it.encodedQuery == null &&
                    it.encodedFragment == null
            }
            ?: defaultUrl
    }

    fun merge(baseUrl: HttpUrl, requestUrl: HttpUrl): HttpUrl {
        val requestSegments = requestUrl.pathSegments.filter { it.isNotEmpty() }
        val mergedSegments = buildList {
            addAll(baseUrl.pathSegments.filter { it.isNotEmpty() })
            addAll(requestSegments)
        }

        return requestUrl.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .encodedPath("/" + mergedSegments.joinToString("/"))
            .build()
    }
}
