package com.jojothemojo.taskmanager.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val IMMEDIATE_WORK_NAME = "sync_immediate"
private const val PERIODIC_WORK_NAME = "sync_periodic"

private val networkConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager get() = WorkManager.getInstance(context)

    // Enqueued after every local Room write (create/update/delete) in TaskRepositoryImpl, in
    // addition to the periodic run below - this is what makes sync feel near-instant while
    // online instead of waiting for the next periodic window. KEEP (not REPLACE) is
    // deliberate: SyncWorker re-reads the full pending queue from Room each time it runs, so
    // if one is already queued/running there's nothing a second enqueue would add - it would
    // just be redundant work, not a way to sync anything sooner.
    fun scheduleImmediateSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints)
            .build()
        workManager.enqueueUniqueWork(IMMEDIATE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    // Backstop for anything the immediate-sync path missed (e.g. the app was killed before a
    // queued immediate sync ran). MIN_PERIODIC_INTERVAL_MILLIS is WorkManager's own enforced
    // floor for periodic work (15 minutes) - not a value chosen for this app specifically.
    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS,
        ).setConstraints(networkConstraints).build()
        workManager.enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
