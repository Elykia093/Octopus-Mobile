package com.elykia.octopus.core.data.remote

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object ServerUrlResolver {
    fun normalize(rawBaseUrl: String): HttpUrl? {
        val trimmed = rawBaseUrl.trim().trimEnd('/')
        if (trimmed.isBlank()) {
            return null
        }

        val withApiPath = if (trimmed.endsWith("/api/v1", ignoreCase = true)) {
            trimmed
        } else {
            "$trimmed/api/v1"
        }

        return withApiPath.plus("/").toHttpUrlOrNull()
    }

    fun merge(baseUrl: HttpUrl, requestUrl: HttpUrl): HttpUrl {
        val baseSegments = baseUrl.pathSegments.filter { it.isNotEmpty() }
        var requestSegments = requestUrl.pathSegments.filter { it.isNotEmpty() }
        val overlap = findPathOverlap(baseSegments, requestSegments)
        if (overlap > 0) {
            requestSegments = requestSegments.drop(overlap)
        }

        val mergedSegments = baseSegments + requestSegments
        return requestUrl.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .encodedPath("/" + mergedSegments.joinToString("/"))
            .build()
    }

    private fun findPathOverlap(baseSegments: List<String>, requestSegments: List<String>): Int {
        var overlap = 0
        val maxOverlap = minOf(baseSegments.size, requestSegments.size)
        for (size in 1..maxOverlap) {
            if (baseSegments.takeLast(size) == requestSegments.take(size)) {
                overlap = size
            }
        }
        return overlap
    }
}
