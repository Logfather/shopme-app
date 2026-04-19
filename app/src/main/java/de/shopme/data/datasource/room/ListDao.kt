package de.shopme.data.datasource.room

import androidx.room.*
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {

    @Query("SELECT * FROM lists WHERE deletedAt IS NULL")
    fun observeLists(): Flow<List<ShoppingListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<ShoppingListEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingListEntity)

    @Delete
    suspend fun deleteList(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: ShoppingListEntity)

    @Query("DELETE FROM lists")
    suspend fun clearAll()

    @Query("DELETE FROM lists WHERE id = :listId")
    suspend fun deleteListById(listId: String)

    @Query("SELECT * FROM lists WHERE id = :listId LIMIT 1")
    suspend fun getListOnce(listId: String): ShoppingListEntity?

    @Query("UPDATE lists SET deletedAt = :timestamp WHERE id = :listId")
    suspend fun markDeleted(listId: String, timestamp: Long)

    @Query("SELECT id FROM lists")
    fun getAllListIdsSync(): List<String>

    @Query("SELECT * FROM lists WHERE id = :id LIMIT 1")
    suspend fun getListById(id: String): ShoppingListEntity?

    @Query("DELETE FROM lists WHERE id = :listId")
    suspend fun deleteById(listId: String)

    @Query("DELETE FROM lists WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<String>)

    @Query("SELECT * FROM lists WHERE deletedAt IS NULL")
    suspend fun observeListsOnce(): List<ShoppingListEntity>

    // ============================================================
    // ✅ ADDITION: Required for AccountDeletionManager
    // ============================================================
    @Query("SELECT * FROM lists WHERE deletedAt IS NULL")
    suspend fun getAllListsOnce(): List<ShoppingListEntity>
}