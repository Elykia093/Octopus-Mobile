package com.elykia.octopus.feature.group

import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.data.model.GroupItemAddRequest
import com.elykia.octopus.core.data.model.GroupItemUpdateRequest
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GroupUpdateRequestBuilderTest {
    @Test
    fun buildGroupUpdateRequestSeparatesNewChangedUpdatedAndDeletedItems() {
        val group = Group(
            id = 7,
            name = "old",
            mode = 1,
            items = listOf(
                GroupItem(id = 1, groupId = 7, channelId = 10, modelName = "gpt-4o", priority = 100, weight = 1),
                GroupItem(id = 2, groupId = 7, channelId = 11, modelName = "claude", priority = 50, weight = 2),
                GroupItem(id = 3, groupId = 7, channelId = 12, modelName = "gemini", priority = 20, weight = 3),
            ),
        )
        val request = buildGroupUpdateRequest(
            group = group,
            name = " new name ",
            mode = 4,
            matchRegex = " .* ",
            firstTokenTimeOut = 30,
            sessionKeepTime = 120,
            retryEnabled = true,
            maxRetries = 5,
            items = listOf(
                GroupItem(id = 1, groupId = 7, channelId = 10, modelName = "gpt-4o", priority = 200, weight = 4),
                GroupItem(id = 2, groupId = 7, channelId = 99, modelName = "claude-3-5", priority = 60, weight = 5),
                GroupItem(channelId = 13, modelName = "deepseek", priority = 70, weight = 6),
            ),
        )

        assertThat(request.id).isEqualTo(7)
        assertThat(request.name).isEqualTo("new name")
        assertThat(request.mode).isEqualTo(4)
        assertThat(request.matchRegex).isEqualTo(".*")
        assertThat(request.firstTokenTimeOut).isEqualTo(30)
        assertThat(request.sessionKeepTime).isEqualTo(120)
        assertThat(request.retryEnabled).isTrue()
        assertThat(request.maxRetries).isEqualTo(5)
        assertThat(request.itemsToAdd).containsExactly(
            GroupItemAddRequest(channelId = 13, modelName = "deepseek", priority = 70, weight = 6),
            GroupItemAddRequest(channelId = 99, modelName = "claude-3-5", priority = 60, weight = 5),
        ).inOrder()
        assertThat(request.itemsToUpdate).containsExactly(
            GroupItemUpdateRequest(id = 1, priority = 200, weight = 4),
        )
        assertThat(request.itemsToDelete).containsExactly(3, 2).inOrder()
    }

    @Test
    fun buildGroupUpdateRequestAllowsClearingAllItems() {
        val group = Group(
            id = 8,
            name = "old",
            mode = 1,
            items = listOf(
                GroupItem(id = 4, groupId = 8, channelId = 10, modelName = "gpt-4o", priority = 1, weight = 1),
                GroupItem(id = 5, groupId = 8, channelId = 11, modelName = "claude", priority = 2, weight = 1),
            ),
        )

        val request = buildGroupUpdateRequest(
            group = group,
            name = "empty",
            mode = 2,
            matchRegex = "",
            firstTokenTimeOut = 0,
            sessionKeepTime = 0,
            retryEnabled = false,
            maxRetries = 3,
            items = emptyList(),
        )

        assertThat(request.itemsToAdd).isEmpty()
        assertThat(request.itemsToUpdate).isEmpty()
        assertThat(request.itemsToDelete).containsExactly(4, 5).inOrder()
        assertThat(request.name).isEqualTo("empty")
        assertThat(request.retryEnabled).isFalse()
        assertThat(request.maxRetries).isEqualTo(3)
    }

    @Test
    fun buildGroupUpdateRequestDefaultsInvalidMaxRetries() {
        val request = buildGroupUpdateRequest(
            group = Group(id = 9, name = "old", mode = 1),
            name = "old",
            mode = 1,
            matchRegex = "",
            firstTokenTimeOut = 0,
            sessionKeepTime = 0,
            retryEnabled = true,
            maxRetries = 0,
            items = emptyList(),
        )

        assertThat(request.maxRetries).isEqualTo(3)
    }

    @Test
    fun parseGroupEditorValuesTreatsBlankOptionalFieldsAsDefaults() {
        val result = parseGroupEditorValues(
            firstTokenTimeOut = "",
            sessionKeepTime = " ",
            retryEnabled = true,
            maxRetries = "",
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().firstTokenTimeOut).isEqualTo(0)
        assertThat(result.getOrThrow().sessionKeepTime).isEqualTo(0)
        assertThat(result.getOrThrow().maxRetries).isEqualTo(3)
    }

    @Test
    fun parseGroupEditorValuesAcceptsNonNegativeTimeoutsAndPositiveRetries() {
        val result = parseGroupEditorValues(
            firstTokenTimeOut = "30",
            sessionKeepTime = "120",
            retryEnabled = true,
            maxRetries = "5",
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().firstTokenTimeOut).isEqualTo(30)
        assertThat(result.getOrThrow().sessionKeepTime).isEqualTo(120)
        assertThat(result.getOrThrow().maxRetries).isEqualTo(5)
    }

    @Test
    fun parseGroupEditorValuesRejectsInvalidTimeouts() {
        val negative = parseGroupEditorValues("-1", "0", true, "3")
        val text = parseGroupEditorValues("soon", "0", true, "3")

        assertThat((negative.exceptionOrNull() as GroupEditorValidationException).issue)
            .isEqualTo(GroupEditorValidationIssue.InvalidFirstTokenTimeout)
        assertThat((text.exceptionOrNull() as GroupEditorValidationException).issue)
            .isEqualTo(GroupEditorValidationIssue.InvalidFirstTokenTimeout)
    }

    @Test
    fun parseGroupEditorValuesRejectsInvalidKeepTime() {
        val negative = parseGroupEditorValues("0", "-1", true, "3")
        val text = parseGroupEditorValues("0", "later", true, "3")

        assertThat((negative.exceptionOrNull() as GroupEditorValidationException).issue)
            .isEqualTo(GroupEditorValidationIssue.InvalidSessionKeepTime)
        assertThat((text.exceptionOrNull() as GroupEditorValidationException).issue)
            .isEqualTo(GroupEditorValidationIssue.InvalidSessionKeepTime)
    }

    @Test
    fun parseGroupEditorValuesRejectsInvalidMaxRetriesOnlyWhenRetryIsEnabled() {
        val zero = parseGroupEditorValues("0", "0", true, "0")
        val text = parseGroupEditorValues("0", "0", true, "many")
        val disabled = parseGroupEditorValues("0", "0", false, "many")

        assertThat((zero.exceptionOrNull() as GroupEditorValidationException).issue)
            .isEqualTo(GroupEditorValidationIssue.InvalidMaxRetries)
        assertThat((text.exceptionOrNull() as GroupEditorValidationException).issue)
            .isEqualTo(GroupEditorValidationIssue.InvalidMaxRetries)
        assertThat(disabled.isSuccess).isTrue()
        assertThat(disabled.getOrThrow().maxRetries).isEqualTo(3)
    }

    @Test
    fun canSubmitGroupEditorRequiresValidInputItemsAndIdleState() {
        assertThat(
            canSubmitGroupEditor(
                name = "default",
                firstTokenTimeOut = "30",
                sessionKeepTime = "120",
                retryEnabled = true,
                maxRetries = "3",
                hasValidItems = true,
                submitting = false,
            )
        ).isTrue()

        assertThat(
            canSubmitGroupEditor(
                name = " ",
                firstTokenTimeOut = "30",
                sessionKeepTime = "120",
                retryEnabled = true,
                maxRetries = "3",
                hasValidItems = true,
                submitting = false,
            )
        ).isFalse()
        assertThat(
            canSubmitGroupEditor(
                name = "default",
                firstTokenTimeOut = "-1",
                sessionKeepTime = "120",
                retryEnabled = true,
                maxRetries = "3",
                hasValidItems = true,
                submitting = false,
            )
        ).isFalse()
        assertThat(
            canSubmitGroupEditor(
                name = "default",
                firstTokenTimeOut = "30",
                sessionKeepTime = "-1",
                retryEnabled = true,
                maxRetries = "3",
                hasValidItems = true,
                submitting = false,
            )
        ).isFalse()
        assertThat(
            canSubmitGroupEditor(
                name = "default",
                firstTokenTimeOut = "30",
                sessionKeepTime = "120",
                retryEnabled = true,
                maxRetries = "0",
                hasValidItems = true,
                submitting = false,
            )
        ).isFalse()
        assertThat(
            canSubmitGroupEditor(
                name = "default",
                firstTokenTimeOut = "30",
                sessionKeepTime = "120",
                retryEnabled = false,
                maxRetries = "0",
                hasValidItems = true,
                submitting = false,
            )
        ).isTrue()
        assertThat(
            canSubmitGroupEditor(
                name = "default",
                firstTokenTimeOut = "30",
                sessionKeepTime = "120",
                retryEnabled = true,
                maxRetries = "3",
                hasValidItems = false,
                submitting = false,
            )
        ).isFalse()
        assertThat(
            canSubmitGroupEditor(
                name = "default",
                firstTokenTimeOut = "30",
                sessionKeepTime = "120",
                retryEnabled = true,
                maxRetries = "3",
                hasValidItems = true,
                submitting = true,
            )
        ).isFalse()
    }

    @Test
    fun parseGroupItemNonNegativeIntRejectsInvalidValues() {
        assertThat(parseGroupItemNonNegativeInt("0")).isEqualTo(0)
        assertThat(parseGroupItemNonNegativeInt("42")).isEqualTo(42)
        assertThat(parseGroupItemNonNegativeInt("-1")).isNull()
        assertThat(parseGroupItemNonNegativeInt("1.5")).isNull()
        assertThat(parseGroupItemNonNegativeInt("abc")).isNull()
        assertThat(parseGroupItemNonNegativeInt("")).isNull()
    }

    @Test
    fun parseGroupItemNumberInputAllowsBlankDisplayWithZeroValue() {
        val blank = parseGroupItemNumberInput("")
        val spaces = parseGroupItemNumberInput("   ")

        assertThat(blank).isEqualTo(GroupItemNumberInput(displayValue = "", value = 0))
        assertThat(spaces).isEqualTo(GroupItemNumberInput(displayValue = "", value = 0))
    }

    @Test
    fun parseGroupItemNumberInputKeepsTypedDisplayForValidNumbers() {
        val parsed = parseGroupItemNumberInput(" 007 ")

        assertThat(parsed).isEqualTo(GroupItemNumberInput(displayValue = "007", value = 7))
    }

    @Test
    fun parseGroupItemNumberInputRejectsInvalidNumbers() {
        assertThat(parseGroupItemNumberInput("-1")).isNull()
        assertThat(parseGroupItemNumberInput("1.5")).isNull()
        assertThat(parseGroupItemNumberInput("abc")).isNull()
    }
}
