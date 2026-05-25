package com.elykia.octopus.feature.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.data.remote.ModelApiService
import com.elykia.octopus.core.data.remote.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.util.Locale
import javax.inject.Inject

data class ModelUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSyncingPrice: Boolean = false,
    val error: String? = null,
    val models: List<LlmInfo> = emptyList(),
    val channels: List<LlmChannel> = emptyList(),
    val lastUpdateTime: String = "",
) {
    val channelsByModel: Map<String, List<LlmChannel>>
        get() = channels.groupBy { it.name }
}

@HiltViewModel
class ModelViewModel @Inject constructor(
    private val modelApi: ModelApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelUiState())
    val uiState: StateFlow<ModelUiState> = _uiState.asStateFlow()

    init {
        loadModels()
    }

    fun loadModels(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                val modelsDeferred = async { modelApi.getModels() }
                val channelsDeferred = async { modelApi.getModelChannels() }
                val lastUpdateDeferred = async { modelApi.getLastUpdateTime() }
                val modelsResponse = modelsDeferred.await()
                val channelsResponse = channelsDeferred.await()
                val lastUpdateResponse = lastUpdateDeferred.await()

                val firstError = listOf(modelsResponse, channelsResponse, lastUpdateResponse)
                    .firstOrNull { !it.isSuccessful }
                    ?.message

                if (firstError == null) {
                    _uiState.update {
                        it.copy(
                            models = modelsResponse.data.orEmpty().sortedBy { model ->
                                model.name.lowercase(Locale.getDefault())
                            },
                            channels = channelsResponse.data.orEmpty(),
                            lastUpdateTime = lastUpdateResponse.data.orEmpty(),
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = firstError.ifBlank { "加载模型价格失败" },
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.toUserMessage(), isLoading = false, isRefreshing = false)
                }
            }
        }
    }

    fun syncPrice() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingPrice = true, error = null) }
            try {
                val response = modelApi.updatePrice(JsonObject(emptyMap()))
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSyncingPrice = false) }
                    loadModels(isRefresh = true)
                } else {
                    _uiState.update {
                        it.copy(
                            error = response.message.ifBlank { "同步模型价格失败" },
                            isSyncingPrice = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.toUserMessage(), isSyncingPrice = false) }
            }
        }
    }
}
