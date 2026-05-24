package com.elykia.octopus.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class LoginViewModelTest {
    @Test
    fun buildAdminLoginRequestPreservesPasswordSpacing() {
        val request = buildAdminLoginRequest(
            LoginUiState(
                username = "  admin  ",
                password = "  p@ss word  ",
                expireMinutes = "86400",
            ),
            expireMinutes = 86400,
        )

        assertEquals("admin", request.username)
        assertEquals("  p@ss word  ", request.password)
        assertEquals(86400, request.expire)
    }
}
