package com.jojothemojo.taskmanager.data.local

import androidx.room.TypeConverter
import com.jojothemojo.taskmanager.domain.model.SyncStatus
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromEpochMilli(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun toEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun fromSyncStatusName(value: String): SyncStatus = SyncStatus.valueOf(value)

    @TypeConverter
    fun toSyncStatusName(status: SyncStatus): String = status.name
}
