package com.jojothemojo.taskmanager.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Retrofit/OkHttp providers land here in a later phase.
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule
