package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.SettingItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SettingApiService {
    @GET("/api/v1/setting/list")
    suspend fun settings(): ApiEnvelope<List<SettingItem>>

    @POST("/api/v1/setting/set")
    suspend fun setSetting(@Body setting: SettingItem): ApiEnvelope<SettingItem>
}
