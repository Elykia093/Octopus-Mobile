package com.elykia.octopus.feature.apikey

import com.elykia.octopus.core.data.model.Group
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiKeyModelRestrictionsTest {
    @Test
    fun parseRestrictionsTrimsDropsBlankAndKeepsFirstOccurrence() {
        assertThat(parseApiKeyModelRestrictions(" gpt-4o, ,claude, gpt-4o "))
            .containsExactly("gpt-4o", "claude")
            .inOrder()
    }

    @Test
    fun toggleRestrictionAddsAndRemovesModel() {
        assertThat(toggleApiKeyModelRestriction("gpt-4o", "claude")).isEqualTo("gpt-4o,claude")
        assertThat(toggleApiKeyModelRestriction("gpt-4o,claude", "gpt-4o")).isEqualTo("claude")
    }

    @Test
    fun candidatesUseSortedDistinctGroupNames() {
        val groups = listOf(
            Group(id = 1, name = "vip", mode = 1),
            Group(id = 2, name = " default ", mode = 1),
            Group(id = 3, name = "vip", mode = 1),
            Group(id = 4, name = "", mode = 1),
        )

        assertThat(apiKeyModelRestrictionCandidates(groups))
            .containsExactly("default", "vip")
            .inOrder()
    }
}
