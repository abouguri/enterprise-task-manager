package com.jojothemojo.taskmanager.di

import com.jojothemojo.taskmanager.data.remote.auth.MsalAuthRepositoryImpl
import com.jojothemojo.taskmanager.data.repository.TaskRepositoryImpl
import com.jojothemojo.taskmanager.domain.repository.AuthRepository
import com.jojothemojo.taskmanager.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(impl: MsalAuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}
