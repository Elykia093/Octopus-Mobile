package com.elykia.octopus.feature.apikey

internal fun String.maskApiKey(): String = when {
    isBlank() -> ""
    length <= 4 -> "****"
    length <= 12 -> take(2) + "..." + takeLast(2)
    else -> take(8) + "..." + takeLast(4)
}

internal enum class ApiKeyEditorValidationIssue {
    InvalidExpireAt,
    InvalidMaxCost,
}

internal data class ApiKeyEditorValues(
    val expireAt: Long,
    val maxCost: Double,
)

internal fun parseApiKeyEditorValues(
    expireAt: String,
    maxCost: String,
): Result<ApiKeyEditorValues> {
    val parsedExpireAt = parseOptionalNonNegativeLong(expireAt)
        ?: return Result.failure(ApiKeyEditorValidationException(ApiKeyEditorValidationIssue.InvalidExpireAt))
    val parsedMaxCost = parseOptionalNonNegativeFiniteDouble(maxCost)
        ?: return Result.failure(ApiKeyEditorValidationException(ApiKeyEditorValidationIssue.InvalidMaxCost))

    return Result.success(
        ApiKeyEditorValues(
            expireAt = parsedExpireAt,
            maxCost = parsedMaxCost,
        ),
    )
}

internal fun apiKeyEditorValidationIssue(
    expireAt: String,
    maxCost: String,
): ApiKeyEditorValidationIssue? =
    (parseApiKeyEditorValues(expireAt = expireAt, maxCost = maxCost).exceptionOrNull() as? ApiKeyEditorValidationException)
        ?.issue

internal fun canSubmitApiKeyEditor(
    name: String,
    expireAt: String,
    maxCost: String,
    submitting: Boolean,
): Boolean =
    !submitting &&
        name.isNotBlank() &&
        apiKeyEditorValidationIssue(expireAt = expireAt, maxCost = maxCost) == null

private fun parseOptionalNonNegativeLong(value: String): Long? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return 0L
    return trimmed.toLongOrNull()?.takeIf { it >= 0L }
}

private fun parseOptionalNonNegativeFiniteDouble(value: String): Double? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return 0.0
    val number = trimmed.toDoubleOrNull() ?: return null
    return number.takeIf { it >= 0.0 && !it.isNaN() && !it.isInfinite() }
}

internal class ApiKeyEditorValidationException(
    val issue: ApiKeyEditorValidationIssue,
) : IllegalArgumentException(issue.name)
