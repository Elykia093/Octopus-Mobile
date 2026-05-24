package com.elykia.octopus.core.data.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChannelModelsTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun channelListDecodesHureruNullSlicesAndNewFields() {
        val response = json.decodeFromString<ApiResponse<List<Channel>>>(
            """
            {
              "code": 200,
              "message": "success",
              "data": [
                {
                  "id": 7,
                  "name": "OpenAI",
                  "type": 1,
                  "enabled": true,
                  "base_urls": null,
                  "keys": null,
                  "model": "gpt-4o",
                  "custom_model": "",
                  "proxy_mode": "pool",
                  "proxy_config_id": 3,
                  "auto_sync": false,
                  "auto_group": 0,
                  "custom_header": null,
                  "ws_mode": "transform",
                  "param_override": null,
                  "match_regex": null,
                  "managed": true,
                  "managed_source": {
                    "site_id": 1,
                    "site_account_id": 2,
                    "site_user_group_id": null,
                    "group_key": "default"
                  },
                  "stats": {
                    "channel_id": 7,
                    "input_token": 10,
                    "output_token": 20,
                    "input_cost": 0.01,
                    "output_cost": 0.02,
                    "wait_time": 30,
                    "request_success": 4,
                    "request_failed": 1
                  }
                }
              ]
            }
            """.trimIndent()
        )

        val channel = response.data!!.single()
        assertTrue(response.isSuccessful)
        assertEquals(emptyList<BaseUrl>(), channel.baseUrls)
        assertEquals(emptyList<ChannelKey>(), channel.keys)
        assertEquals(emptyList<CustomHeader>(), channel.customHeader)
        assertEquals("pool", channel.proxyMode)
        assertEquals(3, channel.proxyConfigId)
        assertEquals("transform", channel.wsMode)
        assertTrue(channel.managed)
        assertEquals("default", channel.managedSource?.groupKey)
        assertNull(channel.managedSource?.siteUserGroupId)
        assertTrue(channel.stats != null)
    }
}
