package com.elykia.octopus.feature.model

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ModelStateTest {
    @Test
    fun modelPageErrorOnlyFollowsListFailureWithoutCachedModels() {
        assertThat(ModelUiState(modelListError = "models failed").shouldShowModelPageError()).isTrue()

        assertThat(
            ModelUiState(
                models = listOf(LlmInfo(name = "gpt-4o")),
                modelListError = "models failed",
            ).shouldShowModelPageError()
        ).isFalse()
    }

    @Test
    fun modelRefreshKeepsPartialDataWhenSecondaryRequestsFail() {
        val previousChannels = listOf(
            LlmChannel(name = "gpt-4o", enabled = true, channelId = 1, channelName = "OpenAI"),
        )
        val state = buildModelRefreshState(
            previous = ModelUiState(
                modelChannels = previousChannels,
                modelLastUpdateTime = "old-time",
            ),
            modelsResult = AppResult.Success(listOf(LlmInfo(name = "gpt-4o", input = 1.0))),
            modelChannelsResult = AppResult.Error("channels failed"),
            lastUpdateResult = AppResult.Error("time failed"),
        )

        assertThat(state.loading).isFalse()
        assertThat(state.models).containsExactly(LlmInfo(name = "gpt-4o", input = 1.0))
        assertThat(state.modelChannels).isEqualTo(previousChannels)
        assertThat(state.modelLastUpdateTime).isEqualTo("old-time")
        assertThat(state.modelChannelError).isEqualTo("channels failed")
        assertThat(state.modelLastUpdateError).isEqualTo("time failed")
    }

    @Test
    fun modelFilterAndSortMatchesWebModelToolbarSemantics() {
        val models = listOf(
            LlmInfo(name = "z-free"),
            LlmInfo(name = "a-paid", input = 0.01),
            LlmInfo(name = "m-paid", cacheWrite = 0.02),
        )

        assertThat(filterAndSortModels(models, "", ModelFilter.Priced, ModelSort.NameAsc).map { it.name })
            .containsExactly("a-paid", "m-paid")
            .inOrder()
        assertThat(filterAndSortModels(models, "paid", ModelFilter.All, ModelSort.NameDesc).map { it.name })
            .containsExactly("m-paid", "a-paid")
            .inOrder()
        assertThat(filterAndSortModels(models, "", ModelFilter.Free, ModelSort.NameAsc).map { it.name })
            .containsExactly("z-free")
    }

    @Test
    fun modelEditorParsesBlankPricesAsZeroAndTrimsName() {
        val values = parseModelEditorValues(
            name = " gpt-4o ",
            input = "",
            output = "0.02",
            cacheRead = "0",
            cacheWrite = "0.01",
        ).getOrThrow()

        assertThat(values.toModel()).isEqualTo(
            LlmInfo(
                name = "gpt-4o",
                input = 0.0,
                output = 0.02,
                cacheRead = 0.0,
                cacheWrite = 0.01,
            ),
        )
    }

    @Test
    fun modelEditorRejectsBlankNameAndInvalidPrices() {
        assertThat(canSubmitModelEditor("", "0", "0", "0", "0", submitting = false)).isFalse()
        assertThat(canSubmitModelEditor("gpt-4o", "-1", "0", "0", "0", submitting = false)).isFalse()
        assertThat(canSubmitModelEditor("gpt-4o", "NaN", "0", "0", "0", submitting = false)).isFalse()
        assertThat(canSubmitModelEditor("gpt-4o", "0", "0", "0", "0", submitting = true)).isFalse()
    }
}
