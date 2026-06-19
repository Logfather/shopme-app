package de.shopme.data.sync.queue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChangeQueueDao {

    // ============================================================
    // INSERT
    // ============================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(change: ChangeQueueEntity)

    // ============================================================
    // PROCESSING
    // ============================================================

    @Query("""
    UPDATE change_queue
    SET
        progress = :progress,
        state = 'PROCESSING'
    WHERE id = :id
    """)
    suspend fun updateProgress(
        id: String,
        progress: Float
    )

    @Query("""
    UPDATE change_queue
    SET
        state = :state,
        lastAttemptAt = :timestamp
    WHERE id = :id
    """)
    suspend fun updateProcessingState(
        id: String,
        state: String,
        timestamp: Long
    )

    @Query("""
    UPDATE change_queue
    SET
        state = 'PROCESSING',
        lastAttemptAt = :timestamp
    WHERE id = :id
    AND state IN ('PENDING', 'RETRY_WAIT')
    """)
    suspend fun markProcessingIfPendingInternal(
        id: String,
        timestamp: Long
    )

    // ============================================================
    // RETRY
    // ============================================================

    @Query("""
    UPDATE change_queue
    SET
        state = :state,
        retryCount = :retryCount,
        lastAttemptAt = :timestamp,
        nextRetryAt = :nextRetryAt
    WHERE id = :id
    """)
    suspend fun updateRetry(
        id: String,
        state: String,
        retryCount: Int,
        timestamp: Long,
        nextRetryAt: Long
    )

    @Query("""
    UPDATE change_queue
    SET nextRetryAt = NULL
    WHERE state IN ('PENDING', 'RETRY_WAIT')
    """)
    suspend fun resetRetryBackoff()

    // ============================================================
    // STATE
    // ============================================================

    @Query("""
    UPDATE change_queue
    SET state = :state
    WHERE id = :id
    AND state = 'PROCESSING'
    """)
    suspend fun updateState(
        id: String,
        state: String
    )

    @Query("""
    SELECT state
    FROM change_queue
    WHERE id = :id
    """)
    suspend fun getState(id: String): String?

    // ============================================================
    // PENDING
    // ============================================================

    @Query("""
    SELECT * FROM change_queue
    WHERE
        (
            state = 'PENDING'
            OR
            state = 'RETRY_WAIT'
        )
    AND
        (
            nextRetryAt IS NULL
            OR
            nextRetryAt <= :now
        )
    ORDER BY createdAt ASC
    """)
    suspend fun getPendingChanges(
        now: Long = System.currentTimeMillis()
    ): List<ChangeQueueEntity>

    @Query("""
    SELECT * FROM change_queue
    WHERE
        (
            state = 'PENDING'
            OR
            state = 'RETRY_WAIT'
        )
    AND
        (
            nextRetryAt IS NULL
            OR
            nextRetryAt <= :now
        )
    ORDER BY createdAt ASC
    LIMIT 1
    """)
    suspend fun getOldestPendingChange(
        now: Long = System.currentTimeMillis()
    ): ChangeQueueEntity?

    @Query("""
    SELECT * FROM change_queue
    WHERE
        state = 'PENDING'
        OR
        state = 'RETRY_WAIT'
    ORDER BY createdAt ASC
    LIMIT 1
    """)
    suspend fun getOldestPendingChangeIgnoringRetry():
            ChangeQueueEntity?

    @Query("""
    SELECT * FROM change_queue
    WHERE
        (
            state = 'PENDING'
            OR
            state = 'RETRY_WAIT'
        )
    AND
        (
            nextRetryAt IS NULL
            OR
            nextRetryAt <= :now
        )
    ORDER BY createdAt ASC
    LIMIT :limit
    """)
    suspend fun getPending(
        limit: Int,
        now: Long = System.currentTimeMillis()
    ): List<ChangeQueueEntity>

    // ============================================================
    // OBSERVE
    // ============================================================

    @Query("""
    SELECT entityId, state, progress, createdAt, retryCount
    FROM change_queue
    WHERE state IN (
        'PENDING',
        'PROCESSING',
        'RETRY_WAIT',
        'FAILED',
        'DONE'
    )
    """)
    fun observeSyncStates(): Flow<List<SyncStateTuple>>

    @Query("""
    SELECT state, COUNT(*) as count
    FROM change_queue
    GROUP BY state
    """)
    fun observeQueueStats(): Flow<List<QueueStateCount>>

    // ============================================================
    // RETRY ACTIONS
    // ============================================================

    @Query("""
    UPDATE change_queue
    SET
        state = 'PENDING',
        progress = 0
    WHERE entityId = :entityId
    AND state IN ('FAILED', 'RETRY_WAIT')
    """)
    suspend fun retryFailedChanges(
        entityId: String
    )

    // ============================================================
    // ENTITY LOOKUPS
    // ============================================================

    @Query("""
    SELECT * FROM change_queue
    WHERE entityId = :entityId
    AND state IN (
        'PENDING',
        'PROCESSING',
        'RETRY_WAIT'
    )
    """)
    suspend fun getActiveByEntityId(
        entityId: String
    ): List<ChangeQueueEntity>

    @Query("""
    SELECT * FROM change_queue
    WHERE entityId = :entityId
    AND state IN (
        'PENDING',
        'PROCESSING',
        'RETRY_WAIT'
    )
    ORDER BY createdAt DESC
    LIMIT 1
    """)
    suspend fun getLatestActiveByEntityId(
        entityId: String
    ): ChangeQueueEntity?

    @Query("""
    SELECT * FROM change_queue
    WHERE entityId = :entityId
    AND state IN (
        'PENDING',
        'PROCESSING',
        'RETRY_WAIT'
    )
    ORDER BY createdAt DESC
    LIMIT 1
    """)
    suspend fun getLatestPendingByEntityId(
        entityId: String
    ): ChangeQueueEntity?

    @Query("""
    SELECT * FROM change_queue
    WHERE entityId = :entityId
    AND state = 'PENDING'
    """)
    suspend fun getPendingByEntityId(
        entityId: String
    ): List<ChangeQueueEntity>

    @Query("""
    SELECT * FROM change_queue
    WHERE entityId = :entityId
    AND state = 'PENDING'
    """)
    suspend fun getPendingForEntity(
        entityId: String
    ): List<ChangeQueueEntity>

    // ============================================================
    // CLEANUP
    // ============================================================

    @Query("""
    DELETE FROM change_queue
    WHERE state = 'DONE'
    """)
    suspend fun deleteCompleted()

    @Query("""
    DELETE FROM change_queue
    WHERE id = :id
    """)
    suspend fun deleteById(id: String)

    @Query("""
    DELETE FROM change_queue
    WHERE entityId = :entityId
    AND entityType = 'item'
    AND operation = 'UPDATE'
    AND state IN (
        'PENDING',
        'PROCESSING',
        'RETRY_WAIT'
    )
    """)
    suspend fun deletePendingUpdatesForEntity(
        entityId: String
    )

    @Query("""
    DELETE FROM change_queue
    """)
    suspend fun clearAll()

    // ============================================================
    // RECOVERY
    // ============================================================

    @Query("""
    UPDATE change_queue
    SET
        state = 'PENDING'
    WHERE state = 'PROCESSING'
    """)
    suspend fun recoverInterruptedProcessing()

    // ============================================================
    // MISC
    // ============================================================

    @Query("""
    UPDATE change_queue
    SET state = 'PENDING'
    WHERE entityId = :itemId
    """)
    suspend fun markPendingByEntityId(
        itemId: String
    )

    @Query("""
    UPDATE change_queue
    SET state = 'DONE'
    WHERE entityId = :entityId
    """)
    suspend fun markDoneByEntityId(
        entityId: String
    )

    @Query("""
    SELECT * FROM change_queue
    WHERE payload LIKE :query
    ORDER BY lastAttemptAt DESC
    LIMIT 1
    """)
    suspend fun getLatestChangeForItem(
        query: String
    ): ChangeQueueEntity?

    @Query("""
    UPDATE change_queue
    SET
        baseVersion = :baseVersion,
        createdAt = :createdAt
    WHERE id = :id
    """)
    suspend fun updateBaseVersionAndTimestamp(
        id: String,
        baseVersion: Long,
        createdAt: Long
    )

    @Query("""
    UPDATE change_queue
    SET
        baseVersion = :baseVersion,
        createdAt = :now
    WHERE id = :id
    """)
    suspend fun updateBaseVersion(
        id: String,
        baseVersion: Long,
        now: Long = System.currentTimeMillis()
    )

    @Query("""
    SELECT * FROM change_queue
    """)
    suspend fun getAllChanges(): List<ChangeQueueEntity>

    @Query("""
    SELECT * FROM change_queue
    WHERE id = :id
    LIMIT 1
    """)
    suspend fun getChangeById(
        id: String
    ): ChangeQueueEntity?

    @Query("""
    UPDATE change_queue
    SET
        state = 'PROCESSING',
        lastAttemptAt = :timestamp
    WHERE id = :id
    AND (
        state = 'PENDING'
        OR
        state = 'RETRY_WAIT'
    )
    """)
    suspend fun claimProcessingOwnership(
        id: String,
        timestamp: Long
    ): Int
}

data class QueueStateCount(
    val state: String,
    val count: Int
)

data class SyncStateTuple(
    val entityId: String,
    val state: String,
    val progress: Float?,
    val createdAt: Long,
    val retryCount: Int
)