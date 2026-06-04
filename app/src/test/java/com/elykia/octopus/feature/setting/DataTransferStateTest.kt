package com.elykia.octopus.feature.setting

import com.elykia.octopus.core.common.AppResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DataTransferStateTest {
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
        val previous = listOf(com.elykia.octopus.core.data.model.ApiKeyItem(id = 1, name = "Main", apiKey = ""))

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
}
