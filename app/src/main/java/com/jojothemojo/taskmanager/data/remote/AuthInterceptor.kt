package com.jojothemojo.taskmanager.data.remote

import com.jojothemojo.taskmanager.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// runBlocking here is intentional, not an anti-pattern: OkHttp's Interceptor.intercept()
// is inherently synchronous and already runs on OkHttp's own dispatcher thread, never the
// main thread - this is the standard way to bridge a suspend call into that contract.
class AuthInterceptor @Inject constructor(
    private val authRepository: AuthRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authRepository.getAccessToken() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
