package com.elykia.octopus.feature.group

/**
 * Group Item 数字输入解析结果
 */
data class GroupItemNumberInput(
    val displayValue: String,
    val value: Int,
)

/**
 * 解析 Group Item 的非负整数输入
 * 空白返回 GroupItemNumberInput("", 0)
 * 无效输入返回 null
 */
fun parseGroupItemNumberInput(value: String): GroupItemNumberInput? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return GroupItemNumberInput(displayValue = "", value = 0)
    val parsed = trimmed.toIntOrNull()?.takeIf { it >= 0 } ?: return null
    return GroupItemNumberInput(displayValue = trimmed, value = parsed)
}

/**
 * 解析 Group Item 的非负整数（废弃，使用 parseGroupItemNumberInput）
 */
@Deprecated("Use parseGroupItemNumberInput instead", ReplaceWith("parseGroupItemNumberInput(value)?.value"))
fun parseGroupItemNonNegativeInt(value: String): Int? =
    value.trim().toIntOrNull()?.takeIf { it >= 0 }

/**
 * 计算下一个 Group Item 的优先级
 */
fun nextGroupItemPriority(items: List<com.elykia.octopus.core.data.model.GroupItem>): Int {
    return (items.maxOfOrNull { it.priority } ?: -1) + 1
}
