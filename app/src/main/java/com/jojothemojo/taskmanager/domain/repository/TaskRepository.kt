package com.jojothemojo.taskmanager.domain.repository

import com.jojothemojo.taskmanager.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun getTask(id: String): Task?

    // id/createdAt/updatedAt/syncStatus on the passed Task are ignored — the
    // repository stamps its own (new UUID, now(), PENDING_CREATE).
    suspend fun createTask(task: Task)

    // updatedAt/syncStatus are re-stamped by the repository; everything else
    // is taken from the passed Task as-is.
    suspend fun updateTask(task: Task)

    suspend fun deleteTask(id: String)
}
