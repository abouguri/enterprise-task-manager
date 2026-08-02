package com.jojothemojo.taskmanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jojothemojo.taskmanager.domain.model.SyncStatus
import java.time.Instant

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val dueDate: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
)
