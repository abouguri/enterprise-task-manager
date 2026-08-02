package com.jojothemojo.taskmanager.data.remote.auth

import android.app.Activity
import com.jojothemojo.taskmanager.domain.model.AuthState
import com.jojothemojo.taskmanager.domain.model.User
import com.jojothemojo.taskmanager.domain.repository.AuthRepository
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.CurrentAccountCallback
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.SignOutCallback
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.exception.MsalException
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Scope exposed by our own backend API (TaskManager.Api), not Microsoft Graph - nothing in
// this app calls Graph, so there's no reason to request its User.Read scope. Must match the
// "access_as_user" scope's Application ID URI from the same Entra ID App Registration used for
// sign-in (see TaskManager-Api's AGENT.md §6 for the Azure Portal side of this).
private val DEFAULT_SCOPES = arrayOf("api://53c04bd3-4155-4a67-9d4c-1ac161d92801/access_as_user")

@Singleton
class MsalAuthRepositoryImpl @Inject constructor(
    // Injected as Lazy so the blocking PublicClientApplication creation (config parsing +
    // OIDC metadata discovery) only runs on first use, inside pca(), off the main thread —
    // not eagerly whenever Hilt first wires up this repository.
    private val pcaProvider: Lazy<ISingleAccountPublicClientApplication>,
) : AuthRepository {

    private val authState = MutableStateFlow<AuthState>(AuthState.Loading)

    override fun observeAuthState(): Flow<AuthState> = authState.onStart { refreshCurrentAccount() }

    override suspend fun signIn(activity: Any) {
        authState.value = AuthState.Loading
        val result = try {
            val pca = pca()
            suspendCancellableCoroutine<IAuthenticationResult?> { continuation ->
                val params = SignInParameters.builder()
                    .withActivity(activity as Activity)
                    .withScopes(DEFAULT_SCOPES.toList())
                    .withCallback(object : AuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult) {
                            continuation.resume(authenticationResult)
                        }

                        override fun onError(exception: MsalException) {
                            continuation.resumeWithException(exception)
                        }

                        override fun onCancel() {
                            continuation.resume(null)
                        }
                    })
                    .build()
                pca.signIn(params)
            }
        } catch (e: MsalException) {
            authState.value = AuthState.Error(e.message ?: "Sign-in failed")
            return
        }
        authState.value = if (result != null) {
            AuthState.Authenticated(result.account.toDomainUser())
        } else {
            AuthState.Unauthenticated
        }
    }

    override suspend fun signOut() {
        try {
            val pca = pca()
            suspendCancellableCoroutine<Unit> { continuation ->
                pca.signOut(object : SignOutCallback {
                    override fun onSignOut() {
                        continuation.resume(Unit)
                    }

                    override fun onError(exception: MsalException) {
                        continuation.resumeWithException(exception)
                    }
                })
            }
            authState.value = AuthState.Unauthenticated
        } catch (e: MsalException) {
            authState.value = AuthState.Error(e.message ?: "Sign-out failed")
        }
    }

    override suspend fun getCurrentAccount(): User? =
        try {
            fetchCurrentAccount()?.toDomainUser()
        } catch (e: MsalException) {
            null
        }

    private suspend fun refreshCurrentAccount() {
        authState.value = AuthState.Loading
        authState.value = try {
            val account = fetchCurrentAccount()
            if (account != null) AuthState.Authenticated(account.toDomainUser()) else AuthState.Unauthenticated
        } catch (e: MsalException) {
            AuthState.Error(e.message ?: "Failed to load current account")
        }
    }

    private suspend fun fetchCurrentAccount(): IAccount? {
        val pca = pca()
        return suspendCancellableCoroutine { continuation ->
            pca.getCurrentAccountAsync(object : CurrentAccountCallback {
                override fun onAccountLoaded(activeAccount: IAccount?) {
                    continuation.resume(activeAccount)
                }

                override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                    // onAccountLoaded already resolves this call; nothing to do here.
                }

                override fun onError(exception: MsalException) {
                    continuation.resumeWithException(exception)
                }
            })
        }
    }

    private suspend fun pca(): ISingleAccountPublicClientApplication =
        withContext(Dispatchers.IO) { pcaProvider.get() }
}
