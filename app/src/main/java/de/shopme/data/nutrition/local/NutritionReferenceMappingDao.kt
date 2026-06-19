package de.shopme.data.nutrition.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NutritionReferenceMappingDao {

    @Query(
        """
        SELECT barcode
        FROM nutrition_reference_mapping
        WHERE reference = :reference
        LIMIT 1
        """
    )
    suspend fun getBarcodeForReference(
        reference: String
    ): String?

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun upsertMapping(
        mapping: NutritionReferenceMappingEntity
    )
}