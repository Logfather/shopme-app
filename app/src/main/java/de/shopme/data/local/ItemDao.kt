package de.shopme.data.local

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

}