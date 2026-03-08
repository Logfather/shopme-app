package de.shopme.data.local

import androidx.room.TypeConverter
import de.shopme.domain.model.StoreType

class StoreTypeConverter {

    @TypeConverter
    fun fromStoreTypes(types: List<StoreType>): String {
        return types.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toStoreTypes(value: String): List<StoreType> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { StoreType.valueOf(it) }
    }

}