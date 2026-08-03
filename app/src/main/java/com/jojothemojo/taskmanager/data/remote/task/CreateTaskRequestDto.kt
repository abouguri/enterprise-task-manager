package com.jojothemojo.taskmanager.data.remote.task

import kotlinx.serialization.Serializable

// Mirrors the backend's CreateTaskRequest exactly - deliberately no id/userId fields.
// Both are set server-side (Id generated, UserId from the caller's token) regardless of
// what the client sends, so there's no point (and real risk) in sending either.
@Serializable
data class CreateTaskRequestDto(
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val dueDate: String?,
)
