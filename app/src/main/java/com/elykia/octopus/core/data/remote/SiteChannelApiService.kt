package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.SiteChannelCard
import retrofit2.http.GET
import retrofit2.http.Query

interface SiteChannelApiService {
    @GET("/api/v1/site-channel/list")
    suspend fun siteChannels(
        @Query("include_history") includeHistory: Boolean = true,
    ): ApiEnvelope<List<SiteChannelCard>?>
}
