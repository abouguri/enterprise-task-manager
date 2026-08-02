package com.jojothemojo.taskmanager.domain.usecase

import com.jojothemojo.taskmanager.domain.model.Task
import com.jojothemojo.taskmanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(): Flow<List<Task>> = taskRepository.observeTasks()
}
