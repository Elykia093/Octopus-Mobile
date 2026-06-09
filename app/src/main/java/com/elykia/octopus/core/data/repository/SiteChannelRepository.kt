package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.SiteChannelCard
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
}

private fun List<SiteChannelCard>.withoutSensitiveKeys(): List<SiteChannelCard> = map { card ->
    card.copy(
        accounts = card.accounts.map { account ->
            account.copy(
                groups = account.groups.map { group ->
                    group.copy(
                        sourceKeys = group.sourceKeys.map { sourceKey -> sourceKey.copy(token = "") },
                        projectedKeys = group.projectedKeys.map { projectedKey -> projectedKey.copy(channelKey = "") },
                    )
                },
            )
        },
    )
}
