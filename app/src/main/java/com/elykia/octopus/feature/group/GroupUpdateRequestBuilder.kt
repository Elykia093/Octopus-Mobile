package com.elykia.octopus.feature.group

import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.data.model.GroupItemAddRequest
import com.elykia.octopus.core.data.model.GroupItemUpdateRequest
import com.elykia.octopus.core.data.model.GroupUpdateRequest

internal fun buildGroupUpdateRequest(
    group: Group,
    name: String,
    mode: Int,
    matchRegex: String,
    firstTokenTimeOut: Int,
    sessionKeepTime: Int,
    items: List<GroupItem>,
): GroupUpdateRequest {
    val existingItemsById = group.items.associateBy { it.id }
    val newItems = items.filter { it.id == 0 }
    val changedItems = items.filter { it.id != 0 }
    val deleteItemIds = group.items.map { it.id }.filter { existingId ->
        items.none { current -> current.id == existingId }
    }.toMutableList()

    val itemsToAdd = mutableListOf<GroupItemAddRequest>()
    val itemsToUpdate = mutableListOf<GroupItemUpdateRequest>()

    changedItems.forEach { item ->
        val existing = existingItemsById[item.id] ?: return@forEach
        if (existing.channelId != item.channelId || existing.modelName != item.modelName) {
            deleteItemIds += existing.id
            itemsToAdd += item.toAddRequest()
        } else {
            itemsToUpdate += GroupItemUpdateRequest(
                id = item.id,
                priority = item.priority,
                weight = item.weight,
            )
        }
    }

    return GroupUpdateRequest(
        id = group.id,
        name = name.trim(),
        mode = mode,
        matchRegex = matchRegex.trim(),
        firstTokenTimeOut = firstTokenTimeOut,
        sessionKeepTime = sessionKeepTime,
        itemsToAdd = newItems.map { it.toAddRequest() } + itemsToAdd,
        itemsToUpdate = itemsToUpdate,
        itemsToDelete = deleteItemIds.distinct(),
    )
}

private fun GroupItem.toAddRequest(): GroupItemAddRequest = GroupItemAddRequest(
    channelId = channelId,
    modelName = modelName,
    priority = priority,
    weight = weight,
)
