package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.ApiKeyMutationRequest
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.ChannelEnableRequest
import com.elykia.octopus.core.data.model.ChannelFetchModelRequest
import com.elykia.octopus.core.data.model.ChannelUpdateRequest
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupUpdateRequest
import com.elykia.octopus.core.data.model.ImportResult
import com.elykia.octopus.core.data.model.LatestInfo
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsHourly
import com.elykia.octopus.core.data.model.StatsTotal
import com.elykia.octopus.core.data.model.UserLoginRequest
import com.elykia.octopus.core.data.model.UserLoginResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
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

    @GET("/api/v1/stats/today")
    suspend fun statsToday(): ApiEnvelope<StatsDaily>

    @GET("/api/v1/stats/daily")
    suspend fun statsDaily(): ApiEnvelope<List<StatsDaily>>

    @GET("/api/v1/stats/hourly")
    suspend fun statsHourly(): ApiEnvelope<List<StatsHourly>>

    @GET("/api/v1/stats/apikey")
    suspend fun statsApiKey(): ApiEnvelope<List<StatsApiKeyEntry>>

    @GET("/api/v1/channel/list")
    suspend fun channels(): ApiEnvelope<List<Channel>>

    @GET("/api/v1/group/list")
    suspend fun groups(): ApiEnvelope<List<Group>>

    @POST("/api/v1/channel/create")
    suspend fun createChannel(@Body request: Channel): ApiEnvelope<Channel>

    @POST("/api/v1/channel/update")
    suspend fun updateChannel(@Body request: ChannelUpdateRequest): ApiEnvelope<Channel>

    @POST("/api/v1/channel/fetch-model")
    suspend fun fetchChannelModels(@Body request: ChannelFetchModelRequest): ApiEnvelope<List<String>>

    @POST("/api/v1/channel/sync")
    suspend fun syncChannelModels(@Body body: Map<String, String> = emptyMap()): ApiEnvelope<String?>

    @POST("/api/v1/group/create")
    suspend fun createGroup(@Body request: Group): ApiEnvelope<Group>

    @POST("/api/v1/group/update")
    suspend fun updateGroup(@Body request: GroupUpdateRequest): ApiEnvelope<Group>

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

    @POST("/api/v1/apikey/create")
    suspend fun createApiKey(@Body request: ApiKeyMutationRequest): ApiEnvelope<ApiKeyItem>

    @POST("/api/v1/apikey/update")
    suspend fun updateApiKey(@Body request: ApiKeyMutationRequest): ApiEnvelope<ApiKeyItem>

    @DELETE("/api/v1/apikey/delete/{id}")
    suspend fun deleteApiKey(@Path("id") id: Int): ApiEnvelope<String?>

    @POST("/api/v1/channel/enable")
    suspend fun enableChannel(@Body request: ChannelEnableRequest): ApiEnvelope<String?>

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
    ): Response<ResponseBody>

    @Multipart
    @POST("/api/v1/setting/import")
    suspend fun importData(@Part file: MultipartBody.Part): ApiEnvelope<ImportResult>

    @DELETE("/api/v1/channel/delete/{id}")
    suspend fun deleteChannel(@Path("id") id: Int): ApiEnvelope<String?>

    @DELETE("/api/v1/group/delete/{id}")
    suspend fun deleteGroup(@Path("id") id: Int): ApiEnvelope<String?>
}
