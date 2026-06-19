package de.shopme.data.sync.queue

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.shopme.data.sync.QueueState

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

    /**
     * Replay lifecycle state.
     *
     * Stored as String for Room compatibility.
     *
     * Values:
     * - PENDING
     * - PROCESSING
     * - RETRY_WAIT
     * - DONE
     * - DEAD
     */
    val state: String = QueueState.PENDING.name,

    /**
     * Number of replay attempts.
     */
    val retryCount: Int = 0,

    /**
     * Timestamp of the last replay attempt.
     */
    val lastAttemptAt: Long? = null,

    /**
     * Earliest allowed retry timestamp.
     *
     * Used for exponential backoff scheduling.
     */
    val nextRetryAt: Long? = null,

    /**
     * Optional progress metadata for long-running sync operations.
     */
    val progress: Float? = null,

    /**
     * Last replay error for debugging / observability.
     */
    val errorMessage: String? = null,

    /**
     * Timestamp when processing started.
     *
     * Used for process death recovery and stuck replay detection.
     */
    val processingStartedAt: Long? = null,

    /**
     * Local entity version at queue creation time.
     *
     * Used for stale replay protection.
     */
    val baseVersion: Long
)