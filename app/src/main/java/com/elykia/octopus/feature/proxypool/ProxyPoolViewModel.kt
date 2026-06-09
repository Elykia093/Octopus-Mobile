package com.elykia.octopus.feature.proxypool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ProxyConfiguration
import com.elykia.octopus.core.data.model.ProxyConfigurationReference
import com.elykia.octopus.core.data.model.ProxyTestRequest
import com.elykia.octopus.core.data.model.ProxyTestResult
import com.elykia.octopus.core.data.repository.ProxyPoolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProxyPoolUiState(
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val testingKey: String? = null,
    val referencesLoading: Boolean = false,
    val proxies: List<ProxyConfiguration> = emptyList(),
    val references: List<ProxyConfigurationReference> = emptyList(),
    val error: String? = null,
    val operationError: String? = null,
    val operationMessage: String? = null,
    val referencesError: String? = null,
)

internal fun ProxyPoolUiState.shouldShowPageError(): Boolean =
    error != null && proxies.isEmpty()

@HiltViewModel
class ProxyPoolViewModel @Inject constructor(
    private val repository: ProxyPoolRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProxyPoolUiState())
    val uiState: StateFlow<ProxyPoolUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null, operationError = null)
            when (val result = repository.proxyConfigurations()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        proxies = result.data.sortedWith(
                            compareByDescending<ProxyConfiguration> { it.enabled }
                                .thenBy { it.name.lowercase() }
                                .thenBy { it.id },
                        ),
                        error = null,
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(loading = false, error = result.message)
                }
            }
        }
    }

    fun clearOperationFeedback() {
        _uiState.value = _uiState.value.copy(operationError = null, operationMessage = null)
    }

    fun clearReferences() {
        _uiState.value = _uiState.value.copy(
            references = emptyList(),
            referencesLoading = false,
            referencesError = null,
        )
    }

    fun loadReferences(proxy: ProxyConfiguration) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(referencesLoading = true, referencesError = null, references = emptyList())
            when (val result = repository.references(proxy.id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(referencesLoading = false, references = result.data)
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(referencesLoading = false, referencesError = result.message)
                }
            }
        }
    }

    fun createProxy(values: ProxyPoolEditorValues, onSuccess: () -> Unit = {}) {
        launchMutation(
            invalidMessage = "请输入有效的代理名称和地址。",
            isValid = { canSubmitProxyConfiguration(values, submitting = false) },
            block = { repository.createProxyConfiguration(values.toCreateRequest()) },
            onSuccess = { onSuccess() },
        )
    }

    fun updateProxy(proxy: ProxyConfiguration, values: ProxyPoolEditorValues, onSuccess: () -> Unit = {}) {
        launchMutation(
            invalidMessage = "请输入有效的代理名称和地址。",
            isValid = { canSubmitProxyConfiguration(values, submitting = false) },
            block = { repository.updateProxyConfiguration(values.toUpdateRequest(proxy)) },
            onSuccess = { onSuccess() },
        )
    }

    fun deleteProxy(proxy: ProxyConfiguration) {
        launchMutation(
            invalidMessage = "该代理仍被引用，不能删除。",
            isValid = { proxy.referenceCount == 0 },
            block = { repository.deleteProxyConfiguration(proxy.id) },
        )
    }

    fun setProxyEnabled(proxy: ProxyConfiguration, enabled: Boolean) {
        launchMutation(
            block = {
                repository.updateProxyConfiguration(
                    proxy.copy(enabled = enabled).toEditorValues().toUpdateRequest(proxy),
                )
            },
        )
    }

    fun testSavedProxy(proxy: ProxyConfiguration, targetUrl: String = DEFAULT_PROXY_TEST_URL) {
        val request = if (proxy.enabled) {
            ProxyTestRequest(proxyConfigId = proxy.id, url = targetUrl.trim().ifBlank { DEFAULT_PROXY_TEST_URL })
        } else {
            ProxyTestRequest(proxyUrl = proxy.url, url = targetUrl.trim().ifBlank { DEFAULT_PROXY_TEST_URL })
        }
        testProxy(key = "saved-${proxy.id}", request = request)
    }

    fun testDraftProxy(values: ProxyPoolEditorValues, targetUrl: String = DEFAULT_PROXY_TEST_URL) {
        if (!values.url.isValidProxyUrl()) {
            _uiState.value = _uiState.value.copy(operationError = "请输入有效的代理地址。")
            return
        }
        testProxy(
            key = "draft",
            request = ProxyTestRequest(proxyUrl = values.url.trim(), url = targetUrl.trim().ifBlank { DEFAULT_PROXY_TEST_URL }),
        )
    }

    private fun testProxy(key: String, request: ProxyTestRequest) {
        viewModelScope.launch {
            if (_uiState.value.testingKey != null) return@launch
            _uiState.value = _uiState.value.copy(testingKey = key, operationError = null, operationMessage = null)
            when (val result = repository.testProxyConfiguration(request)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        testingKey = null,
                        operationMessage = result.data.toOperationMessage(),
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(testingKey = null, operationError = result.message)
                }
            }
        }
    }

    private fun <T> launchMutation(
        invalidMessage: String? = null,
        isValid: () -> Boolean = { true },
        block: suspend () -> AppResult<T>,
        onSuccess: (T) -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            if (!isValid()) {
                _uiState.value = _uiState.value.copy(operationError = invalidMessage ?: "输入无效。")
                return@launch
            }
            _uiState.value = _uiState.value.copy(submitting = true, operationError = null, operationMessage = null)
            when (val result = block()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false, operationError = null)
                    onSuccess(result.data)
                    refresh()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(submitting = false, operationError = result.message)
                }
            }
        }
    }
}

private fun ProxyTestResult.toOperationMessage(): String =
    if (success) {
        "代理测试成功：HTTP $statusCode · ${durationMs} ms。"
    } else {
        "代理测试失败：${message.ifBlank { "目标不可达。" }}"
    }
