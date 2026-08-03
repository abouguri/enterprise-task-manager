package com.jojothemojo.taskmanager

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jojothemojo.taskmanager.data.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TaskManagerApplication : Application(), Configuration.Provider {

    // Field injection into the @HiltAndroidApp Application class itself is a documented
    // exception to Hilt's usual constructor-injection pattern - it's the standard way to get
    // HiltWorkerFactory here so SyncWorker's own dependencies (TaskDao, TaskApiService, Json)
    // get resolved through the same Hilt graph as everything else, not built by hand.
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncScheduler: SyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        syncScheduler.schedulePeriodicSync()
    }
}
