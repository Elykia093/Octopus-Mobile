package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.EntityEnableRequest
import com.elykia.octopus.core.data.model.Site
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.model.SiteAccountCreateRequest
import com.elykia.octopus.core.data.model.SiteAccountUpdateRequest
import com.elykia.octopus.core.data.model.SiteCheckinResult
import com.elykia.octopus.core.data.model.SiteCreateRequest
import com.elykia.octopus.core.data.model.SiteSyncResult
import com.elykia.octopus.core.data.model.SiteToken
import com.elykia.octopus.core.data.model.SiteUpdateRequest
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.SiteApiService
import com.elykia.octopus.core.data.remote.sanitizeErrorMessage
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteRepository @Inject constructor(
    private val apiService: SiteApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun sites(): AppResult<List<Site>> = withContext(dispatchers.io) {
        when (val result = executor.executeNullable { apiService.sites() }) {
            is AppResult.Success -> AppResult.Success(result.data?.map { it.withHiddenSecrets() } ?: emptyList())
            is AppResult.Error -> result
        }
    }

    suspend fun archivedSites(): AppResult<List<Site>> = withContext(dispatchers.io) {
        when (val result = executor.executeNullable { apiService.archivedSites() }) {
            is AppResult.Success -> AppResult.Success(result.data?.map { it.withHiddenSecrets() } ?: emptyList())
            is AppResult.Error -> result
        }
    }

    suspend fun createSite(request: SiteCreateRequest): AppResult<Site> = withContext(dispatchers.io) {
        when (val result = executor.execute { apiService.createSite(request) }) {
            is AppResult.Success -> AppResult.Success(result.data.withHiddenSecrets())
            is AppResult.Error -> result
        }
    }

    suspend fun updateSite(request: SiteUpdateRequest): AppResult<Site> = withContext(dispatchers.io) {
        when (val result = executor.execute { apiService.updateSite(request) }) {
            is AppResult.Success -> AppResult.Success(result.data.withHiddenSecrets())
            is AppResult.Error -> result
        }
    }

    suspend fun setSiteEnabled(id: Int, enabled: Boolean): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.enableSite(EntityEnableRequest(id = id, enabled = enabled)) }
    }

    suspend fun archiveSite(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.archiveSite(id) }
    }

    suspend fun restoreSite(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.restoreSite(id) }
    }

    suspend fun deleteSite(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteSite(id) }
    }

    suspend fun createAccount(request: SiteAccountCreateRequest): AppResult<SiteAccount> = withContext(dispatchers.io) {
        when (val result = executor.execute { apiService.createAccount(request) }) {
            is AppResult.Success -> AppResult.Success(result.data.withHiddenSecrets())
            is AppResult.Error -> result
        }
    }

    suspend fun updateAccount(request: SiteAccountUpdateRequest): AppResult<SiteAccount> = withContext(dispatchers.io) {
        when (val result = executor.execute { apiService.updateAccount(request) }) {
            is AppResult.Success -> AppResult.Success(result.data.withHiddenSecrets())
            is AppResult.Error -> result
        }
    }

    suspend fun setAccountEnabled(id: Int, enabled: Boolean): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.enableAccount(EntityEnableRequest(id = id, enabled = enabled)) }
    }

    suspend fun deleteAccount(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteAccount(id) }
    }

    suspend fun syncAccount(id: Int): AppResult<SiteSyncResult> = withContext(dispatchers.io) {
        executor.execute { apiService.syncAccount(id) }
    }

    suspend fun checkinAccount(id: Int): AppResult<SiteCheckinResult> = withContext(dispatchers.io) {
        executor.execute { apiService.checkinAccount(id) }
    }

    suspend fun syncAll(): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.syncAll() }
    }

    suspend fun checkinAll(): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.checkinAll() }
    }
}

internal fun Site.withHiddenSecrets(): Site = copy(
    customHeader = customHeader ?: emptyList(),
    accounts = accounts?.map { it.withHiddenSecrets() } ?: emptyList(),
)

internal fun SiteAccount.withHiddenSecrets(): SiteAccount = copy(
    password = "",
    accessToken = "",
    apiKey = "",
    refreshToken = "",
    lastSyncMessage = lastSyncMessage.sanitizeErrorMessage(),
    lastCheckinMessage = lastCheckinMessage.sanitizeErrorMessage(),
    tokens = tokens?.map { it.withHiddenSecret() } ?: emptyList(),
    userGroups = userGroups ?: emptyList(),
    models = models ?: emptyList(),
    channelBindings = channelBindings ?: emptyList(),
)

private fun SiteToken.withHiddenSecret(): SiteToken = copy(token = "")
