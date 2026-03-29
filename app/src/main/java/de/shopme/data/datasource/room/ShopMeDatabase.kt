package de.shopme.data.datasource.room

import androidx.room.Database
import androidx.room.RoomDatabase
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.ChangeQueueEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        ChangeQueueEntity::class
    ],
    version = 4,
    exportSchema = false
)


@TypeConverters(
    StoreTypeConverter::class,
    StringListConverter::class // 🔥 NEU
)
abstract class ShopMeDatabase : RoomDatabase() {

    abstract fun listDao(): ListDao
    abstract fun itemDao(): ItemDao
    abstract fun changeQueueDao(): ChangeQueueDao

    companion object {

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
                    ALTER TABLE items 
                    ADD COLUMN listId TEXT NOT NULL DEFAULT ''
                """.trimIndent())
            }
        }
    }
}