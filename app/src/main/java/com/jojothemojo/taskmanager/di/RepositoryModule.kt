package com.jojothemojo.taskmanager.di

import com.jojothemojo.taskmanager.data.remote.auth.MsalAuthRepositoryImpl
import com.jojothemojo.taskmanager.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Further @Binds bindings from domain/repository interfaces to data/repository impls land here.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(impl: MsalAuthRepositoryImpl): AuthRepository
}
