package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.SiteChannelAccount
import com.elykia.octopus.core.data.model.SiteChannelCard
import com.elykia.octopus.core.data.model.SiteChannelKeyCreateRequest
import com.elykia.octopus.core.data.model.SiteGroupProjectionUpdateRequest
import com.elykia.octopus.core.data.model.SiteManualModelAddRequest
import com.elykia.octopus.core.data.model.SiteManualModelDeleteRequest
import com.elykia.octopus.core.data.model.SiteModelDisableUpdateRequest
import com.elykia.octopus.core.data.model.SiteModelRouteUpdateRequest
import com.elykia.octopus.core.data.model.SiteProjectedChannelSettingsUpdateRequest
import com.elykia.octopus.core.data.model.SiteSourceKeyUpdateRequest
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.SiteChannelApiService
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteChannelRepository @Inject constructor(
    private val apiService: SiteChannelApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun siteChannels(includeHistory: Boolean = true): AppResult<List<SiteChannelCard>> = withContext(dispatchers.io) {
        when (val result = executor.executeNullable { apiService.siteChannels(includeHistory = includeHistory) }) {
            is AppResult.Success -> AppResult.Success(result.data.orEmpty().withoutSensitiveKeys())
            is AppResult.Error -> result
        }
    }

    suspend fun updateModelRoutes(
        siteId: Int,
        accountId: Int,
        request: List<SiteModelRouteUpdateRequest>,
    ): AppResult<SiteChannelAccount> = withContext(dispatchers.io) {
        sanitizeAccountResult {
            apiService.updateModelRoutes(siteId = siteId, accountId = accountId, request = request)
        }
    }

    suspend fun resetModelRoutes(siteId: Int, accountId: Int): AppResult<SiteChannelAccount> = withContext(dispatchers.io) {
        sanitizeAccountResult {
            apiService.resetModelRoutes(siteId = siteId, accountId = accountId)
        }
    }

    suspend fun createKey(
        siteId: Int,
        accountId: Int,
        request: SiteChannelKeyCreateRequest,
    ): AppResult<SiteChannelAccount> = withContext(dispatchers.io) {
        sanitizeAccountResult {
            apiService.createKey(siteId = siteId, accountId = accountId, request = request)
        }
    }

    suspend fun updateSourceKeys(
        siteId: Int,
        accountId: Int,
        request: SiteSourceKeyUpdateRequest,
    ): AppResult<SiteChannelAccount> = withContext(dispatchers.io) {
        sanitizeAccountResult {
            apiService.updateSourceKeys(siteId = siteId, accountId = accountId, request = request)
        }
    }

    suspend fun updateProjectedChannelSettings(
        siteId: Int,
        accountId: Int,
        request: List<SiteProjectedChannelSettingsUpdateRequest>,
    ): AppResult<SiteChannelAccount> = withContext(dispatchers.io) {
        sanitizeAccountResult {
            apiService.updateProjectedChannelSettings(siteId = siteId, accountId = accountId, request = request)
        }
    }

    suspend fun addManualModels(
        siteId: Int,
        accountId: Int,
        request: SiteManualModelAddRequest,
    ): AppResult<SiteChannelAccount> = withContext(dispatchers.io) {
        sanitizeAccountResult {
            apiService.addManualModels(siteId = siteId, accountId = accountId, request = request)
        }
    }

    suspend fun deleteManualModel(
        siteId: Int,
        accountId: Int,
        request: SiteManualModelDeleteRequest,
    ): AppResult<SiteChannelAccount> = withContext(dispatchers.io) {
        sanitizeAccountResult {
            apiService.deleteManualModel(siteId = siteId, accountId = accountId, request = request)
        }
    }

    suspend fun updateModelDisabled(
        siteId: Int,
        accountId: Int,
        request: List<SiteModelDisableUpdateRequest>,
    ): AppResult<SiteChannelAccount> = withContext(dispatchers.io) {
        sanitizeAccountResult {
            apiService.updateModelDisabled(siteId = siteId, accountId = accountId, request = request)
        }
    }

    suspend fun updateGroupProjection(
        siteId: Int,
        accountId: Int,
        request: SiteGroupProjectionUpdateRequest,
    ): AppResult<SiteChannelAccount> = withContext(dispatchers.io) {
        sanitizeAccountResult {
            apiService.updateGroupProjection(siteId = siteId, accountId = accountId, request = request)
        }
    }

    private suspend fun sanitizeAccountResult(
        block: suspend () -> com.elykia.octopus.core.data.model.ApiEnvelope<SiteChannelAccount>,
    ): AppResult<SiteChannelAccount> {
        return when (val result = executor.execute(block)) {
            is AppResult.Success -> AppResult.Success(result.data.withoutSensitiveKeys())
            is AppResult.Error -> result
        }
    }
}

private fun List<SiteChannelCard>.withoutSensitiveKeys(): List<SiteChannelCard> = map { card ->
    card.copy(
        accounts = card.accounts.map { account ->
            account.withoutSensitiveKeys()
        },
    )
}

private fun SiteChannelAccount.withoutSensitiveKeys(): SiteChannelAccount = copy(
    groups = groups.map { group ->
        group.copy(
            sourceKeys = group.sourceKeys.map { sourceKey -> sourceKey.copy(token = "") },
            projectedKeys = group.projectedKeys.map { projectedKey -> projectedKey.copy(channelKey = "") },
        )
    },
)
