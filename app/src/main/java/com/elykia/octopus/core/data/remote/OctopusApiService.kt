package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.ApiKeyDashboard
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.ImportResult
import com.elykia.octopus.core.data.model.LatestInfo
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsHourly
import com.elykia.octopus.core.data.model.StatsTotal
import com.elykia.octopus.core.data.model.UserLoginRequest
import com.elykia.octopus.core.data.model.UserLoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface OctopusApiService {
    @POST("/api/v1/user/login")
    suspend fun login(@Body request: UserLoginRequest): ApiEnvelope<UserLoginResponse>

    @GET("/api/v1/user/status")
    suspend fun status(): ApiEnvelope<String>

    @GET("/api/v1/stats/total")
    suspend fun statsTotal(): ApiEnvelope<StatsTotal>

    @GET("/api/v1/stats/daily")
    suspend fun statsDaily(): ApiEnvelope<List<StatsDaily>>

    @GET("/api/v1/stats/hourly")
    suspend fun statsHourly(): ApiEnvelope<List<StatsHourly>>

    @GET("/api/v1/channel/list")
    suspend fun channels(): ApiEnvelope<List<Channel>>

    @GET("/api/v1/group/list")
    suspend fun groups(): ApiEnvelope<List<Group>>

    @GET("/api/v1/model/list")
    suspend fun models(): ApiEnvelope<List<LlmInfo>>

    @GET("/api/v1/model/channel")
    suspend fun modelChannels(): ApiEnvelope<List<LlmChannel>>

    @POST("/api/v1/model/update-price")
    suspend fun updateModelPrice(@Body body: Map<String, String> = emptyMap()): ApiEnvelope<String?>

    @GET("/api/v1/model/last-update-time")
    suspend fun modelLastUpdateTime(): ApiEnvelope<String>

    @GET("/api/v1/log/list")
    suspend fun logs(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): ApiEnvelope<List<RelayLog>?>

    @DELETE("/api/v1/log/clear")
    suspend fun clearLogs(): ApiEnvelope<String?>

    @GET("/api/v1/setting/list")
    suspend fun settings(): ApiEnvelope<List<SettingItem>>

    @POST("/api/v1/setting/set")
    suspend fun setSetting(@Body setting: SettingItem): ApiEnvelope<SettingItem>

    @GET("/api/v1/apikey/list")
    suspend fun apiKeys(): ApiEnvelope<List<ApiKeyItem>>

    @GET("/api/v1/apikey/stats")
    suspend fun apiKeyStats(): ApiEnvelope<ApiKeyDashboard>

    @GET("/api/v1/update")
    suspend fun latestUpdate(): ApiEnvelope<LatestInfo>

    @GET("/api/v1/update/now-version")
    suspend fun currentVersion(): ApiEnvelope<String>

    @POST("/api/v1/update")
    suspend fun triggerUpdate(@Body body: Map<String, String> = emptyMap()): ApiEnvelope<String>

    @GET("/api/v1/setting/export")
    suspend fun exportData(
        @Query("include_logs") includeLogs: Boolean,
        @Query("include_stats") includeStats: Boolean,
    ): Response<RequestBody>

    @Multipart
    @POST("/api/v1/setting/import")
    suspend fun importData(@Part file: MultipartBody.Part): ApiEnvelope<ImportResult>

    @DELETE("/api/v1/channel/delete/{id}")
    suspend fun deleteChannel(@Path("id") id: Int): ApiEnvelope<String?>

    @DELETE("/api/v1/group/delete/{id}")
    suspend fun deleteGroup(@Path("id") id: Int): ApiEnvelope<String?>
}
