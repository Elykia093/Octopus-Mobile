package com.elykia.octopus

import com.elykia.octopus.navigation.TopLevelDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OctopusAppTest {
    @Test
    fun adminModeShowsManagementDestinations() {
        val destinations = topLevelDestinationsFor(isApiKeyMode = false)

        assertEquals(
            listOf(
                TopLevelDestination.DASHBOARD,
                TopLevelDestination.CHANNEL,
                TopLevelDestination.GROUP,
                TopLevelDestination.API_KEY,
                TopLevelDestination.MODEL,
                TopLevelDestination.LOG,
                TopLevelDestination.SETTING,
            ),
            destinations,
        )
    }

    @Test
    fun apiKeyModeHidesAdminOnlyDestinations() {
        val destinations = topLevelDestinationsFor(isApiKeyMode = true)

        assertEquals(
            listOf(
                TopLevelDestination.DASHBOARD,
                TopLevelDestination.SETTING,
            ),
            destinations,
        )
        assertFalse(destinations.contains(TopLevelDestination.CHANNEL))
        assertFalse(destinations.contains(TopLevelDestination.GROUP))
        assertFalse(destinations.contains(TopLevelDestination.API_KEY))
        assertFalse(destinations.contains(TopLevelDestination.MODEL))
        assertFalse(destinations.contains(TopLevelDestination.LOG))
        assertTrue(destinations.contains(TopLevelDestination.DASHBOARD))
    }
}
