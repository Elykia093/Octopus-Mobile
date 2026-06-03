package com.elykia.octopus.core.data.local

import com.elykia.octopus.core.data.model.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private val authState = MutableStateFlow(AuthState())
    private val unauthorizedState = MutableStateFlow(false)
    private val securityWarningState = MutableStateFlow<String?>(null)

    val unauthorized: Flow<Boolean> = unauthorizedState.asStateFlow()
    val securityWarning: Flow<String?> = securityWarningState.asStateFlow()

    fun update(authState: AuthState) {
        this.authState.value = authState
        unauthorizedState.value = false
    }

    fun clear() {
        authState.value = AuthState()
        unauthorizedState.value = true
    }

    fun markSecurityWarning(message: String) {
        securityWarningState.value = message
    }

    fun consumeUnauthorized() {
        unauthorizedState.value = false
    }

    fun consumeSecurityWarning() {
        securityWarningState.value = null
    }

    fun currentAuth(): AuthState = authState.value
}
