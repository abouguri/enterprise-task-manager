package com.jojothemojo.taskmanager.domain.usecase

import com.jojothemojo.taskmanager.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(id: String) = taskRepository.deleteTask(id)
}
