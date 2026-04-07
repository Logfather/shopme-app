package de.shopme.data.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "change_queue")
data class ChangeQueueEntity(

    @PrimaryKey
    val id: String,

    val entityType: String,

    val entityId: String,

    val listId: String,

    val operation: String,

    val payload: String?,

    val createdAt: Long,

    val state: String,

    val retryCount: Int = 0,

    val lastAttemptAt: Long? = null,

    val progress: Float? = null,

    val errorMessage: String? = null,

    // 🔥 baseVersion = updatedAt snapshot zum Zeitpunkt der Änderung (Millis!)
    val baseVersion: Long
)

enum class SyncStatus {
    PENDING,
    SYNCING,
    FAILED,
    SYNCED
}