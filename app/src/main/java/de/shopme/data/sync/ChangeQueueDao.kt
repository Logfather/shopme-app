package de.shopme.data.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

import de.shopme.data.sync.ChangeQueueEntity


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

    @Query("UPDATE change_queue SET state = :state WHERE id = :id")
    suspend fun updateState(id: String, state: String)

    @Query("DELETE FROM change_queue WHERE state = 'DONE'")
    suspend fun deleteCompleted()

    @Query("SELECT * FROM change_queue WHERE state = 'PENDING' ORDER BY createdAt ASC LIMIT :limit")
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
    SELECT entityId, state, progress
    FROM change_queue
    WHERE state IN ('PENDING', 'SYNCING', 'FAILED')
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
    WHERE id = :id
    """)
    suspend fun markSyncing(id: String, timestamp: Long)
}



data class SyncStateTuple(
    val entityId: String,
    val state: String,
    val progress: Float?
)