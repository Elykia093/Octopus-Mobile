package com.elykia.octopus.core.data.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupModelsTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun groupListDecodesPinnedAndActivePresetFields() {
        val response = json.decodeFromString<ApiResponse<List<Group>>>(
            """
            {
              "code": 200,
              "message": "success",
              "data": [
                {
                  "id": 3,
                  "name": "gpt-4o",
                  "mode": 3,
                  "match_regex": "",
                  "first_token_time_out": 45,
                  "session_keep_time": 60,
                  "retry_enabled": true,
                  "max_retries": 2,
                  "pinned": true,
                  "pinned_at": "2026-05-25T11:39:59Z",
                  "active_preset_id": 9,
                  "items": [
                    {
                      "id": 10,
                      "group_id": 3,
                      "channel_id": 7,
                      "model_name": "gpt-4o",
                      "priority": 1,
                      "weight": 5
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        )

        val group = response.data!!.single()
        assertTrue(response.isSuccessful)
        assertTrue(group.pinned)
        assertEquals(9, group.activePresetId)
        assertEquals(45, group.firstTokenTimeOut)
        assertEquals(60, group.sessionKeepTime)
        assertEquals(2, group.maxRetries)
        assertEquals("gpt-4o", group.items.single().modelName)
    }

    @Test
    fun groupListTreatsNullItemsAsEmpty() {
        val response = json.decodeFromString<ApiResponse<List<Group>>>(
            """
            {
              "code": 200,
              "message": "success",
              "data": [
                {
                  "id": 4,
                  "name": "empty",
                  "mode": 1,
                  "items": null,
                  "pinned_at": null,
                  "active_preset_id": null
                }
              ]
            }
            """.trimIndent()
        )

        val group = response.data!!.single()
        assertEquals(emptyList<GroupItem>(), group.items)
        assertNull(group.pinnedAt)
        assertNull(group.activePresetId)
    }
}
