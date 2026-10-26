package com.example.dajho.data.local

import androidx.room.TypeConverter
import com.example.dajho.data.local.entity.SyncStatus

/**
 * Type Converters para Room (Fase 2).
 */
class Converters {

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(statusStr: String): SyncStatus {
        return try {
            SyncStatus.valueOf(statusStr)
        } catch (_: Exception) {
            SyncStatus.PENDING_INSERT
        }
    }
}
