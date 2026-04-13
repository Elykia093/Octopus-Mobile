package com.elykia.octopus.feature.app

import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.ServerConfig

sealed interface LaunchState {
    data object Loading : LaunchState
    data class NeedServer(val config: ServerConfig) : LaunchState
    data class NeedLogin(val config: ServerConfig) : LaunchState
    data class Ready(val config: ServerConfig, val authState: AuthState) : LaunchState
}
