package de.shopme.data.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChangeQueueDao {

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
    SELECT entityId, state 
    FROM change_queue
""")
    fun observeSyncStates(): Flow<List<SyncStateTuple>>
}

data class SyncStateTuple(
    val entityId: String,
    val state: String
)