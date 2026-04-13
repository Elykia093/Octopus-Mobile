package com.elykia.octopus.core.data.local

import com.elykia.octopus.core.data.model.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private val tokenState = MutableStateFlow("")
    private val unauthorizedState = MutableStateFlow(false)

    val token: Flow<String> = tokenState.asStateFlow()
    val unauthorized: Flow<Boolean> = unauthorizedState.asStateFlow()

    fun update(authState: AuthState) {
        tokenState.value = authState.token
        unauthorizedState.value = false
    }

    fun clear() {
        tokenState.value = ""
        unauthorizedState.value = true
    }

    fun consumeUnauthorized() {
        unauthorizedState.value = false
    }

    fun currentToken(): String = tokenState.value
}
