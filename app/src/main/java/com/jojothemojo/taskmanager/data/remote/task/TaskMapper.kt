package com.jojothemojo.taskmanager.data.remote.task

import com.jojothemojo.taskmanager.domain.model.SyncStatus
import com.jojothemojo.taskmanager.domain.model.Task
import java.time.OffsetDateTime

// .NET's DateTimeOffset serializes with an explicit numeric offset (e.g. "+00:00"), not
// always a literal "Z" - OffsetDateTime.parse handles both; Instant.parse only handles "Z".
private fun String.toInstant() = OffsetDateTime.parse(this).toInstant()

fun TaskDto.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDate = dueDate?.toInstant(),
    createdAt = createdAt.toInstant(),
    updatedAt = updatedAt.toInstant(),
    // Fetched fresh from the server, so it's synced by definition.
    syncStatus = SyncStatus.SYNCED,
)
