package com.jojothemojo.taskmanager.di

import android.content.Context
import com.jojothemojo.taskmanager.R
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    // Blocking call (config parsing + OIDC metadata discovery) — only ever invoked via
    // the dagger.Lazy<T> injected into MsalAuthRepositoryImpl, off the main thread.
    @Provides
    @Singleton
    fun providePublicClientApplication(
        @ApplicationContext context: Context,
    ): ISingleAccountPublicClientApplication =
        PublicClientApplication.createSingleAccountPublicClientApplication(context, R.raw.msal_config)
}
