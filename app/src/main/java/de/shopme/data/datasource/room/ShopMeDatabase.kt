package de.shopme.data.datasource.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.shopme.data.nutrition.local.NutritionDao
import de.shopme.data.nutrition.local.NutritionInfoEntity
import de.shopme.data.nutrition.local.NutritionProductEntity
import de.shopme.data.nutrition.local.NutritionReferenceMappingDao
import de.shopme.data.nutrition.local.NutritionReferenceMappingEntity
import de.shopme.data.sync.queue.ChangeQueueDao
import de.shopme.data.sync.queue.ChangeQueueEntity
import de.shopme.data.sync.remote.RemoteApplyStateDao
import de.shopme.data.sync.remote.RemoteApplyStateEntity
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        ChangeQueueEntity::class,
        RemoteApplyStateEntity::class,
        NutritionProductEntity::class,
        NutritionInfoEntity::class,
        NutritionReferenceMappingEntity::class
    ],
    version = 8,
    exportSchema = false
)

@TypeConverters(
    StoreTypeConverter::class,
    StringListConverter::class
)
abstract class ShopMeDatabase : RoomDatabase() {

    abstract fun listDao(): ListDao

    abstract fun itemDao(): ItemDao

    abstract fun changeQueueDao(): ChangeQueueDao

    abstract fun remoteApplyStateDao():
            RemoteApplyStateDao

    abstract fun nutritionDao(): NutritionDao

    abstract fun nutritionReferenceMappingDao():
            NutritionReferenceMappingDao

    companion object {

        val MIGRATION_5_6 =
            object : Migration(5, 6) {

                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {

                    database.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS remote_apply_state (
                        entityId TEXT NOT NULL PRIMARY KEY,
                        lastAppliedRemoteVersion INTEGER NOT NULL,
                        lastAppliedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                    )

                    database.execSQL(
                        """
                    ALTER TABLE change_queue
                    ADD COLUMN retryCount INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                    )

                    database.execSQL(
                        """
                    ALTER TABLE change_queue
                    ADD COLUMN lastAttemptAt INTEGER
                    """.trimIndent()
                    )

                    database.execSQL(
                        """
                    ALTER TABLE change_queue
                    ADD COLUMN nextRetryAt INTEGER
                    """.trimIndent()
                    )

                    database.execSQL(
                        """
                    ALTER TABLE change_queue
                    ADD COLUMN progress REAL
                    """.trimIndent()
                    )

                    database.execSQL(
                        """
                    ALTER TABLE change_queue
                    ADD COLUMN errorMessage TEXT
                    """.trimIndent()
                    )

                    database.execSQL(
                        """
                    ALTER TABLE change_queue
                    ADD COLUMN processingStartedAt INTEGER
                    """.trimIndent()
                    )
                }
            }

        val MIGRATION_6_7 =
            object : Migration(6, 7) {

                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {

                    // nutrition_products

                    // nutrition_info

                    // index
                }
            }

        val MIGRATION_7_8 =
            object : Migration(7, 8) {

                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {

                    database.execSQL(
                        """
                CREATE TABLE IF NOT EXISTS
                nutrition_reference_mapping (
                    reference TEXT NOT NULL PRIMARY KEY,
                    barcode TEXT NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent()
                    )
                }
            }
    }
}