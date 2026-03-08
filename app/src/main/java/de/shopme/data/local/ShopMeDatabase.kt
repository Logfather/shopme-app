package de.shopme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import androidx.room.TypeConverters

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