package de.shopme.data.datasource.room

import androidx.room.Database
import androidx.room.RoomDatabase
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import androidx.room.TypeConverters
import de.shopme.data.datasource.room.StoreTypeConverter

@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StoreTypeConverter::class)
abstract class ShopMeDatabase : RoomDatabase() {

    abstract fun listDao(): ListDao
    abstract fun itemDao(): ItemDao

}