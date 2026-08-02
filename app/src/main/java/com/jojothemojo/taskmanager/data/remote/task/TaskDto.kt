package com.jojothemojo.taskmanager.data.remote.task

import kotlinx.serialization.Serializable

// Mirrors TaskManager.Api's TaskEntity JSON shape exactly - no syncStatus field, the
// backend doesn't have or want that (it's a client-only offline-queue concept).
@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val dueDate: String?,
    val createdAt: String,
    val updatedAt: String,
)
