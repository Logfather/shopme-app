package de.shopme.data.datasource.room

import androidx.room.*
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {

    @Query("SELECT * FROM lists")
    fun observeLists(): Flow<List<ShoppingListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<ShoppingListEntity>)

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

}