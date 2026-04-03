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
}