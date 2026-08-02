package com.jojothemojo.taskmanager.data.repository

import com.jojothemojo.taskmanager.data.local.TaskDao
import com.jojothemojo.taskmanager.data.local.toDomain
import com.jojothemojo.taskmanager.data.local.toEntity
import com.jojothemojo.taskmanager.domain.model.SyncStatus
import com.jojothemojo.taskmanager.domain.model.Task
import com.jojothemojo.taskmanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeActiveTasks().map { entities -> entities.map { it.toDomain() } }

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
    }

    override suspend fun deleteTask(id: String) {
        val existing = taskDao.getById(id) ?: return
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            // Never made it to the server, so there's nothing to tell it about — just drop it.
            taskDao.deleteById(id)
        } else {
            taskDao.update(existing.copy(syncStatus = SyncStatus.PENDING_DELETE, updatedAt = Instant.now()))
        }
    }
}
