package com.jojothemojo.taskmanager.domain.repository

import com.jojothemojo.taskmanager.domain.model.AuthState
import com.jojothemojo.taskmanager.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>

    // Typed as Any, not android.app.Activity: domain/ may not depend on Android.
    // MSAL's interactive flow needs the current foreground Activity; the data-layer
    // implementation casts it back.
    suspend fun signIn(activity: Any)

    suspend fun signOut()
    suspend fun getCurrentAccount(): User?
}
