package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.ProxyConfiguration
import com.elykia.octopus.core.data.model.ProxyConfigurationCreateRequest
import com.elykia.octopus.core.data.model.ProxyConfigurationReference
import com.elykia.octopus.core.data.model.ProxyConfigurationUpdateRequest
import com.elykia.octopus.core.data.model.ProxyTestRequest
import com.elykia.octopus.core.data.model.ProxyTestResult
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.ProxyPoolApiService
import com.elykia.octopus.core.data.remote.sanitizeErrorMessage
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProxyPoolRepository @Inject constructor(
    private val apiService: ProxyPoolApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun proxyConfigurations(): AppResult<List<ProxyConfiguration>> = withContext(dispatchers.io) {
        when (val result = executor.executeNullable { apiService.proxyConfigurations() }) {
            is AppResult.Success -> AppResult.Success(result.data.orEmpty())
            is AppResult.Error -> result
        }
    }

    suspend fun references(id: Int): AppResult<List<ProxyConfigurationReference>> = withContext(dispatchers.io) {
        when (val result = executor.executeNullable { apiService.references(id) }) {
            is AppResult.Success -> AppResult.Success(result.data.orEmpty())
            is AppResult.Error -> result
        }
    }

    suspend fun createProxyConfiguration(
        request: ProxyConfigurationCreateRequest,
    ): AppResult<ProxyConfiguration> = withContext(dispatchers.io) {
        executor.execute { apiService.createProxyConfiguration(request) }
    }

    suspend fun updateProxyConfiguration(
        request: ProxyConfigurationUpdateRequest,
    ): AppResult<ProxyConfiguration> = withContext(dispatchers.io) {
        executor.execute { apiService.updateProxyConfiguration(request) }
    }

    suspend fun deleteProxyConfiguration(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteProxyConfiguration(id) }
    }

    suspend fun testProxyConfiguration(request: ProxyTestRequest): AppResult<ProxyTestResult> = withContext(dispatchers.io) {
        when (val result = executor.execute { apiService.testProxyConfiguration(request) }) {
            is AppResult.Success -> AppResult.Success(result.data.copy(message = result.data.message.sanitizeProxyMessage()))
            is AppResult.Error -> result
        }
    }
}

private fun String.sanitizeProxyMessage(): String =
    PROXY_CREDENTIAL_PATTERN.replace(sanitizeErrorMessage()) { match ->
        "${match.groups[1]?.value.orEmpty()}${match.groups[2]?.value.orEmpty()}:***@"
    }

private val PROXY_CREDENTIAL_PATTERN = Regex(
    pattern = """(?i)\b((?:https?|socks4|socks5)://)([^/@\s:]+):([^/@\s]+)@""",
)
