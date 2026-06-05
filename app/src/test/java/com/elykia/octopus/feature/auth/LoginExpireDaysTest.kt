package com.elykia.octopus.feature.auth

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginExpireDaysTest {
    @Test
    fun loginInputRejectsMissingUsernamePasswordAndServerUrl() {
        assertThat(loginInputError(LoginUiState(username = "", password = "secret"))).isEqualTo("请输入用户名。")
        assertThat(loginInputError(LoginUiState(username = "   ", password = "secret"))).isEqualTo("请输入用户名。")
        assertThat(loginInputError(LoginUiState(username = "admin", password = ""))).isEqualTo("请输入密码。")
        assertThat(
            loginInputError(
                LoginUiState(
                    showServerField = true,
                    serverUrl = " ",
                    username = "admin",
                    password = "secret",
                )
            )
        ).isEqualTo("请输入服务器地址。")
    }

    @Test
    fun loginInputRejectsInvalidExpireDays() {
        assertThat(loginInputError(LoginUiState(username = "admin", password = "secret", expireDays = "0")))
            .isEqualTo("请输入 1 到 3650 之间的令牌天数。")
    }

    @Test
    fun canSubmitLoginRequiresValidInputAndIdleState() {
        val validState = LoginUiState(username = " admin ", password = "secret", expireDays = "7")

        assertThat(canSubmitLogin(validState)).isTrue()
        assertThat(canSubmitLogin(validState.copy(isLoading = true))).isFalse()
        assertThat(canSubmitLogin(validState.copy(password = ""))).isFalse()
    }

    @Test
    fun loginInlineErrorAppearsOnlyAfterInputStarts() {
        assertThat(loginInlineError(LoginUiState())).isNull()
        assertThat(loginInlineError(LoginUiState(username = "", password = "secret"))).isEqualTo("请输入用户名。")
        assertThat(loginInlineError(LoginUiState(username = "operator"))).isEqualTo("请输入密码。")
        assertThat(loginInlineError(LoginUiState(username = "admin", password = "secret", expireDays = "0")))
            .isEqualTo("请输入 1 到 3650 之间的令牌天数。")
    }

    @Test
    fun parseLoginExpireDaysAcceptsTrimmedPositiveDays() {
        assertThat(parseLoginExpireDays(" 7 ")).isEqualTo(7)
        assertThat(parseLoginExpireDays("3650")).isEqualTo(3650)
    }

    @Test
    fun parseLoginExpireDaysRejectsInvalidOrUnsafeValues() {
        assertThat(parseLoginExpireDays("")).isNull()
        assertThat(parseLoginExpireDays("abc")).isNull()
        assertThat(parseLoginExpireDays("0")).isNull()
        assertThat(parseLoginExpireDays("-1")).isNull()
        assertThat(parseLoginExpireDays("3651")).isNull()
    }
}
