package com.jojothemojo.taskmanager.domain.model

import java.time.Instant

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val dueDate: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
)
