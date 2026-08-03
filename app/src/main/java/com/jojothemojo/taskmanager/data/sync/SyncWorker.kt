package com.jojothemojo.taskmanager.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jojothemojo.taskmanager.data.local.TaskDao
import com.jojothemojo.taskmanager.data.local.TaskEntity
import com.jojothemojo.taskmanager.data.local.toEntity
import com.jojothemojo.taskmanager.data.remote.task.CreateTaskRequestDto
import com.jojothemojo.taskmanager.data.remote.task.TaskApiService
import com.jojothemojo.taskmanager.data.remote.task.TaskDto
import com.jojothemojo.taskmanager.data.remote.task.UpdateTaskRequestDto
import com.jojothemojo.taskmanager.data.remote.task.toDomain
import com.jojothemojo.taskmanager.domain.model.SyncStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "SyncWorker"

// Processes every Room row with syncStatus != SYNCED against the backend's CRUD endpoints.
// Rows are handled independently, in whatever order getPendingSync() returns them in -
// there's no cross-row ordering dependency to worry about here. A single Task row carries
// exactly one syncStatus at a time (see TaskRepositoryImpl: an edit to a still-PENDING_CREATE
// row stays PENDING_CREATE rather than becoming PENDING_UPDATE, and deleting a never-synced
// row just removes it locally outright rather than queuing a delete) - so there's no scenario
// where a create and an update/delete for the SAME task both need to be sent, in either order.
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskDao: TaskDao,
    private val taskApiService: TaskApiService,
    private val json: Json,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = taskDao.getPendingSync()
        var anyTransientFailure = false

        for (entity in pending) {
            val succeeded = try {
                when (entity.syncStatus) {
                    SyncStatus.PENDING_CREATE -> syncCreate(entity)
                    SyncStatus.PENDING_UPDATE -> syncUpdate(entity)
                    SyncStatus.PENDING_DELETE -> syncDelete(entity)
                    SyncStatus.SYNCED -> true // Excluded by getPendingSync(); unreachable.
                }
            } catch (e: IOException) {
                // No connectivity, or backend unreachable - retry later, not an error to
                // surface. The CONNECTED network constraint on this worker (see
                // SyncScheduler) makes this rarer than it sounds, but connectivity can still
                // drop mid-run between the constraint being satisfied and the call landing.
                Log.w(TAG, "Network failure syncing task ${entity.id}, will retry", e)
                false
            } catch (e: HttpException) {
                // 401 (expired/invalid token) or 5xx - retry later. 409/404 are handled
                // inline below via Response<T>, not exceptions, so they never land here;
                // this only ever fires for createTask's plain (non-Response-wrapped) return.
                Log.w(TAG, "HTTP failure syncing task ${entity.id}: ${e.code()}, will retry", e)
                false
            }
            if (!succeeded) anyTransientFailure = true
        }

        // WorkManager's own backoff policy handles retiming - no custom retry loop here.
        return if (anyTransientFailure) Result.retry() else Result.success()
    }

    private suspend fun syncCreate(entity: TaskEntity): Boolean {
        val request = CreateTaskRequestDto(
            title = entity.title,
            description = entity.description,
            isCompleted = entity.isCompleted,
            dueDate = entity.dueDate?.toString(),
        )
        val created = taskApiService.createTask(request)
        // The backend never accepts a client-supplied Id on POST (CreateTaskRequest has no
        // Id field at all) - it always mints its own via Guid.NewGuid(), so the server's
        // returned Id will (in practice, always) differ from this row's client-generated
        // UUID primary key. Room can't "update" a primary key value via @Update (the WHERE
        // clause would look for a row that no longer has that id), so this is a
        // delete-then-insert, not an in-place update.
        val synced = created.toDomain().copy(syncStatus = SyncStatus.SYNCED)
        if (synced.id != entity.id) {
            taskDao.deleteById(entity.id)
        }
        taskDao.insert(synced.toEntity())
        return true
    }

    private suspend fun syncUpdate(entity: TaskEntity): Boolean {
        val request = UpdateTaskRequestDto(
            title = entity.title,
            description = entity.description,
            isCompleted = entity.isCompleted,
            dueDate = entity.dueDate?.toString(),
            updatedAt = entity.updatedAt.toString(),
        )
        val response = taskApiService.updateTask(entity.id, request)
        return when (response.code()) {
            200 -> {
                val body = response.body() ?: return false
                taskDao.update(body.toDomain().copy(syncStatus = SyncStatus.SYNCED).toEntity())
                true
            }
            409 -> {
                // Real conflict: the server rejected this row's UpdatedAt as stale.
                // Last-write-wins means the server's version wins outright here - there's no
                // merge UI in this project's scope - so the user's local edit is discarded.
                // Retrofit only runs the success-body converter for 2xx responses, so the
                // 409's body has to be read from errorBody() and decoded by hand.
                val serverTask = response.errorBody()?.string()
                    ?.let { json.decodeFromString<TaskDto>(it) }
                    ?: return false // Malformed/missing 409 body - retry later.
                Log.w(
                    TAG,
                    "Conflict on task ${entity.id}: local edit discarded, adopting server " +
                        "version (local updatedAt=${entity.updatedAt}, " +
                        "server updatedAt=${serverTask.updatedAt})",
                )
                taskDao.update(serverTask.toDomain().copy(syncStatus = SyncStatus.SYNCED).toEntity())
                true
            }
            404 -> {
                // Task no longer exists server-side (deleted elsewhere) - nothing left for
                // this pending edit to apply to. Drop it locally too; not a failure to retry.
                taskDao.deleteById(entity.id)
                true
            }
            else -> false // Unexpected status - treat as transient, retry later.
        }
    }

    private suspend fun syncDelete(entity: TaskEntity): Boolean {
        val response = taskApiService.deleteTask(entity.id)
        return when (response.code()) {
            204 -> {
                taskDao.deleteById(entity.id)
                true
            }
            404 -> {
                // Already gone server-side (or belonged to a since-changed session) - the
                // end state this delete wanted is already true. Not an error.
                taskDao.deleteById(entity.id)
                true
            }
            else -> false // Unexpected status - treat as transient, retry later.
        }
    }
}
