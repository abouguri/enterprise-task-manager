package com.jojothemojo.taskmanager.data.local

import com.jojothemojo.taskmanager.domain.model.Task

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDate = dueDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDate = dueDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
)
