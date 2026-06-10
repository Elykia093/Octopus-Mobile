package com.elykia.octopus.feature.site

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
}
