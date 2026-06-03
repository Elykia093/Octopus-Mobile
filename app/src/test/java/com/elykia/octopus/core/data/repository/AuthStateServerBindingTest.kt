package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.ServerConfig
import com.elykia.octopus.core.common.AppResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AuthStateServerBindingTest {
    @Test
    fun authStateIsBoundOnlyWhenTokenAndServerMatch() {
        val auth = AuthState(token = "token", serverUrl = "https://example.com")

        assertThat(auth.isBoundToServer("https://example.com")).isTrue()
        assertThat(auth.isBoundToServer("https://other.example.com")).isFalse()
    }

    @Test
    fun authStateRejectsLegacySessionWithoutServerBinding() {
        val legacyAuth = AuthState(token = "token")

        assertThat(legacyAuth.isBoundToServer("https://example.com")).isFalse()
    }

    @Test
    fun normalizeServerIdentityTrimsWhitespaceAndTrailingSlash() {
        assertThat(normalizeServerIdentity(" https://example.com/octopus/ "))
            .isEqualTo("https://example.com/octopus")
    }

    @Test
    fun normalizeValidServerIdentityAcceptsHttpsPath() {
        assertThat(normalizeValidServerIdentity(" https://example.com/octopus/ "))
            .isEqualTo("https://example.com/octopus")
    }

    @Test
    fun normalizeValidServerIdentityRejectsUnsafeServerUrls() {
        assertThat(normalizeValidServerIdentity("http://example.com")).isNull()
        assertThat(normalizeValidServerIdentity("https://user:pass@example.com")).isNull()
        assertThat(normalizeValidServerIdentity("https://example.com?token=secret")).isNull()
        assertThat(normalizeValidServerIdentity("https://example.com/#admin")).isNull()
    }

    @Test
    fun serverConfigHttpsOnlyRejectsUnsafeStoredUrls() {
        assertThat(ServerConfig(baseUrl = "https://example.com/path").httpsOnly().baseUrl)
            .isEqualTo("https://example.com/path")
        assertThat(ServerConfig(baseUrl = "http://example.com").httpsOnly().baseUrl).isEmpty()
        assertThat(ServerConfig(baseUrl = "https://user:pass@example.com").httpsOnly().baseUrl).isEmpty()
        assertThat(ServerConfig(baseUrl = "https://example.com?token=secret").httpsOnly().baseUrl).isEmpty()
        assertThat(ServerConfig(baseUrl = "https://example.com/#admin").httpsOnly().baseUrl).isEmpty()
    }

    @Test
    fun persistLoginAuthDoesNotUpdateSessionWhenSecureSaveFails() {
        val auth = AuthState(token = "token", serverUrl = "https://example.com")
        var updatedAuth: AuthState? = null

        val result = persistLoginAuth(
            auth = auth,
            saveAuth = { false },
            updateSession = { updatedAuth = it },
        )

        assertThat(result).isEqualTo(AppResult.Error(SESSION_SAVE_FAILED_MESSAGE))
        assertThat(updatedAuth).isNull()
    }

    @Test
    fun persistLoginAuthUpdatesSessionOnlyAfterSecureSaveSucceeds() {
        val auth = AuthState(token = "token", serverUrl = "https://example.com")
        var updatedAuth: AuthState? = null

        val result = persistLoginAuth(
            auth = auth,
            saveAuth = { true },
            updateSession = { updatedAuth = it },
        )

        assertThat(result).isEqualTo(AppResult.Success(auth))
        assertThat(updatedAuth).isEqualTo(auth)
    }

    @Test
    fun clearSessionFailureMessageIsExplicit() {
        assertThat(SESSION_CLEAR_FAILED_MESSAGE).contains("安全清除会话失败")
    }

    @Test
    fun runServerConfigMutationReturnsSuccessResult() = runBlocking {
        val config = ServerConfig(baseUrl = "https://example.com")

        val result = runServerConfigMutation {
            AppResult.Success(config)
        }

        assertThat(result).isEqualTo(AppResult.Success(config))
    }

    @Test
    fun runServerConfigMutationMapsWriteFailureToAppError() = runBlocking {
        val result = runServerConfigMutation {
            error("disk full")
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        val error = result as AppResult.Error
        assertThat(error.message).isEqualTo(SERVER_CONFIG_SAVE_FAILED_MESSAGE)
        assertThat(error.throwable).isInstanceOf(IllegalStateException::class.java)
    }
}
