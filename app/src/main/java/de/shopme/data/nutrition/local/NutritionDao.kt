package de.shopme.data.nutrition.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {

    @Transaction
    @Query("""
        SELECT *
        FROM nutrition_products
        WHERE barcode = :barcode
    """)
    suspend fun getProduct(
        barcode: String
    ): NutritionProductWithInfo?

    @Transaction
    @Query("""
        SELECT *
        FROM nutrition_products
        WHERE barcode = :barcode
    """)
    fun observeProduct(
        barcode: String
    ): Flow<NutritionProductWithInfo?>



    @Query("""
    DELETE FROM nutrition_info
    WHERE barcode = :barcode
    """)
    suspend fun deleteNutritionInfo(
        barcode: String
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun upsertProduct(
        entity: NutritionProductEntity
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun upsertNutritionInfo(
        entity: NutritionInfoEntity
    )

    @Transaction
    suspend fun upsertProductWithNutrition(
        product: NutritionProductEntity,
        nutrition: NutritionInfoEntity?
    ) {

        upsertProduct(product)

        if (nutrition != null) {
            upsertNutritionInfo(nutrition)
        } else {
            deleteNutritionInfo(product.barcode)
        }
    }
}