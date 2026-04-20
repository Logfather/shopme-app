package de.shopme.data.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

import de.shopme.data.sync.ChangeQueueEntity
import de.shopme.domain.model.ShoppingItemEntity


@Dao
interface ChangeQueueDao {

    @Query("""
    UPDATE change_queue
    SET progress = :progress,
        state = 'SYNCING'
    WHERE id = :id
""")
    suspend fun updateProgress(
        id: String,
        progress: Float
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(change: ChangeQueueEntity)

    @Query("SELECT * FROM change_queue WHERE state = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingChanges(): List<ChangeQueueEntity>

    @Query("""
    UPDATE change_queue
    SET state = :state
    WHERE id = :id AND state = 'SYNCING'
    """)
    suspend fun updateState(id: String, state: String)

    @Query("DELETE FROM change_queue WHERE state = 'DONE'")
    suspend fun deleteCompleted()

    @Query("""
    SELECT * FROM change_queue
    WHERE state = 'PENDING'
    ORDER BY createdAt ASC
    LIMIT :limit
""")
    suspend fun getPending(limit: Int): List<ChangeQueueEntity>

    @Query("""
        UPDATE change_queue 
        SET state = :state, 
            retryCount = :retryCount,
            lastAttemptAt = :timestamp
        WHERE id = :id
        """)
    suspend fun updateRetry(
        id: String,
        state: String,
        retryCount: Int,
        timestamp: Long
    )

    @Query("""
    SELECT entityId, state, progress, createdAt
    FROM change_queue
    WHERE state IN ('PENDING', 'SYNCING', 'FAILED', 'DONE')
""")
    fun observeSyncStates(): Flow<List<SyncStateTuple>>

    @Query("""
    UPDATE change_queue
    SET state = 'PENDING',
        progress = 0
    WHERE entityId = :entityId
    AND state = 'FAILED'
""")
    suspend fun retryFailedChanges(entityId: String)

    @Query("""
        SELECT * FROM change_queue
        WHERE payload LIKE :query
        ORDER BY lastAttemptAt DESC
        LIMIT 1
        """)
    suspend fun getLatestChangeForItem(query: String): ChangeQueueEntity?

    @Query("""
    UPDATE change_queue
    SET state = 'SYNCING',
        lastAttemptAt = :timestamp
    WHERE id = :id AND state = 'PENDING'
""")
    suspend fun markSyncing(id: String, timestamp: Long)

    @Query("""
        UPDATE change_queue
        SET state = 'SYNCING',
            lastAttemptAt = :timestamp
        WHERE id = :id AND state = 'PENDING'
        """)
    suspend fun markSyncingIfPendingInternal(id: String, timestamp: Long)

    @Query("SELECT state FROM change_queue WHERE id = :id")
    suspend fun getState(id: String): String?

    @Query("DELETE FROM change_queue")
    suspend fun clearAll()

    @Query("UPDATE change_queue SET state = 'PENDING' WHERE entityId = :itemId")
    suspend fun markPendingByEntityId(itemId: String)

    @Query("""
    SELECT * FROM change_queue 
    WHERE entityId = :entityId 
    AND state = 'PENDING'
""")
    suspend fun getPendingForEntity(entityId: String): List<ChangeQueueEntity>

    @Query("SELECT * FROM change_queue WHERE entityId = :entityId AND state = 'PENDING'")
    suspend fun getPendingByEntityId(entityId: String): List<ChangeQueueEntity>

    @Query("""
    SELECT * FROM change_queue 
    WHERE entityId = :entityId 
    AND state IN ('PENDING', 'SYNCING')
""")
    suspend fun getActiveByEntityId(entityId: String): List<ChangeQueueEntity>

    @Query("""
    UPDATE change_queue 
    SET state = 'DONE' 
    WHERE entityId = :entityId
""")
    suspend fun markDoneByEntityId(entityId: String)

    @Query("""
    DELETE FROM change_queue
    WHERE entityId = :entityId
    AND entityType = 'item'
    AND operation = 'UPDATE'
    AND state IN ('PENDING', 'SYNCING')
""")
    suspend fun deletePendingUpdatesForEntity(entityId: String)

    @Query("UPDATE items SET isChecked = :checked, updatedAt = :timestamp WHERE id = :itemId")
    suspend fun updateChecked(itemId: String, checked: Boolean, timestamp: Long)

    @Query("DELETE FROM change_queue WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
    SELECT * FROM change_queue 
    WHERE entityId = :entityId 
    AND state != 'DONE'
    ORDER BY createdAt DESC
    LIMIT 1
""")
    suspend fun getLatestPendingByEntityId(entityId: String): ChangeQueueEntity?

}



data class SyncStateTuple(
    val entityId: String,
    val state: String,
    val progress: Float?,
    val createdAt: Long // 🔥 NEU
)