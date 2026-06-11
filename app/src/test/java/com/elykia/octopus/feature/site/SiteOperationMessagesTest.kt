package com.elykia.octopus.feature.site

import com.elykia.octopus.core.data.model.AllApiHubImportResult
import com.elykia.octopus.core.data.model.MetApiImportResult
import com.elykia.octopus.core.data.model.SiteBatchAction
import com.elykia.octopus.core.data.model.SiteBatchActionResult
import com.elykia.octopus.core.data.model.SiteBatchFailedItem
import com.elykia.octopus.core.data.model.SiteCheckinResult
import com.elykia.octopus.core.data.model.SiteSyncResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SiteOperationMessagesTest {
    @Test
    fun syncMessageIncludesStatusServerMessageAndCounts() {
        val message = SiteSyncResult(
            status = "partial",
            message = "部分分组同步失败",
            groupCount = 3,
            tokenCount = 5,
            modelCount = 42,
        ).toSiteSyncOperationMessage()

        assertThat(message).isEqualTo("同步部分成功：部分分组同步失败（分组 3，Key 5，模型 42）")
    }

    @Test
    fun checkinMessageIncludesRewardWhenPresent() {
        val message = SiteCheckinResult(
            status = "success",
            message = "签到完成",
            reward = "10 credits",
        ).toSiteCheckinOperationMessage()

        assertThat(message).isEqualTo("签到成功：签到完成，奖励：10 credits")
    }

    @Test
    fun failedStatusIsCaseInsensitive() {
        assertThat("failed".isFailedSiteOperationStatus()).isTrue()
        assertThat("Failed".isFailedSiteOperationStatus()).isTrue()
        assertThat("partial".isFailedSiteOperationStatus()).isFalse()
    }

    @Test
    fun importMessagesUseReadableEnglishSummary() {
        assertThat(
            AllApiHubImportResult(
                createdSites = 1,
                reusedSites = 2,
                createdAccounts = 3,
                updatedAccounts = 4,
                skippedAccounts = 5,
                scheduledSyncAccounts = 6,
            ).toAllApiHubImportOperationMessage()
        ).isEqualTo(
            "All API Hub import completed: created sites 1, reused sites 2, " +
                "created accounts 3, updated accounts 4, skipped accounts 5, scheduled sync 6."
        )

        assertThat(
            MetApiImportResult(
                createdSites = 1,
                reusedSites = 2,
                createdAccounts = 3,
                updatedAccounts = 4,
                skippedAccounts = 5,
                importedTokens = 6,
                importedGroups = 7,
                importedModels = 8,
            ).toMetApiImportOperationMessage()
        ).isEqualTo(
            "MetAPI import completed: created sites 1, reused sites 2, " +
                "created accounts 3, updated accounts 4, skipped accounts 5, imported tokens 6, groups 7, models 8."
        )
    }

    @Test
    fun siteBatchMessageSummarizesSuccessAndFailures() {
        assertThat(siteBatchActionLabel(SiteBatchAction.Enable)).isEqualTo("enable")

        assertThat(
            SiteBatchActionResult(successIds = listOf(1, 2))
                .toSiteBatchOperationMessage("enable")
        ).isEqualTo("Batch enable completed: 2 succeeded.")

        assertThat(
            SiteBatchActionResult(
                successIds = listOf(1),
                failedItems = listOf(
                    SiteBatchFailedItem(id = 2, message = "locked"),
                    SiteBatchFailedItem(id = 3, message = ""),
                    SiteBatchFailedItem(id = 4, message = "gone"),
                    SiteBatchFailedItem(id = 5, message = "busy"),
                ),
            ).toSiteBatchOperationMessage("delete")
        ).isEqualTo(
            "Batch delete completed: 1 succeeded, 4 failed: #2 locked; #3 Unknown error; #4 gone; 1 more failed."
        )
    }
}
