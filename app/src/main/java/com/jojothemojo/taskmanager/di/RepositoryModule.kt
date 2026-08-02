package com.jojothemojo.taskmanager.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// @Binds bindings from domain/repository interfaces to data/repository impls land here in a later phase.
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule
