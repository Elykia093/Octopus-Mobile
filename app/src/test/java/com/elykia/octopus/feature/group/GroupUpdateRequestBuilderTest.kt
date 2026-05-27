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
        assertThat(request.itemsToAdd).containsExactly(
            GroupItemAddRequest(channelId = 13, modelName = "deepseek", priority = 70, weight = 6),
            GroupItemAddRequest(channelId = 99, modelName = "claude-3-5", priority = 60, weight = 5),
        ).inOrder()
        assertThat(request.itemsToUpdate).containsExactly(
            GroupItemUpdateRequest(id = 1, priority = 200, weight = 4),
        )
        assertThat(request.itemsToDelete).containsExactly(3, 2).inOrder()
    }
}
