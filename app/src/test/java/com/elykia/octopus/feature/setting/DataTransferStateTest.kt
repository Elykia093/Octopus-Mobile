package com.elykia.octopus.feature.setting

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.LatestInfo
import com.elykia.octopus.core.data.model.SettingItem
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DataTransferStateTest {
    @Test
    fun settingsPageErrorOnlyShowsWhenRefreshFailsWithoutCachedSections() {
        assertThat(SettingUiState(error = "settings failed").shouldShowSettingsPageError()).isTrue()

        assertThat(
            SettingUiState(
                sections = listOf(
                    SettingSection(
                        key = "system",
                        title = "System",
                        summary = "Cached settings.",
                        items = listOf(SettingItem("proxy_url", "")),
                    )
                ),
                error = "settings failed",
            ).shouldShowSettingsPageError()
        ).isFalse()
    }

    @Test
    fun dataTransferStartClearsPreviousMessageAndError() {
        val state = SettingUiState(
            dataTransferMessage = "exported",
            dataTransferError = "old error",
        ).dataTransferStarted()

        assertThat(state.dataTransferSubmitting).isTrue()
        assertThat(state.dataTransferMessage).isNull()
        assertThat(state.dataTransferError).isNull()
    }

    @Test
    fun dataTransferSuccessAndFailureStopSubmitting() {
        val succeeded = SettingUiState(dataTransferSubmitting = true)
            .dataTransferSucceeded("done")

        assertThat(succeeded.dataTransferSubmitting).isFalse()
        assertThat(succeeded.dataTransferMessage).isEqualTo("done")
        assertThat(succeeded.dataTransferError).isNull()

        val failed = SettingUiState(dataTransferSubmitting = true)
            .dataTransferFailed("failed")

        assertThat(failed.dataTransferSubmitting).isFalse()
        assertThat(failed.dataTransferMessage).isNull()
        assertThat(failed.dataTransferError).isEqualTo("failed")
    }

    @Test
    fun actionStartSuccessAndFailureTrackIndependentStatus() {
        val started = SettingUiState(
            actionMessage = "old message",
            actionError = "old error",
            dataTransferMessage = "exported",
        ).actionStarted()

        assertThat(started.actionSubmitting).isTrue()
        assertThat(started.actionMessage).isNull()
        assertThat(started.actionError).isNull()
        assertThat(started.dataTransferMessage).isEqualTo("exported")

        val succeeded = started.actionSucceeded("done")

        assertThat(succeeded.actionSubmitting).isFalse()
        assertThat(succeeded.actionMessage).isEqualTo("done")
        assertThat(succeeded.actionError).isNull()

        val failed = started.actionFailed("failed")

        assertThat(failed.actionSubmitting).isFalse()
        assertThat(failed.actionMessage).isNull()
        assertThat(failed.actionError).isEqualTo("failed")
    }

    @Test
    fun partialRefreshErrorKeepsPreviousDataAndExposesError() {
        val previous = listOf(SettingItem("relay_log_keep_enabled", "true"))

        val result = AppResult.Error("api failed").dataOrPrevious(previous)

        assertThat(result).isEqualTo(previous)
        assertThat(AppResult.Error("api failed").errorMessageOrNull()).isEqualTo("api failed")
        assertThat(AppResult.Success(previous).errorMessageOrNull()).isNull()
    }

    @Test
    fun partialRefreshErrorKeepsPreviousNullableData() {
        val result = AppResult.Error("version failed").dataOrPreviousNullable("0.1.0")

        assertThat(result).isEqualTo("0.1.0")
    }

    @Test
    fun settingRefreshSuccessClearsRecoveredPartialErrors() {
        val state = buildSettingRefreshState(
            previous = SettingUiState(
                versionInfoError = "old version error",
                modelLastUpdateError = "old model error",
            ),
            settingsResult = AppResult.Success(listOf(SettingItem("relay_log_keep_enabled", "true"))),
            latestResult = AppResult.Success(LatestInfo(tagName = "v1.0.0", publishedAt = "2026-06-01")),
            versionResult = AppResult.Success("v0.9.0"),
            modelTimeResult = AppResult.Success("2026-06-01"),
            channelTimeResult = AppResult.Success("2026-06-02"),
            username = "admin",
            language = "zh-CN",
            themeMode = 1,
        )

        assertThat(state.loading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.settings).containsExactly(SettingItem("relay_log_keep_enabled", "true"))
        assertThat(state.latestInfo?.tagName).isEqualTo("v1.0.0")
        assertThat(state.currentVersion).isEqualTo("v0.9.0")
        assertThat(state.modelLastUpdateTime).isEqualTo("2026-06-01")
        assertThat(state.channelLastSyncTime).isEqualTo("2026-06-02")
        assertThat(state.versionInfoError).isNull()
        assertThat(state.modelLastUpdateError).isNull()
        assertThat(state.channelLastSyncError).isNull()
        assertThat(state.username).isEqualTo("admin")
        assertThat(state.language).isEqualTo("zh-CN")
        assertThat(state.themeMode).isEqualTo(1)
    }

    @Test
    fun settingRefreshFailureKeepsPreviousPageDataAndUpdatesPartialResults() {
        val previous = SettingUiState(
            settings = listOf(SettingItem("relay_log_keep_enabled", "false")),
            sections = listOf(
                SettingSection(
                    key = "log",
                    title = "Log settings",
                    summary = "Log retention settings.",
                    items = listOf(SettingItem("relay_log_keep_enabled", "false")),
                )
            ),
            latestInfo = LatestInfo(tagName = "old", publishedAt = "2026-05-01"),
            currentVersion = "old-current",
            modelLastUpdateTime = "old-time",
            channelLastSyncTime = "old-sync",
            versionInfoError = "old version error",
            modelLastUpdateError = "old model error",
            channelLastSyncError = "old channel error",
        )

        val state = buildSettingRefreshState(
            previous = previous,
            settingsResult = AppResult.Error("settings failed"),
            latestResult = AppResult.Success(LatestInfo(tagName = "new", publishedAt = "2026-06-01")),
            versionResult = AppResult.Error("version failed"),
            modelTimeResult = AppResult.Success("new-time"),
            channelTimeResult = AppResult.Error("sync time failed"),
            username = "operator",
            language = "en",
            themeMode = 2,
        )

        assertThat(state.loading).isFalse()
        assertThat(state.error).isEqualTo("settings failed")
        assertThat(state.settings).isEqualTo(previous.settings)
        assertThat(state.sections).isEqualTo(previous.sections)
        assertThat(state.latestInfo?.tagName).isEqualTo("new")
        assertThat(state.currentVersion).isEqualTo("old-current")
        assertThat(state.modelLastUpdateTime).isEqualTo("new-time")
        assertThat(state.channelLastSyncTime).isEqualTo("old-sync")
        assertThat(state.versionInfoError).isEqualTo("version failed")
        assertThat(state.modelLastUpdateError).isNull()
        assertThat(state.channelLastSyncError).isEqualTo("sync time failed")
        assertThat(state.username).isEqualTo("operator")
        assertThat(state.language).isEqualTo("en")
        assertThat(state.themeMode).isEqualTo(2)
    }

    @Test
    fun settingValidationAcceptsConfiguredBoundaries() {
        assertThat(validateSettingValue("stats_save_interval", "1")).isNull()
        assertThat(validateSettingValue("stats_save_interval", "1440")).isNull()
        assertThat(validateSettingValue("model_info_update_interval", "8760")).isNull()
        assertThat(validateSettingValue("sync_llm_interval", "8760")).isNull()
        assertThat(validateSettingValue("relay_log_keep_period", "3650")).isNull()
        assertThat(validateSettingValue("circuit_breaker_threshold", "1000")).isNull()
        assertThat(validateSettingValue("circuit_breaker_cooldown", "86400")).isNull()
        assertThat(validateSettingValue("circuit_breaker_max_cooldown", "604800")).isNull()
    }

    @Test
    fun settingValidationRejectsInvalidNumbers() {
        assertThat(validateSettingValue("stats_save_interval", "0"))
            .isEqualTo(SettingValidationIssue.InvalidNumber)
        assertThat(validateSettingValue("stats_save_interval", "1441"))
            .isEqualTo(SettingValidationIssue.InvalidNumber)
        assertThat(validateSettingValue("stats_save_interval", "-1"))
            .isEqualTo(SettingValidationIssue.InvalidNumber)
        assertThat(validateSettingValue("stats_save_interval", "1.5"))
            .isEqualTo(SettingValidationIssue.InvalidNumber)
        assertThat(validateSettingValue("stats_save_interval", "NaN"))
            .isEqualTo(SettingValidationIssue.InvalidNumber)
        assertThat(validateSettingValue("stats_save_interval", "Infinity"))
            .isEqualTo(SettingValidationIssue.InvalidNumber)
    }

    @Test
    fun settingValidationAcceptsSafeProxyUrlAndCorsOrigins() {
        assertThat(validateSettingValue("proxy_url", "")).isNull()
        assertThat(validateSettingValue("proxy_url", "https://proxy.example.com:8443/path")).isNull()
        assertThat(validateSettingValue("proxy_url", "http://127.0.0.1:8080")).isNull()
        assertThat(validateSettingValue("cors_allow_origins", "")).isNull()
        assertThat(validateSettingValue("cors_allow_origins", "*")).isNull()
        assertThat(validateSettingValue("cors_allow_origins", "https://app.example.com, http://localhost:3000")).isNull()
    }

    @Test
    fun settingValidationRejectsUnsafeProxyUrlAndCorsOrigins() {
        assertThat(validateSettingValue("proxy_url", "ftp://proxy.example.com"))
            .isEqualTo(SettingValidationIssue.InvalidUrl)
        assertThat(validateSettingValue("proxy_url", "https://user:pass@proxy.example.com"))
            .isEqualTo(SettingValidationIssue.InvalidUrl)
        assertThat(validateSettingValue("proxy_url", "not a url"))
            .isEqualTo(SettingValidationIssue.InvalidUrl)

        assertThat(validateSettingValue("cors_allow_origins", "https://app.example.com/path"))
            .isEqualTo(SettingValidationIssue.InvalidCors)
        assertThat(validateSettingValue("cors_allow_origins", "https://app.example.com?debug=true"))
            .isEqualTo(SettingValidationIssue.InvalidCors)
        assertThat(validateSettingValue("cors_allow_origins", "https://user:pass@app.example.com"))
            .isEqualTo(SettingValidationIssue.InvalidCors)
        assertThat(validateSettingValue("cors_allow_origins", "ftp://app.example.com"))
            .isEqualTo(SettingValidationIssue.InvalidCors)
    }

    @Test
    fun canSubmitSettingEditFollowsValidationRules() {
        assertThat(canSubmitSettingEdit("stats_save_interval", "30")).isTrue()
        assertThat(canSubmitSettingEdit("stats_save_interval", "0")).isFalse()
        assertThat(canSubmitSettingEdit("proxy_url", "https://proxy.example.com")).isTrue()
        assertThat(canSubmitSettingEdit("proxy_url", "https://user:pass@proxy.example.com")).isFalse()
        assertThat(canSubmitSettingEdit("cors_allow_origins", "https://app.example.com")).isTrue()
        assertThat(canSubmitSettingEdit("cors_allow_origins", "https://app.example.com/path")).isFalse()
    }

    @Test
    fun accountValidationRequiresUsernameAndMatchingPassword() {
        assertThat(validateUsernameChange("")).isEqualTo(AccountValidationIssue.UsernameBlank)
        assertThat(validateUsernameChange(" admin ")).isNull()

        assertThat(validatePasswordChange("", "newpass", "newpass"))
            .isEqualTo(AccountValidationIssue.OldPasswordBlank)
        assertThat(validatePasswordChange("old", "", ""))
            .isEqualTo(AccountValidationIssue.NewPasswordBlank)
        assertThat(validatePasswordChange("old", "newpass", "different"))
            .isEqualTo(AccountValidationIssue.PasswordMismatch)
        assertThat(validatePasswordChange("old", "12345", "12345"))
            .isEqualTo(AccountValidationIssue.PasswordTooShort)
        assertThat(validatePasswordChange("old", "123456", "123456")).isNull()

        assertThat(canSubmitUsernameChange("admin", submitting = false)).isTrue()
        assertThat(canSubmitUsernameChange("", submitting = false)).isFalse()
        assertThat(canSubmitPasswordChange("old", "123456", "123456", submitting = false)).isTrue()
        assertThat(canSubmitPasswordChange("old", "123456", "123456", submitting = true)).isFalse()
    }
}
