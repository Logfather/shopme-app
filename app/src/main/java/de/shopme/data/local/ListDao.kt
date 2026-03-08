package de.shopme.data.local

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

}