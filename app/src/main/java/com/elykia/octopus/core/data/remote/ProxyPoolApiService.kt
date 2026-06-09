package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.ProxyConfiguration
import com.elykia.octopus.core.data.model.ProxyConfigurationCreateRequest
import com.elykia.octopus.core.data.model.ProxyConfigurationReference
import com.elykia.octopus.core.data.model.ProxyConfigurationUpdateRequest
import com.elykia.octopus.core.data.model.ProxyTestRequest
import com.elykia.octopus.core.data.model.ProxyTestResult
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProxyPoolApiService {
    @GET("/api/v1/proxy-pool/list")
    suspend fun proxyConfigurations(): ApiEnvelope<List<ProxyConfiguration>?>

    @GET("/api/v1/proxy-pool/references/{id}")
    suspend fun references(@Path("id") id: Int): ApiEnvelope<List<ProxyConfigurationReference>?>

    @POST("/api/v1/proxy-pool/create")
    suspend fun createProxyConfiguration(
        @Body request: ProxyConfigurationCreateRequest,
    ): ApiEnvelope<ProxyConfiguration>

    @POST("/api/v1/proxy-pool/update")
    suspend fun updateProxyConfiguration(
        @Body request: ProxyConfigurationUpdateRequest,
    ): ApiEnvelope<ProxyConfiguration>

    @DELETE("/api/v1/proxy-pool/delete/{id}")
    suspend fun deleteProxyConfiguration(@Path("id") id: Int): ApiEnvelope<String?>

    @POST("/api/v1/proxy-pool/test")
    suspend fun testProxyConfiguration(@Body request: ProxyTestRequest): ApiEnvelope<ProxyTestResult>
}
