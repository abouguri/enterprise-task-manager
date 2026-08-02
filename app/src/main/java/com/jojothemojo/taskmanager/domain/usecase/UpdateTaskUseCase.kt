package com.jojothemojo.taskmanager.domain.usecase

import com.jojothemojo.taskmanager.domain.model.Task
import com.jojothemojo.taskmanager.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(task: Task) = taskRepository.updateTask(task)
}
