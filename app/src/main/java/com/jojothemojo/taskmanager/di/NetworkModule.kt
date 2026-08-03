package com.jojothemojo.taskmanager.di

import com.jojothemojo.taskmanager.data.remote.AuthInterceptor
import com.jojothemojo.taskmanager.data.remote.task.TaskApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

// Local-dev-only, not a secret or a production endpoint - the emulator's special alias
// for the host machine's loopback. Becomes a real (per-build-variant / BuildConfig)
// value once there's an actual Azure-hosted backend to point at.
//
// If this can't reach the backend on a given machine (connect timeouts specifically from
// app-owned network traffic, while `adb shell` can reach the same host:port fine), see
// AGENT.md's "10.0.2.2 unreachable from app UID" section for the `adb reverse` workaround -
// that's an AVD-environment quirk, not a bug in this app.
private const val BASE_URL = "http://10.0.2.2:5253/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // The backend's TaskEntity JSON includes fields (e.g. userId) that this app's
        // TaskDto deliberately doesn't model - without this, deserialization throws.
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideTaskApiService(retrofit: Retrofit): TaskApiService = retrofit.create(TaskApiService::class.java)
}
