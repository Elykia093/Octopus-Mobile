package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ServerConfig
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl

@Singleton
class ServerUrlProvider @Inject constructor() {
    @Volatile
    private var baseUrl: HttpUrl = ServerUrlResolver.normalize(ServerConfig())

    fun current(): HttpUrl = baseUrl

    fun update(config: ServerConfig) {
        baseUrl = ServerUrlResolver.normalize(config)
    }
}
