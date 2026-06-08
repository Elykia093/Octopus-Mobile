package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.EntityEnableRequest
import com.elykia.octopus.core.data.model.Site
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.model.SiteAccountCreateRequest
import com.elykia.octopus.core.data.model.SiteAccountUpdateRequest
import com.elykia.octopus.core.data.model.SiteCheckinResult
import com.elykia.octopus.core.data.model.SiteCreateRequest
import com.elykia.octopus.core.data.model.SiteSyncResult
import com.elykia.octopus.core.data.model.SiteUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SiteApiService {
    @GET("/api/v1/site/list")
    suspend fun sites(): ApiEnvelope<List<Site>?>

    @GET("/api/v1/site/archived")
    suspend fun archivedSites(): ApiEnvelope<List<Site>?>

    @POST("/api/v1/site/create")
    suspend fun createSite(@Body request: SiteCreateRequest): ApiEnvelope<Site>

    @POST("/api/v1/site/update")
    suspend fun updateSite(@Body request: SiteUpdateRequest): ApiEnvelope<Site>

    @POST("/api/v1/site/enable")
    suspend fun enableSite(@Body request: EntityEnableRequest): ApiEnvelope<String?>

    @DELETE("/api/v1/site/delete/{id}")
    suspend fun deleteSite(@Path("id") id: Int): ApiEnvelope<String?>

    @POST("/api/v1/site/archive/{id}")
    suspend fun archiveSite(@Path("id") id: Int): ApiEnvelope<String?>

    @POST("/api/v1/site/restore/{id}")
    suspend fun restoreSite(@Path("id") id: Int): ApiEnvelope<String?>

    @POST("/api/v1/site/account/create")
    suspend fun createAccount(@Body request: SiteAccountCreateRequest): ApiEnvelope<SiteAccount>

    @POST("/api/v1/site/account/update")
    suspend fun updateAccount(@Body request: SiteAccountUpdateRequest): ApiEnvelope<SiteAccount>

    @POST("/api/v1/site/account/enable")
    suspend fun enableAccount(@Body request: EntityEnableRequest): ApiEnvelope<String?>

    @DELETE("/api/v1/site/account/delete/{id}")
    suspend fun deleteAccount(@Path("id") id: Int): ApiEnvelope<String?>

    @POST("/api/v1/site/account/sync/{id}")
    suspend fun syncAccount(@Path("id") id: Int, @Body body: Map<String, String> = emptyMap()): ApiEnvelope<SiteSyncResult>

    @POST("/api/v1/site/account/checkin/{id}")
    suspend fun checkinAccount(@Path("id") id: Int, @Body body: Map<String, String> = emptyMap()): ApiEnvelope<SiteCheckinResult>

    @POST("/api/v1/site/sync-all")
    suspend fun syncAll(@Body body: Map<String, String> = emptyMap()): ApiEnvelope<String?>

    @POST("/api/v1/site/checkin-all")
    suspend fun checkinAll(@Body body: Map<String, String> = emptyMap()): ApiEnvelope<String?>
}
