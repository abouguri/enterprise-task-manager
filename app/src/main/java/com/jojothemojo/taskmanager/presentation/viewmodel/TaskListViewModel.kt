package com.jojothemojo.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojothemojo.taskmanager.domain.model.SyncStatus
import com.jojothemojo.taskmanager.domain.model.Task
import com.jojothemojo.taskmanager.domain.usecase.CreateTaskUseCase
import com.jojothemojo.taskmanager.domain.usecase.DeleteTaskUseCase
import com.jojothemojo.taskmanager.domain.usecase.GetTasksUseCase
import com.jojothemojo.taskmanager.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    getTasksUseCase: GetTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = getTasksUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val now = Instant.now()
            createTaskUseCase(
                Task(
                    id = "",
                    title = title,
                    description = null,
                    isCompleted = false,
                    dueDate = null,
                    createdAt = now,
                    updatedAt = now,
                    syncStatus = SyncStatus.PENDING_CREATE,
                )
            )
        }
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { deleteTaskUseCase(task.id) }
    }
}
