package com.jojothemojo.taskmanager.data.remote.task

import kotlinx.serialization.Serializable

// Mirrors the backend's UpdateTaskRequest. updatedAt is this client's current view of the
// row, used ONLY for the server's last-write-wins staleness check - it is never persisted
// as-is. The server always stamps its own new UpdatedAt on a successful write and rejects
// the request with 409 (current server row in the body) if this value isn't strictly newer
// than what's stored. See TaskManager-Api's AGENT.md §5 for the exact server-side semantics.
@Serializable
data class UpdateTaskRequestDto(
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val dueDate: String?,
    val updatedAt: String,
)
