package com.elykia.octopus.feature.apikey

import com.elykia.octopus.core.data.model.Group

internal fun parseApiKeyModelRestrictions(value: String?): List<String> =
    value.orEmpty()
        .split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

internal fun formatApiKeyModelRestrictions(models: List<String>): String =
    models
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(",")

internal fun toggleApiKeyModelRestriction(current: String?, model: String): String {
    val normalizedModel = model.trim()
    if (normalizedModel.isBlank()) return current.orEmpty().trim()

    val models = parseApiKeyModelRestrictions(current)
    val next = if (normalizedModel in models) {
        models - normalizedModel
    } else {
        models + normalizedModel
    }
    return formatApiKeyModelRestrictions(next)
}

internal fun apiKeyModelRestrictionCandidates(groups: List<Group>): List<String> =
    groups
        .map { it.name.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()
