package de.shopme.data.sync.queue

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "change_queue",
    indices = [
        Index(value = ["entityId"], unique = true)
    ]
)
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

    val nextRetryAt: Long? = null,

    val progress: Float? = null,

    val errorMessage: String? = null,

    val baseVersion: Long
)

enum class SyncStatus {
    PENDING,
    SYNCING,
    FAILED,
    SYNCED
}