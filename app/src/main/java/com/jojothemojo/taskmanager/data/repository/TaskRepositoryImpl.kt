package com.jojothemojo.taskmanager.data.repository

import com.jojothemojo.taskmanager.data.local.TaskDao
import com.jojothemojo.taskmanager.data.local.toDomain
import com.jojothemojo.taskmanager.data.local.toEntity
import com.jojothemojo.taskmanager.data.remote.task.TaskApiService
import com.jojothemojo.taskmanager.data.remote.task.toDomain
import com.jojothemojo.taskmanager.data.sync.SyncScheduler
import com.jojothemojo.taskmanager.domain.model.SyncStatus
import com.jojothemojo.taskmanager.domain.model.Task
import com.jojothemojo.taskmanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

// Network-fetch-into-Room on read (GET), and createTask/updateTask/deleteTask write to Room
// immediately (so the UI never blocks on the network) then hand off to SyncScheduler, which
// enqueues SyncWorker to push the change to the backend asynchronously. See AGENT.md §5 for
// the full sync design: PENDING_* status transitions, last-write-wins conflict handling, and
// what's deliberately still out of scope (no soft-delete/tombstones).
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskApiService: TaskApiService,
    private val syncScheduler: SyncScheduler,
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeActiveTasks()
            .onStart { refreshFromNetwork() }
            .map { entities -> entities.map { it.toDomain() } }

    private suspend fun refreshFromNetwork() {
        try {
            taskApiService.getTasks().forEach { dto -> taskDao.insert(dto.toDomain().toEntity()) }
        } catch (e: IOException) {
            // Backend unreachable (not running, no network) - Room keeps serving whatever
            // was last successfully fetched. Not surfaced as an error state yet.
        } catch (e: HttpException) {
            // Backend reachable but returned a non-2xx (e.g. 401 if silent token refresh
            // also failed) - same degrade-to-cached-data behavior as above.
        }
    }

    override suspend fun getTask(id: String): Task? = taskDao.getById(id)?.toDomain()

    override suspend fun createTask(task: Task) {
        val now = Instant.now()
        val newTask = task.copy(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_CREATE,
        )
        taskDao.insert(newTask.toEntity())
        syncScheduler.scheduleImmediateSync()
    }

    override suspend fun updateTask(task: Task) {
        val existing = taskDao.getById(task.id) ?: return
        // A task that was never synced still just needs a single eventual POST,
        // not a POST followed by a PATCH — keep it PENDING_CREATE.
        val nextStatus = if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            SyncStatus.PENDING_CREATE
        } else {
            SyncStatus.PENDING_UPDATE
        }
        taskDao.update(task.copy(updatedAt = Instant.now(), syncStatus = nextStatus).toEntity())
        syncScheduler.scheduleImmediateSync()
    }

    override suspend fun deleteTask(id: String) {
        val existing = taskDao.getById(id) ?: return
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            // Never made it to the server, so there's nothing to tell it about — just drop it.
            taskDao.deleteById(id)
        } else {
            taskDao.update(existing.copy(syncStatus = SyncStatus.PENDING_DELETE, updatedAt = Instant.now()))
            syncScheduler.scheduleImmediateSync()
        }
    }
}
