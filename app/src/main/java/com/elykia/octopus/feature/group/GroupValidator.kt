package com.elykia.octopus.feature.group

/**
 * Group 编辑器验证问题类型
 */
enum class GroupEditorValidationIssue {
    InvalidFirstTokenTimeout,
    InvalidSessionKeepTime,
    InvalidMaxRetries,
}

/**
 * Group 编辑器验证后的值
 */
data class GroupEditorValues(
    val firstTokenTimeOut: Int,
    val sessionKeepTime: Int,
    val maxRetries: Int,
)

/**
 * Group 编辑器验证异常
 */
class GroupEditorValidationException(
    val issue: GroupEditorValidationIssue,
) : IllegalArgumentException(issue.name)

/**
 * 解析并验证 Group 编辑器输入值
 *
 * @return Success(GroupEditorValues) 如果所有值有效
 * @return Failure(GroupEditorValidationException) 如果验证失败
 */
fun parseGroupEditorValues(
    firstTokenTimeOut: String,
    sessionKeepTime: String,
    retryEnabled: Boolean,
    maxRetries: String,
): Result<GroupEditorValues> {
    val parsedTimeout = parseOptionalNonNegativeInt(firstTokenTimeOut)
        ?: return Result.failure(GroupEditorValidationException(GroupEditorValidationIssue.InvalidFirstTokenTimeout))

    val parsedKeepTime = parseOptionalNonNegativeInt(sessionKeepTime)
        ?: return Result.failure(GroupEditorValidationException(GroupEditorValidationIssue.InvalidSessionKeepTime))

    val parsedMaxRetries = if (retryEnabled) {
        parseOptionalPositiveInt(maxRetries, defaultValue = 3)
            ?: return Result.failure(GroupEditorValidationException(GroupEditorValidationIssue.InvalidMaxRetries))
    } else {
        3
    }

    return Result.success(
        GroupEditorValues(
            firstTokenTimeOut = parsedTimeout,
            sessionKeepTime = parsedKeepTime,
            maxRetries = parsedMaxRetries,
        ),
    )
}

/**
 * 获取验证问题类型（如果有）
 */
fun groupEditorValidationIssue(
    firstTokenTimeOut: String,
    sessionKeepTime: String,
    retryEnabled: Boolean,
    maxRetries: String,
): GroupEditorValidationIssue? =
    (parseGroupEditorValues(
        firstTokenTimeOut = firstTokenTimeOut,
        sessionKeepTime = sessionKeepTime,
        retryEnabled = retryEnabled,
        maxRetries = maxRetries,
    ).exceptionOrNull() as? GroupEditorValidationException)
        ?.issue

/**
 * 检查是否可以提交 Group 编辑器
 */
fun canSubmitGroupEditor(
    name: String,
    firstTokenTimeOut: String,
    sessionKeepTime: String,
    retryEnabled: Boolean,
    maxRetries: String,
    hasValidItems: Boolean,
    submitting: Boolean,
): Boolean =
    !submitting &&
        name.isNotBlank() &&
        hasValidItems &&
        groupEditorValidationIssue(
            firstTokenTimeOut = firstTokenTimeOut,
            sessionKeepTime = sessionKeepTime,
            retryEnabled = retryEnabled,
            maxRetries = maxRetries,
        ) == null

/**
 * 解析可选的非负整数，空白视为 0
 */
private fun parseOptionalNonNegativeInt(value: String): Int? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return 0
    return trimmed.toIntOrNull()?.takeIf { it >= 0 }
}

/**
 * 解析可选的正整数，空白使用默认值
 */
private fun parseOptionalPositiveInt(value: String, defaultValue: Int): Int? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return defaultValue
    return trimmed.toIntOrNull()?.takeIf { it > 0 }
}
