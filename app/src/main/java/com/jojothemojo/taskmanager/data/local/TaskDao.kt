package com.jojothemojo.taskmanager.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jojothemojo.taskmanager.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE syncStatus != :excludedStatus ORDER BY createdAt DESC")
    fun observeActiveTasks(excludedStatus: SyncStatus = SyncStatus.PENDING_DELETE): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE syncStatus != :syncedStatus")
    suspend fun getPendingSync(syncedStatus: SyncStatus = SyncStatus.SYNCED): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)
}
