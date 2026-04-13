package com.elykia.octopus.feature.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelUiState(
    val loading: Boolean = true,
    val models: List<LlmInfo> = emptyList(),
    val channels: List<LlmChannel> = emptyList(),
    val lastUpdateTime: String? = null,
    val error: String? = null,
)

@HiltViewModel
class ModelViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ModelUiState())
    val uiState: StateFlow<ModelUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val modelsDeferred = async { repository.models() }
            val channelsDeferred = async { repository.modelChannels() }
            val timeDeferred = async { repository.modelLastUpdateTime() }

            val models = modelsDeferred.await()
            val channels = channelsDeferred.await()
            val time = timeDeferred.await()

            if (models is AppResult.Success) {
                _uiState.value = ModelUiState(
                    loading = false,
                    models = models.data,
                    channels = (channels as? AppResult.Success)?.data.orEmpty(),
                    lastUpdateTime = (time as? AppResult.Success)?.data,
                )
            } else {
                _uiState.value = ModelUiState(loading = false, error = (models as AppResult.Error).message)
            }
        }
    }

    fun refreshPrice() {
        viewModelScope.launch {
            repository.refreshModelPrice()
            refresh()
        }
    }
}
