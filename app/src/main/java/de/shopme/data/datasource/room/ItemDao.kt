package de.shopme.data.datasource.room

import androidx.room.*
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items")
    fun observeItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingItemEntity>)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("SELECT * FROM items WHERE deletedAt IS NULL")
    fun observeActiveItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ShoppingItemEntity)

    @Query("DELETE FROM items")
    suspend fun clearAll()

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ShoppingItemEntity?

    @Query("""
    SELECT * FROM items
    WHERE listId = :listId
""")
    fun observeItemsForList(listId: String): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM items WHERE listId = :listId")
    suspend fun getItemsForList(listId: String): List<ShoppingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    @Query("DELETE FROM items WHERE listId = :listId")
    suspend fun deleteByListId(listId: String)

    @Query("""
    SELECT entityId FROM change_queue
    WHERE entityType = :type AND state IN ('PENDING', 'SYNCING')
    """)
    suspend fun getPendingEntityIds(type: String): List<String>

    @Query("""
    SELECT entityId FROM change_queue
    WHERE entityType = 'item'
    AND state IN ('PENDING', 'SYNCING')
""")
    suspend fun getPendingItemIds(): List<String>

    @Query("""
    SELECT * FROM items 
    WHERE listId = :listId 
    AND deletedAt IS NULL
    ORDER BY createdAt ASC
""")
    fun observeItems(listId: String): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    suspend fun getItemById(itemId: String): ShoppingItemEntity?

    @Query("""
    UPDATE items 
    SET isChecked = :checked, updatedAt = :updatedAt 
    WHERE id = :id
""")
    suspend fun updateChecked(
        id: String,
        checked: Boolean,
        updatedAt: Long
    )


}