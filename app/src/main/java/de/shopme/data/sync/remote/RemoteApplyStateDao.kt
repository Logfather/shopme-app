package de.shopme.data.sync.remote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteApplyStateDao {

    @Query("""
        SELECT * FROM remote_apply_state
        WHERE entityId = :entityId
        LIMIT 1
    """)
    suspend fun getState(
        entityId: String
    ): RemoteApplyStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(
        state: RemoteApplyStateEntity
    )

    @Query("""
        DELETE FROM remote_apply_state
    """)
    suspend fun clearAll()
}