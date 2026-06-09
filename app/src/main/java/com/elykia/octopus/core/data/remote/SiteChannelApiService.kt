package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SiteChannelApiService {
    @GET("/api/v1/site-channel/list")
    suspend fun siteChannels(
        @Query("include_history") includeHistory: Boolean = true,
    ): ApiEnvelope<List<SiteChannelCard>?>

    @PUT("/api/v1/site-channel/{siteId}/account/{accountId}/model-routes")
    suspend fun updateModelRoutes(
        @Path("siteId") siteId: Int,
        @Path("accountId") accountId: Int,
        @Body request: List<SiteModelRouteUpdateRequest>,
    ): ApiEnvelope<SiteChannelAccount>

    @POST("/api/v1/site-channel/{siteId}/account/{accountId}/model-routes/reset")
    suspend fun resetModelRoutes(
        @Path("siteId") siteId: Int,
        @Path("accountId") accountId: Int,
        @Body body: Map<String, String> = emptyMap(),
    ): ApiEnvelope<SiteChannelAccount>

    @POST("/api/v1/site-channel/{siteId}/account/{accountId}/keys")
    suspend fun createKey(
        @Path("siteId") siteId: Int,
        @Path("accountId") accountId: Int,
        @Body request: SiteChannelKeyCreateRequest,
    ): ApiEnvelope<SiteChannelAccount>

    @PUT("/api/v1/site-channel/{siteId}/account/{accountId}/source-keys")
    suspend fun updateSourceKeys(
        @Path("siteId") siteId: Int,
        @Path("accountId") accountId: Int,
        @Body request: SiteSourceKeyUpdateRequest,
    ): ApiEnvelope<SiteChannelAccount>

    @PUT("/api/v1/site-channel/{siteId}/account/{accountId}/projected-channel-settings")
    suspend fun updateProjectedChannelSettings(
        @Path("siteId") siteId: Int,
        @Path("accountId") accountId: Int,
        @Body request: List<SiteProjectedChannelSettingsUpdateRequest>,
    ): ApiEnvelope<SiteChannelAccount>

    @POST("/api/v1/site-channel/{siteId}/account/{accountId}/manual-models")
    suspend fun addManualModels(
        @Path("siteId") siteId: Int,
        @Path("accountId") accountId: Int,
        @Body request: SiteManualModelAddRequest,
    ): ApiEnvelope<SiteChannelAccount>

    @POST("/api/v1/site-channel/{siteId}/account/{accountId}/manual-models/delete")
    suspend fun deleteManualModel(
        @Path("siteId") siteId: Int,
        @Path("accountId") accountId: Int,
        @Body request: SiteManualModelDeleteRequest,
    ): ApiEnvelope<SiteChannelAccount>

    @PUT("/api/v1/site-channel/{siteId}/account/{accountId}/model-disabled")
    suspend fun updateModelDisabled(
        @Path("siteId") siteId: Int,
        @Path("accountId") accountId: Int,
        @Body request: List<SiteModelDisableUpdateRequest>,
    ): ApiEnvelope<SiteChannelAccount>

    @PUT("/api/v1/site-channel/{siteId}/account/{accountId}/group-projection")
    suspend fun updateGroupProjection(
        @Path("siteId") siteId: Int,
        @Path("accountId") accountId: Int,
        @Body request: SiteGroupProjectionUpdateRequest,
    ): ApiEnvelope<SiteChannelAccount>
}
